package ch.wsl.box.client.services

import ch.wsl.box.client.{Context, MainModule, RoutingState}
import io.udash.{State, Url}
import org.scalajs.dom.{BeforeUnloadEvent, window}
import scribe.Logging


object Navigate extends Logging with MainModule {
  private var enabled:Boolean = true
  private var enabler:() => Boolean = () => false

  def disable(enabler: () => Boolean = () => false) = {
    this.enabler = enabler
    enabled = false
  }
  def enable() = {enabled = true }

  def to(state:RoutingState) = toAction{ () =>
    logger.debug(s"navigate to $state")
    context.applicationInstance.goTo(state)
  }

  def toUrl(url:String) = toAction{ () =>
    logger.debug(s"navigate to $url")
    val state = context.routingRegistry.matchUrl(Url(url))
    context.applicationInstance.goTo(state)
  }

  def toAction(action: () => Unit) = {
    if(enabled) {
      action()
    } else if(enabler()) {
      enabled = true
      window.onbeforeunload = { (e:BeforeUnloadEvent) => } //is a new page i don't want to block the user anymore
      action()
    }
  }



  import scalatags.JsDom.all._
  import org.scalajs.dom.Event
  import io.udash._


  def event(state:RoutingState) = (e:Event) => to(state)

  def click(state:RoutingState) = onclick :+= event(state)

  def click(url:String) = onclick :+= ((e:Event) => toUrl(url))
}
