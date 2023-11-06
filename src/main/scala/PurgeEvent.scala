import io.circe._
import io.circe.generic.auto._
import io.circe.parser._


object PurgeEvent {}

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
case class PressJobMessage(Type: String,
                           MessageId: String,
                           TopicArn: String,
                           Message: String,
                           Timestamp: String,
                           SignatureVersion: String,
                           Signature: String,
                           SigningCertURL: String,
                           UnsubscribeURL: String)
