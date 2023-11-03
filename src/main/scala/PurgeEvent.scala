import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._


object PurgeEvent {}

object PressJobMessage {

  implicit val derivedDecoderPressJob: Decoder[PressJob] = deriveDecoder[PressJob]
  implicit val derivedDecoderPressJobMessage: Decoder[PressJobMessage] = deriveDecoder[PressJobMessage]

  def toPressJobMessage(input: String): Either[Error, PressJob] = {
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
                           Message: PressJob,
                           Timestamp: String,
                           SignatureVersion: Int,
                           Signature: String,
                           SigningCertURL: String,
                           UnsubscribeURL: String)
