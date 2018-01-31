package ch.wsl.box.model.shared


case class EntityKind(kind:String){

  def entityOrForm = kind match{
    case "table"|"view" => "entity"
    case _ => kind
  }
  def plural:String = kind match {
    case "entity" => "entities"
    case _ => s"${kind}s"            //for tables, views and forms
  }
}


object EntityKind {
  final val ENTITY = EntityKind("entity")   //table or view
  final val TABLE = EntityKind("table")
  final val VIEW =  EntityKind("view")
  final val FORM =  EntityKind("form")

}
