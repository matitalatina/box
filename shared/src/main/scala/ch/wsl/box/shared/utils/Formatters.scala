package ch.wsl.box.shared.utils

import ch.wsl.box.model.shared.{JSONMetadata, SubLayoutBlock}
import io.circe._

object Formatters {

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
