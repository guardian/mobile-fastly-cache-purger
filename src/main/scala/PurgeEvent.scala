import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._

object PurgeEvent {}

object PressJob {

  implicit val derivedDecoder: Decoder[PressJob] = deriveDecoder[PressJob]

  def toPressJob(input: String): Option[PressJob] = {
    println(s"decode: ${decode(input)}")
    decode(input).toOption
  }
}
case class PressJob(path: String, pressType: String)
