//package ch.wsl.box.client.components.base
//
//
//import ch.wsl.box.client.utils.Log
//import japgolly.scalajs.react
//import japgolly.scalajs.react._
//import japgolly.scalajs.react.vdom.html_<^._
//
//import scala.scalajs.js
//import scala.scalajs.js.{Any, UndefOr, JSON}
//
///**
//  * Created by andreaminetti on 19/10/16.
//  */
//object Debug {
//
//    type DebugComponent = Build[Unit,Option[js.Any],Backend,react.TopNode]
//
//
//
//    class Backend($: BackendScope[Unit, Option[js.Any]]) {
//
//      def change(data:Option[js.Any]):Unit = {
//        println("change something")
//        $.setState(data).runNow()
//      }
//
//      def render(S:Option[js.Any]) =
//        <.div(
//          <.h3("Debug"),
//          <.div(JSON.stringify(S.getOrElse(js.Object())))
//        )
//
//    }
//
//    val component = ReactComponentB[Unit]("DebugComponent")
//      .initialState(None:Option[js.Any])
//      .backend(new Backend(_))
//      .renderS{ case (cb,s) =>
//        cb.backend.render(s)
//      }
//      .build
//
//
//
//
//    def apply(ref: UndefOr[String] = "", key: Any = {}) = component.set(key, ref)()
//
//
//  }
