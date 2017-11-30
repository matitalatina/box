package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST

import scala.util.Try
import scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
  * Created by andre on 6/8/2017.
  */
object Labels {

  def langs = Seq("it","de","fr","en")

  private var labels:Map[String,String] = Map()
  def load(lang:String) = REST.labels(lang).map{ table =>
    labels = table
  }

  def apply(key:String):String = get(key)
  private def get(key:String) = labels.lift(key).getOrElse(key)

  object messages {
    def confirm = get("messages.confirm")
  }

  object subform {
    def remove = get("subform.remove")
    def add = get("subform.add")
  }

  object error{
    def notfound = get("error.notfound")
  }

  object login{
    def failed = get("login.failed")
    def title = get("login.title")
    def username = get("login.username")
    def password = get("login.password")
    def button = get("login.button")
    def choseLang = get("login.chose_lang")
  }

  object navigation{
    def next = get("navigation.next")
    def previous = get("navigation.previous")
    def loading = get("navigation.loading")
  }

  object form{
    def save = get("form.save")
    def save_add = get("form.save_add")
    def save_table = get("form.save_table")
    def addDate = get("form.add_date")
    def removeDate = get("form.remove_date")
  }

  object models{
    def search = get("model.search")
    def title = get("model.title")
    def select = get("model.select")
    def `new` = get("model.new")
    def table = get("model.table")
  }

  object table{
    def actions = get("table.actions")
    def edit = get("table.edit")
  }

  object header{
    def home = get("header.home")
    def models = get("header.models")
    def forms = get("header.forms")
    def lang = get("header.lang")
  }
}
