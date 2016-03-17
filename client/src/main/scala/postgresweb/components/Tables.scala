package postgresweb.components

import ch.wsl.model.shared._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import postgresweb.controllers.{CRUDController, Controller}
import postgresweb.css.CommonStyles

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalacss.Defaults._
import scalacss.ScalaCssReact._

object Tables{
  object Style extends StyleSheet.Inline {

    import dsl._
    val table = style(addClassNames("mdl-data-table","mdl-js-data-table","mdl-shadow--2dp"))
    val td = style(addClassNames("mdl-data-table__cell--non-numeric"))

    val select = style(addClassName("select-wrap"))
    val input  = style(
      addClassName("mdl-textfield__input"),
      width(100.px),
      fontSize(11.px),
      display.inherit,
      lineHeight(15.px)
    )

    val selected = styleF.bool( selected => styleS(
      mixinIf(selected)(
        backgroundColor(c"#C5CAE9")
      )
    )
    )
  }
}

case class Tables(controller: CRUDController) {




  case class State(table:Table,page:Int,selectedRow:Vector[(String,String)])

  class Backend(scope:BackendScope[Unit,State]) {




    def load():Callback = Callback.future{
      controller.table.map{ table =>
          scope.modState(_.copy(table = table))
      }
    }

    load().runNow()



    /**
      * When a row is selected set the global state with the id of the selected row
      * and set the component state
      *
      * @param headers table headers
      * @param row selected row
      * @return A callback that do the action
      */
    def selectRow(headers: Vector[String], row: Vector[String]):Callback = {
      controller.selectId(row.headOption.getOrElse("")) //TODO fix id get
      scope.modState(_.copy(selectedRow = headers.zip(row)))
    }

    /**
      * Helpler method to generate the options for the filter field according to the type of the field
      *
      * @param `type` type of the field (you can use JSONSchema::typeOfTitle to retrive it
      * @return A list of options according to the type
      */
    def filterOption(`type`:String):Seq[TagMod] = `type` match {
      case "string" => Seq(
        <.option(^.value := "=", ^.selected := true,"="),
        <.option(^.value := "like","Like")
      )
      case "number" => Seq(
        <.option(^.value := "=", ^.selected := true,"="),
        <.option(^.value := "<","<"),
        <.option(^.value := ">",">"),
        <.option(^.value := "not","not")
      )
      case _ => {
        println("Type not found: " + `type`)
        Seq(
          <.option(^.value := "=", "=")
        )
      }
    }

    //def refresh() = Callback.future(controller.table)


    def modOperator(s:State, field:String)(e: ReactEventI):Callback = {
      val operator = e.target.value
      println(operator)
      val value = controller.query.filter.find(_.column == field).map(_.value).getOrElse("")
      val newFilter =  JSONQueryFilter(field,Some(operator),value) :: controller.query.filter
      val newQuery = controller.query.copy(filter = newFilter)
      controller.setQuery(newQuery)

      Callback.log("State operator for " + field + "changed") >>
      CallbackTo(controller.table)

    }

    def modFilter(s:State, field:String)(e: ReactEventI):Callback = {
      val value = e.target.value
      val operator:Option[String] = controller.query.filter.find(_.column == field).flatMap(_.operator)
      val newFilter = if(value.size > 0) {
        JSONQueryFilter(field,operator,value) :: controller.query.filter
      } else {
        println("Remove filter to field" + field)
        controller.query.filter.filterNot(_.column == field)
      }
      val newQuery = controller.query.copy(filter = newFilter)

      println(newQuery)

      controller.setQuery(newQuery)

      Callback.log("State filter for " + field + "changed") >>
        CallbackTo(controller.table)
    }



    def render(S:State) = {
      import Tables._
      <.div(CommonStyles.row,
        <.div(CommonStyles.fullWidth,
          <.div(CommonStyles.scroll,
            <.table(Style.table,
              <.thead(
                <.tr(
                  S.table.headers.map(title => <.th(Style.td,title))
                )
              ),
              <.tbody(
                <.tr(
                  S.table.headers.map(title => <.td(Style.td,
                    <.input(Style.input,^.onChange ==> modFilter(S,title)), //TODO should not be the title here but the key
                    <.span(Style.select,
                      <.select(
                        ^.onChange ==> modOperator(S,title), //TODO should not be the title here but the key
                        filterOption(S.table.schema.typeOfTitle(title))
                      )
                    )
                  ))
                ),
                S.table.rows.map{row =>
                  <.tr( Style.selected(row == S.selectedRow.map(_._2)),
                    ^.onClick --> selectRow(S.table.headers,row),
                    row.map{ cell =>
                      <.td(Style.td,cell.toString)
                    }
                  )
                }
              )
            )
          )
        )
      )
    }
  }



  val component = ReactComponentB[Unit]("ItemsInfo")
    .initialState(State(Table.empty,1,Vector()))
    .renderBackend[Backend]
    .buildU

  def apply() = component()
}
