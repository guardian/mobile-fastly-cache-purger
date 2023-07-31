import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage

import java.time.Instant
import scala.jdk.CollectionConverters._

object PurgerLambdaLocalRun extends App {

  val sqsEvent: SQSEvent = {
    val event = new SQSEvent()
    val sqsMessage = new SQSMessage()
    sqsMessage.setBody("{\"path\":\"app/front-mss\",\"pressType\":\"draft\",\"creationTime\":1690799830981,\"forceConfigUpdate\":false}")
    sqsMessage.setAttributes((Map("SentTimestamp" -> s"${Instant.now.toEpochMilli}").asJava))
    event.setRecords(List(sqsMessage).asJava)
    event
  }

  PurgerLambda.handleRequest(sqsEvent, null)
}