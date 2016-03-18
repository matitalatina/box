package ch.wsl.box.client.configs

/**
  * Created by andreaminetti on 23/02/16.
  */
object Path {

  val models = Config.endpoint + "models"

  def forModel(model:String) = new ModelPathBuilder(model)

  case class ModelPathBuilder(model:String) {
    def list = Config.endpoint + model + "/list"
    def schema = Config.endpoint + model + "/schema"
    def form = Config.endpoint + model + "/form"
    def keys = Config.endpoint + model + "/keys"
    def count = Config.endpoint + model + "/count"
    def get(i:String) = Config.endpoint + model + "/" + i
    def update(i:String) = Config.endpoint + model + "/" + i
    def insert = Config.endpoint + model
    def firsts = Config.endpoint + model
  }

  def forForms(model:String) = ???

  case class FormPathBuilder(model:String) {
    def list = Config.endpoint + "forms/" + model
  }

}
