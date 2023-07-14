import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import okhttp3._


object PurgerLambda extends RequestHandler[S3Event, Boolean] {
  lazy val httpClient = new OkHttpClient()
  override def handleRequest(event: S3Event, context: Context): Boolean = {

    println(s"Facia-purger lambda starting up")

    val config = Config.load()

    // OkHttp requires a media type even for an empty POST body
    val EmptyJsonBody: RequestBody =
      RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))

    /**
     * Send a soft purge request to Fastly API.
     */
    def sendPurgeRequest(contentId: String = ""): Boolean = {
      val surrogateKey = "Container/uk/groups/collections/0dd06021-c399-4d40-92da-04055628ac7d"
      val url = s"https://api.fastly.com/service/${config.fastlyServiceId}/purge/$surrogateKey"

      val request = new Request.Builder()
        .url(url)
        .header("Fastly-Key", config.fastlyApiKey)
        .header("Fastly-Soft-Purge", "1")
        .post(EmptyJsonBody)
        .build()

      val response = httpClient.newCall(request).execute()
      println(s"Sent purge request for content with ID ''. Response from Fastly API: [${response.code}] [${response.body.string}]")

      response.code == 200
    }

    sendPurgeRequest()
    true
  }
}
