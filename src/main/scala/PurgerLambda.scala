import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import okhttp3._

import scala.jdk.CollectionConverters._


object PurgerLambda extends RequestHandler[SQSEvent, Boolean] {
  lazy val httpClient = new OkHttpClient()
  override def handleRequest(event: SQSEvent, context: Context): Boolean = {

    println("event: ", event)
    val records: List[PressJob] = event.getRecords.asScala.toList.flatMap(r => PressJob.toPressJob(r.getBody))
    println("records: ", records)
    val frontPath: String = records.head.path
    println("frontPath: ", frontPath)

    println(s"Facia-purger lambda starting up")

    val config = Config.load()

    // OkHttp requires a media type even for an empty POST body
    val EmptyJsonBody: RequestBody =
      RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))

    /**
     * Send a soft purge request to Fastly API.
     */
    def sendPurgeRequest(contentId: String = ""): Boolean = {
      val surrogateKey = s"Front/$frontPath"
      val url = s"https://api.fastly.com/service/${config.fastlyServiceId}/purge/$surrogateKey"

      val request = new Request.Builder()
        .url(url)
        .header("Fastly-Key", config.fastlyApiKey)
        .header("Fastly-Soft-Purge", "1")
        .post(EmptyJsonBody)
        .build()

      val response = httpClient.newCall(request).execute()
      println(s"Sent purge request for content with ID [$surrogateKey]. Response from Fastly API: [${response.code}] [${response.body.string}]")

      response.code == 200
    }

    sendPurgeRequest()
    true
  }
}
