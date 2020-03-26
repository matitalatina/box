package ch.wsl.box.shared.utils

import ch.wsl.box.model.shared.{JSONMetadata, SubLayoutBlock}
import io.circe._

import java.time.LocalDateTime

object Formatters {

  import io.circe.{ Decoder, Encoder }
  import scala.util.Try

  implicit val encodeLocalDateTime: Encoder[LocalDateTime] = Encoder.encodeString.contramap[LocalDateTime](_.toString)
  // encodeLocalDateTime: Encoder[LocalDateTime] = io.circe.Encoder$$anon$1@2a71e163

  implicit val decodeLocalDateTime: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str))
  }


  import io.circe.generic.auto._

  implicit val decodeFields: Decoder[Either[String,SubLayoutBlock]] = new Decoder[Either[String,SubLayoutBlock]] {
    override def apply(c: HCursor): Either[DecodingFailure, Either[String, SubLayoutBlock]] = {

      val string = c.as[String].right.map(Left(_))
      val subBlock = c.as[SubLayoutBlock].right.map(Right(_))

      if(subBlock.isRight) {
        subBlock
      } else {
        string
      }
    }
  }

  import io.circe.syntax._

  implicit val encodeFields: Encoder[Either[String,SubLayoutBlock]] = new Encoder[Either[String, SubLayoutBlock]] {
    override def apply(a: Either[String, SubLayoutBlock]): Json = a.fold(
      str => str.asJson,
      subBlock => subBlock.asJson
    )
  }

  import io.circe.generic.semiauto._
  implicit val fooDecoder: Decoder[JSONMetadata] = deriveDecoder[JSONMetadata]
  implicit val fooEncoder: Encoder[JSONMetadata] = deriveEncoder[JSONMetadata]
}
