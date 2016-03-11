package postgresweb.configs

/**
  * Created by andreaminetti on 23/02/16.
  */
object Path {


  val models = Config.endpoint + "models"

  def forModel(model:String) = new PathBuilder(model)

  case class PathBuilder(model:String) {
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

}
