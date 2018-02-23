package ch.wsl.box.model.shared

case class IDs(isLastPage:Boolean,
               currentPage:Int,
               ids:Seq[String],
               count:Int    //stores the number of rows resulting from the query without paging
              )