package ch.wsl.box.model.shared

/**
  * Created by andre on 5/16/2017.
  */


case class Layout(blocks: Seq[LayoutBlock])

/**
  *
  * @param title title of the block
  * @param width in bootstrap cols
  * @param fields list of field to display in that block, with format <table>.<field>
  */
case class LayoutBlock(
                   title: Option[String],
                   width:Int,
                   fields:Seq[String]
                 )