//package ch.wsl.box.client.libraries
//
///**
//  * Created by andre on 3/13/2017.
//  */
//import japgolly.scalajs.react.JsComponentC
//import scalajs.js
//
///**
//  * Component-wrapper for
//  *
//  * https://github.com/JedWatson/react-select
//  */
//object ReactSelect {
//
//  @js.native
//  trait Props extends js.Object {
//    var isOpened: Boolean = js.native
//  }
//
//  def props(isOpened: Boolean): Props = {
//    val p = (new js.Object).asInstanceOf[Props]
//    p.isOpened = isOpened
//    p
//  }
//
//  val component = JsComponent[Props, Children.Varargs, Null]("ReactSelect")
//}
