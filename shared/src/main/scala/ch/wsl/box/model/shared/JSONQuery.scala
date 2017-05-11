package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 24/04/15.
  *
  * @param count how many rows
  * @param page page n of count rows
  * @param sort sort results by JSONSort object
  * @param filter result by JSONQueryFilter object
  */
case class JSONQuery(
                      count:Int,
                      page:Int,
                      sort:List[JSONSort],
                      filter:List[JSONQueryFilter]
                    )

/**
  * Apply operator to column/value
  *
  * @param column
  * @param operator
  * @param value
  */
case class JSONQueryFilter(column:String, operator:Option[String], value:String)

/**
  * Sort data by column
  *
  * @param column
  * @param order valid values are asc/desc
  */
case class JSONSort(column:String,order:String)

/**
  * Created by andreaminetti on 16/03/16.
  */
object JSONQuery{
  /**
    * Generic query
    */
  val baseQuery = JSONQuery(
    count = 30,
    page = 1,
    sort = List(),
    filter = List()
  )

  def limit(limit:Int) = baseQuery.copy(count = limit)
}

object Sort{
  final val DESC = "desc"
  final val IGNORE = ""
  final val ASC = "asc"

  def next(s:String) = s match {
    case DESC => IGNORE
    case ASC => DESC
    case IGNORE => ASC
  }
}

object Filter {
  final val NONE = "none"
}

