package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST

import scala.util.Try

/**
  * Created by andre on 6/8/2017.
  */
object Labels {

  import ch.wsl.box.client.Context._

//  def langs = Seq("it","de","fr","en")

  private var labels:Map[String,String] = Map()

  def load(lang:String) = REST.labels(lang).map{ table =>
    labels = table
  }

  def apply(key:String):String = get(key)

  private def get(key:String):String = labels.lift(key).getOrElse(key)

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
    def recordFound = get("navigation.recordFound")
    def goAway = get("navigation.goAway")
    def first = get("navigation.first")
    def last = get("navigation.last")
    def next = get("navigation.next")
    def previous = get("navigation.previous")
    def firstPage = get("navigation.first")
    def lastPage = get("navigation.last")
    def nextPage = get("navigation.next")
    def previousPage = get("navigation.previous")
    def loading = get("navigation.loading")
    def page = get("navigation.page")
    def record = get("navigation.record")
    def of = get("navigation.of")
  }

  object sort{
    def asc = get("sort.asc")
    def desc = get("sort.desc")
  }

  object form{
    def required = get("form.required")
    def save = get("form.save")
    def save_add = get("form.save_add")
    def save_table = get("form.save_table")
    def addDate = get("form.add_date")
    def removeDate = get("form.remove_date")
    def changed = get("form.changed")
  }

  object entities{
    def search = get("entity.search")
    def title = get("entity.title")
    def select = get("entity.select")
    def `new` = get("entity.new")
    def table = get("entity.table")
  }

  object exports{
    def search = get("exports.search")
    def title = get("exports.title")
    def select = get("exports.select")
  }

  object entity{
    def actions = get("table.actions")
    def show = get("table.show")
    def edit = get("table.edit")
    def no_action = get("table.no_action")
    def delete = get("table.delete")
    def confirmDelete = get("table.confirmDelete")
  }

  object header{
    def home = get("header.home")
    def entities = get("header.entities")
    def forms = get("header.forms")
    def lang = get("header.lang")
  }
}
