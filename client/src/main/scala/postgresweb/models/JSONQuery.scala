package postgresweb.models

/**
  * Created by andreaminetti on 23/02/16.
  */
case class JSONQuery(
                      count:Int,
                      page:Int,
                      sorting:Map[String,String],
                      filter:Map[String,JSONQueryFilter]
                    )

case class JSONQueryFilter(value:String,operator:Option[String])

object JSONQuery{
  val baseFilter = JSONQuery(
    count = 30,
    page = 1,
    sorting = Map(),
    filter = Map()
  )
}