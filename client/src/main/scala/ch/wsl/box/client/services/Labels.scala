package ch.wsl.box.client.services

import ch.wsl.box.model.shared.SharedLabels

/**
  * Created by andre on 6/8/2017.
  */
object Labels {

//  def langs = Seq("it","de","fr","en")

  private var labels:Map[String,String] = Map()

  def load(table:Map[String,String]) = {
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
    def session_expired = get("error.session_expired")
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
    def save = get(SharedLabels.form.save)
    def save_add = get(SharedLabels.form.save_add)
    def save_table = get(SharedLabels.form.save_table)
    def addDate = get("form.add_date")
    def removeDate = get("form.remove_date")
    def changed = get("form.changed")
    def removeMap = get("form.remove-map")
  }

  object lookup{
    def not_found = get("lookup.not_found")
  }

  object entities{
    def search = get("entity.search")
    def title = get("entity.title")
    def select = get("entity.select")
    def `new` = get(SharedLabels.entities.`new`)
    def table = get(SharedLabels.entities.table)
    def duplicate = get(SharedLabels.entities.duplicate)
  }

  object exports{
    def search = get("exports.search")
    def title = get("exports.title")
    def select = get("exports.select")
    def load = get("exports.load")
    def csv = get ("exports.csv")
    def pdf = get ("exports.pdf")
    def html = get ("exports.html")
    def shp = get ("exports.shp")
  }

  object entity{
    def filters = get("table.filters")
    def actions = get("table.actions")
    def show = get("table.show")
    def edit = get("table.edit")
    def no_action = get("table.no_action")
    def delete = get(SharedLabels.entity.delete)
    def revert = get(SharedLabels.entity.revert)
    def confirmDelete = get(SharedLabels.entity.confirmDelete)
    def confirmRevert = get(SharedLabels.entity.confirmRevert)
    def csv = get ("table.csv")
    def xls = get ("table.xls")
  }

  object header{
    def home = get("header.home")
    def entities = get("header.entities")
    def tables = get("header.tables")
    def views = get("header.views")
    def forms = get("header.forms")
    def exports = get("header.exports")
    def functions = get("header.functions")
    def lang = get(SharedLabels.header.lang)
  }

  object popup{
    def search = get("popup.search")
    def close = get("popup.close")
  }
}
