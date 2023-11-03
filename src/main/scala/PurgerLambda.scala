import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.gu.facia.client.{AmazonSdkS3Client, ApiClient}
import io.circe.syntax._
import okhttp3._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._




object PurgerLambda extends RequestHandler[SQSEvent, Boolean] {
  lazy val httpClient = new OkHttpClient()

  override def handleRequest(event: SQSEvent, context: Context): Boolean = {

    // Get the front path from the SQS message
    val scalaRecords = event.getRecords.asScala.toList
    val pressJobs: List[PressJob] = scalaRecords
      .flatMap(r => {
        PressJobMessage
          .toPressJobMessage(r.getBody) match {
          case Left(error) => None //TO-DO: log error message here
          case Right(pressJob) => Some(pressJob.Message)
        }
      })
    val purgerConfig: Config = Config.load()
    println("Facia role: " + purgerConfig.faciaRole)
    println("Fastly service id " + purgerConfig.fastlyServiceId)
    // Currently we do not expect to receive more than one front path in a message, but want to anticipate
    // for this changing in the future
    val frontPathList: List[String] = pressJobs.map(_.path)
    println("Front path list: " + frontPathList)

    // Setting up credentials to get the config.json from the cms fronts AWS profile
    // TO-DO: Check this is correct!

    val provider = new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("cmsFronts"),
      new STSAssumeRoleSessionCredentialsProvider.Builder(purgerConfig.faciaRole, "mobile-fastly-cache-purger").build(),
    )
    println("Provider: " + provider)
    val s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(provider).build()
    println("S3 client: " + s3Client)
    lazy val faciaS3Client = AmazonSdkS3Client(s3Client)
    println("faciaS3 client: " + faciaS3Client)
    val apiClient: ApiClient = new ApiClient("facia-tool-store", "CODE", faciaS3Client)
    println("apiClient: " + apiClient)

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
              case None => {
                // TO-DO: log error here
                None
              }
            }
          )
          .flatten
          .distinct // TO-DO: do we need to distinct?
      )
      // if we add the front path to the list of collections ids, we should be able to call the purge function once
      .map(collectionKeys => sendCollectionPurgeRequest(collectionKeys ++ frontPathList, purgerConfig))

    Await.result(allCollectionsForFront, 10.seconds)  // define the right timeout
                                                      // is it okay to use await here?

    true
  }

  // OkHttp requires a media type even for an empty POST body
  private def EmptyJsonBody: RequestBody =
    RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))

  private def JsonBody(collectionKeys: List[String]): RequestBody =
    RequestBody.create(
      Map("surrogate_keys" -> collectionKeys).asJson.noSpaces,
      MediaType.get("application/json; charset=utf-8")
    )

  /**
   * Send a soft purge request to Fastly API.
   */
  private def sendPurgeRequest(frontPath: String, purgerConfig: Config, contentId: String = ""): Boolean = {
    val surrogateKey = s"Front/$frontPath"
    val url = s"https://api.fastly.com/service/${purgerConfig.fastlyServiceId}/purge/$surrogateKey"

    val request = new Request.Builder()
      .url(url)
      .header("Fastly-Key", purgerConfig.fastlyApiKey)
      .header("Fastly-Soft-Purge", "1")
      .post(EmptyJsonBody)
      .build()

    val response = httpClient.newCall(request).execute()
    println(s"Sent purge request for content with ID [$surrogateKey]. Response from Fastly API: [${response.code}] [${response.body.string}]")

    response.code == 200
  }

  // TO-DO: rename if only this function is used
  private def sendCollectionPurgeRequest(collectionKeys: List[String], purgerConfig: Config): Boolean = {
    val url = s"https://api.fastly.com/service/${purgerConfig.fastlyServiceId}/purge"

    val request = new Request.Builder()
      .url(url)
      .header("Fastly-Key", purgerConfig.fastlyApiKey)
      .header("Fastly-Soft-Purge", "1")
      .post(JsonBody(collectionKeys))
      .build()

    val response = httpClient.newCall(request).execute()
    println(s"Sent purge request for collections ${collectionKeys}. Response from Fastly API: [${response.code}] [${response.body.string}]")

    response.code == 200
  }

}
