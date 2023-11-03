import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._


object PurgeEvent {}

object PressJobMessage {

  implicit val derivedDecoderPressJob: Decoder[PressJob] = deriveDecoder[PressJob]
  implicit val derivedDecoderPressJobMessage: Decoder[PressJobMessage] = deriveDecoder[PressJobMessage]

  def toPressJobMessage(input: String): Either[Error, PressJobMessage] = {
    decode[PressJobMessage](input)
  }
}

object SimplePressJob {

  implicit val derivedDecoder: Decoder[SimplePressJob] = deriveDecoder[SimplePressJob]

  def toPressJob(input: String): Option[SimplePressJob] = {
    decode(input).toOption
  }
}
case class SimplePressJob(path: String, pressType: String)

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
