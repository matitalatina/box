package postgresweb.controllers

import ch.wsl.jsonmodels.{JSONQuery, Table, JSONSchemaUI}
import japgolly.scalajs.react.{CallbackTo, Callback, ReactElement}
import postgresweb.models.Menu
import postgresweb.services.ModelClient
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future
import scala.scalajs.js

/**
  * Created by andreaminetti on 14/03/16.
  */
class TableController extends CRUDController{



  def client = ModelClient(container.model)

  override def load(jq: JSONQuery): Future[Table] = client.Helpers.filter2table(jq)

  override def get(id: String): Future[js.Any] = client.get(id)

  override def schemaAsString: Future[String] = client.schema

  override def uiSchema: Future[JSONSchemaUI] = client.form.map(JSONSchemaUI.fromJSONFields)

  override def get: Future[js.Any] = client.get(id)

  override def onUpdate(data: js.Any): Callback = CallbackTo(client.update(id,data))

  override def onInsert(data: js.Any): Callback = CallbackTo(client.insert(data))


}
