package ch.wsl.jsonmodels

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
  * @param column
  * @param operator
  * @param value
  */
case class JSONQueryFilter(column:String, operator:Option[String], value:String)

/**
  * Sort data by column
  * @param column
  * @param order valid values are asc/desc
  */
case class JSONSort(column:String,order:String)

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
}