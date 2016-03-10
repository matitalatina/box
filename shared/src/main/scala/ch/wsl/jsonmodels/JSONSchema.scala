package ch.wsl.jsonmodels

case class JSONSchema(
  `type`:String,
  title:Option[String] = None,
  properties: Option[Map[String,JSONSchema]] = None,
  required: Option[Seq[String]] = None,
  readonly: Option[Boolean] = None,
  enum: Option[Seq[String]] = None,
  order: Option[Int] = None
)


