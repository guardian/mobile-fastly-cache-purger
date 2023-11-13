import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

object PressJobMessage {

  def toPressJobMessage(input: String): Either[Error, PressJobMessage] = {
    decode[PressJobMessage](input)
  }

  def toPressJob(input: String): Either[Error, PressJob] = {
    decode[PressJob](input)
  }
}

case class PressJob(path: String,
                    pressType: String,
                    creationTime: Long,
                    forceConfigUpdate: Option[Boolean])


// This is the structure of the message received from the fronts tool.
case class PressJobMessage(`type`: String,
                           messageId: String,
                           topicArn: String,
                           message: String,
                           timestamp: String,
                           signatureVersion: String,
                           signature: String,
                           signingCertUrl: String,
                           unsubscribeUrl: String)
