package ch.wsl.rest.domain

/**
 * Created by andreaminetti on 24/04/15.
 */
case class JSONQuery(
                      count:Int,
                      page:Int,
                      sorting:Map[String,String],
                      filter:Map[String,JSONQueryFilter]//,
//                      group:Map[String,String],
//                      groupBy:Option[String]
                      ) {
  
  def query = ???
  
}
                      
                      
case class JSONQueryFilter(value:String,operator:Option[String]) {
  
  def filter = ???
  
}
