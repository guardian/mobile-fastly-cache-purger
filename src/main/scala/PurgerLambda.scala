import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import okhttp3._

import scala.jdk.CollectionConverters._

object PurgerLambda extends RequestHandler[SQSEvent, Boolean] {
  lazy val httpClient = new OkHttpClient()

  override def handleRequest(event: SQSEvent, context: Context): Boolean = {

    val scalaRecords = event.getRecords.asScala.toList
    val pressJobs: List[PressJob] = scalaRecords
      .flatMap(r => {
        PressJobMessage
          .toPressJobMessage(r.getBody) match {
          case Left(error) => None //TO-DO: log error message here
          case Right(pressJob) => Some(pressJob.Message)
        }
    })

    val frontPathList: List[String] = pressJobs.map(_.path) //we just need the path to send our fronts purge request


    val purgerConfig: Config = Config.load()

    frontPathList.foreach(frontPath => sendPurgeRequest(frontPath, purgerConfig))
    true
  }

  private def EmptyJsonBody: RequestBody =
    RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))

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
}
