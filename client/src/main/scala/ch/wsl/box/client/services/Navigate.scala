package ch.wsl.box.client.services

import org.scalajs.dom.{BeforeUnloadEvent, window}


object Navigate {
  private var enabled:Boolean = true
  private var enabler:() => Boolean = () => false

  def disable(enabler: () => Boolean = () => false) = {
    this.enabler = enabler
    enabled = false
  }
  def enable() = {enabled = true }

  def to(url:String) = toAction{ () => io.udash.routing.WindowUrlChangeProvider.changeUrl(url) }

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


  def event(url:String) = (e:Event) => to(url)

  def click(url:String) = onclick :+= event(url)
}
