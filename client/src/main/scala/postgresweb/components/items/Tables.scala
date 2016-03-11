package postgresweb.components.items


import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import postgresweb.css.CommonStyles
import postgresweb.models.{JSONQueryFilter, Table, JSONSchema, JSONQuery}
import postgresweb.services.{GlobalState, ModelClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

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

case class Tables(model:String) {




  case class State(table:Table,page:Int,selectedRow:Vector[(String,String)],query:JSONQuery)

  class Backend(scope:BackendScope[Unit,State]) {



    val client = ModelClient(model)

    def load(jq: JSONQuery):Future[Table] = {
      println("load")
      client.Helpers.filter2table(jq).map { table =>
          println("laoded")
          scope.modState(_.copy(table = table)).runNow()
          table
      }
    }

    load(JSONQuery.baseFilter)


    /**
      * When a row is selected set the global state with the id of the selected row
      * and set the component state
      *
      * @param headers table headers
      * @param row selected row
      * @return A callback that do the action
      */
    def selectRow(headers: Vector[String], row: Vector[String]):Callback = {
      GlobalState.selectedId = row.headOption
      for{
        _ <- Callback.log("Selected row: " + row)
        result <- scope.modState(_.copy(selectedRow = headers.zip(row)))
      } yield result
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

    def refresh() = for{
      state <- scope.state
      table <- {
        println("refresh:" + state.query)
        Callback(load(state.query))
      }
    } yield table

    def modOperator(s:State, field:String)(e: ReactEventI):Callback = {
      val operator = e.target.value
      println(operator)
      val value = s.query.filter.lift(field).map(_.value).getOrElse("")
      val newFilter = s.query.filter + (field -> JSONQueryFilter(value,Some(operator)))
      val newQuery = s.query.copy(filter = newFilter)
      scope.modState(_.copy(query = newQuery)) >>
        Callback.log("State operator for " + field + "changed") >>
        Callback(load(newQuery))
    }

    def modFilter(s:State, field:String)(e: ReactEventI):Callback = {
      val value = e.target.value
      val operator:Option[String] = s.query.filter.lift(field).flatMap(_.operator)
      val newFilter = if(value.size > 0) {
        s.query.filter + (field -> JSONQueryFilter(value,operator))
      } else {
        println("Remove filter to field" + field)
        s.query.filter - field
      }
      val newQuery = s.query.copy(filter = newFilter)

      println(newQuery)
      scope.modState(_.copy(query = newQuery)) >>
        Callback.log("State filter for " + field + "changed") >>
        Callback(load(newQuery))
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
    .initialState(State(Table.empty,1,Vector(),JSONQuery.baseFilter))
    .renderBackend[Backend]
    .buildU

  def apply() = component()
}
