import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
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
    val frontPathList: List[String] = pressJobs.map(_.path)

    // Set up credentials to get the config.json from the CMS fronts S3 bucket
    val purgerConfig: Config = Config.load()
    val provider = new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("cmsFronts"),
      new STSAssumeRoleSessionCredentialsProvider.Builder(purgerConfig.faciaRole, "mobile-fastly-cache-purger").build(),
    )
    val s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(provider).build()
    lazy val faciaS3Client = AmazonSdkS3Client(s3Client)
    val apiClient: ApiClient = new ApiClient("facia-tool-store", "CODE", faciaS3Client) // Hardcoding to CODE for the time being

    // Take the front path (e.g. app/front-mss) and return the list of collection IDs in that front from the config.json
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
      .map(collectionKeys => sendPurgeRequest(collectionKeys.map(i => s"Container/uk/groups/collections/$i") ++ frontPathList.map(i => s"Front/$i"), purgerConfig))

    Await.result(allCollectionsForFront, 10.seconds) // define the right timeout

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
    logger.info(s"Sent purge request for ${collectionKeys}. Response from Fastly API: [${response.code}] [${response.body.string}]")

    response.code == 200
  }

}
