package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 24/04/15.
  *
  * @param paging paging information
  * @param sort sort results by JSONSort object
  * @param filter result by JSONQueryFilter object
  */
case class JSONQuery(
                      paging:Option[JSONQueryPaging],
                      sort:List[JSONSort],
                      filter:List[JSONQueryFilter]
                    ){
  def page = paging.map(_.page).getOrElse(1)
}

/**
  * Apply paging
  *
  * @param count
  * @param page
  */
case class JSONQueryPaging(count:Int, page:Int=1)

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

  def apply(count:Int, page:Int, sort:List[JSONSort], filter:List[JSONQueryFilter]):JSONQuery =
    JSONQuery(paging = Some(JSONQueryPaging(count = count, page = page)), sort, filter)
  /**
    * Generic query
    */
  val baseQuery = JSONQuery(
    paging = None,
    sort = List(),
    filter = List()
  )

  def limit(limit:Int) = baseQuery.copy(paging= Some(JSONQueryPaging(count = limit)))
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
  final val EQUALS = "="
  final val NOT = "not"
  final val > = ">"
  final val < = "<"
  final val >= = ">="
  final val <= = "<="
  final val LIKE = "like"
  final val IN = "in"
  final val BETWEEN = "between"

  def options(`type`:String) = `type` match {
    case JSONFieldTypes.NUMBER => Seq(Filter.EQUALS, Filter.>, Filter.<, Filter.>=, Filter.<=, Filter.NOT, Filter.IN, Filter.BETWEEN)
    case JSONFieldTypes.STRING => Seq(Filter.LIKE, Filter.EQUALS, Filter.NOT)
    case _ => Seq(Filter.EQUALS, Filter.NOT)
  }

  def default(`type`:String) = `type` match {
    case JSONFieldTypes.NUMBER => Filter.EQUALS
    case JSONFieldTypes.STRING => Filter.LIKE
    case _ => Filter.EQUALS
  }
}

