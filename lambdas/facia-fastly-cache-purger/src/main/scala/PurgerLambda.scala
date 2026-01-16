
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.awssdk.auth.credentials._
import software.amazon.awssdk.regions.Region.EU_WEST_1
import com.gu.facia.client.{ApiClient, Environment}
import com.gu.etagcaching.aws.sdkv2.s3.S3ObjectFetching
import io.circe.syntax._
import okhttp3._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._


object PurgerLambda extends RequestHandler[SQSEvent, Boolean] {
  lazy val httpClient = new OkHttpClient()
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def handleRequest(event: SQSEvent, context: Context): Boolean = {

    // Get the front path from the SQS message
    val scalaRecords = event.getRecords.asScala.toList
    val pressJobs: List[PressJob] = scalaRecords
      .flatMap(r => {
        PressJobMessage
          .toPressJobMessage(r.getBody)
          .map(_.Message)
          .flatMap(PressJobMessage.toPressJob) match {
          case Left(error) =>
            logger.error(error.getMessage)
            None
          case Right(pressJob) => Some(pressJob)
        }
      })
    // Currently we do not expect to receive more than one front path in a message, but we want to anticipate
    // for this changing in the future
    val frontPathList: List[String] =
      pressJobs
        .filter(_.pressType != "draft") // We are not interested in draft changes
        .map(_.path)

    // Set up credentials to get the config.json from the CMS fronts S3 bucket
    val purgerConfig: Config = Config.load()

    val stsClient = StsClient.builder()
      .region(EU_WEST_1)
      .build()
    val assumeRoleRequest = AssumeRoleRequest.builder().roleArn(purgerConfig.faciaRole).roleSessionName( "mobile-fastly-cache-purger").build()


    val provider = AwsCredentialsProviderChain.builder()
      .credentialsProviders(
        ProfileCredentialsProvider.create("cmsFronts"),
        StsAssumeRoleCredentialsProvider.builder().refreshRequest(assumeRoleRequest).stsClient(stsClient).build()
      )
      .build()

    val s3AsyncClient = S3AsyncClient.builder()
      .region(EU_WEST_1)
      .credentialsProvider(provider)
      .build()

    val apiClient: ApiClient =  ApiClient.withCaching(
      "facia-tool-store",
      Environment(purgerConfig.faciaEnvironment.toUpperCase),
      S3ObjectFetching.byteArraysWith(s3AsyncClient)
    )

    // Take the front path (e.g. app/front-mss) and return the list of collection IDs in that front from the config.json
    if (frontPathList.isEmpty) {
      logger.warn("No fronts to send a purge request for")
    } else {
      val allCollectionsForFront: Future[Boolean] = apiClient
        .config
        .map(configJson =>
          frontPathList
            .flatMap(frontPath =>
              configJson
                .fronts
                .get(frontPath) match {
                case Some(frontJson) => Some(frontJson.collections)
                case None =>
                  logger.error("Front does not match any front in the config.json")
                  None
              }
            )
            .flatten
            .distinct
        )
        .map(collectionKeys => {
          val containerIds = collectionKeys.map(i => s"Container/$i")
          val bpContainerIds = collectionKeys.map(i => s"BlueprintContainer/$i")
          sendPurgeRequest(containerIds ::: bpContainerIds, purgerConfig)
        })

      Await.result(allCollectionsForFront, 10.seconds) // define the right timeout

    }
    true
  }

  // Create a JSON body to be used in the purge request.
  private def JsonBody(collectionKeys: List[String]): RequestBody =
    RequestBody.create(
      Map("surrogate_keys" -> collectionKeys).asJson.noSpaces,
      MediaType.get("application/json; charset=utf-8")
    )

  /**
   * Send a soft purge request to Fastly API.
   */

  private def sendPurgeRequest(collectionKeys: List[String], purgerConfig: Config): Boolean = {
    val url = s"https://api.fastly.com/service/${purgerConfig.fastlyServiceId}/purge"

    val request = new Request.Builder()
      .url(url)
      .header("Fastly-Key", purgerConfig.fastlyApiKey)
      .header("Fastly-Soft-Purge", "1")
      .post(JsonBody(collectionKeys))
      .build()

    val response = httpClient.newCall(request).execute()
    response.code() match {
      case 200 => logger.info(s"Sent purge request for ${collectionKeys}. Response from Fastly API: [${response.code}] [${response.body.string}]")
      case _ => logger.warn(s"An error occurred. Response from Fastly API: [${response.code}] [${response.body.string}]")
    }
    response.code == 200
  }

}
