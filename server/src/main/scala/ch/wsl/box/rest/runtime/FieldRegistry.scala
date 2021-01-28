package ch.wsl.box.rest.runtime

case class ColType(name:String,nullable:Boolean)

trait FieldRegistry {

    def tables:Seq[String]
    def views:Seq[String]

    def field(table:String,column:String):ColType
}
