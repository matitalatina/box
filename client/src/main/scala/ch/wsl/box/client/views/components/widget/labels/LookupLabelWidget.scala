package ch.wsl.box.client.views.components.widget.labels

import ch.wsl.box.client.views.components.widget.{ComponentWidgetFactory, Widget, WidgetParams, WidgetRegistry}
import ch.wsl.box.model.shared.{EntityKind, JSONField, JSONID, JSONMetadata, WidgetsNames}
import ch.wsl.box.shared.utils.JSONUtils.EnhancedJson
import io.circe.Json
import io.udash._
import io.udash.properties.single.Property
import scalatags.JsDom
import scalatags.JsDom.all._


object LookupLabelWidget extends ComponentWidgetFactory {

  override def name: String = WidgetsNames.lookupLabel


  override def create(params: WidgetParams): Widget = LookupLabelImpl(params)

  case class LookupLabelImpl(params: WidgetParams) extends Widget {


    import ch.wsl.box.client.Context._

    override def field: JSONField = params.field

    val lookupLabel = params.field.lookupLabel.get

    val monitoredFields:Property[Seq[(String,Json)]] = Property(Seq())

    monitoredFields.listen{localFields =>
      if(localFields.exists( x => x._2 != Json.Null)) {
        services.rest.get(
          EntityKind.ENTITY.kind,
          services.clientSession.lang(),
          lookupLabel.remoteEntity,
          JSONID.fromMap(localFields)
        ).map{ remote =>
          remoteField.set(remote.js(lookupLabel.remoteField))
        }
      } else {
        remoteField.set(Json.Null)
      }
    }

    val remoteField:Property[Json] = Property(Json.Null)

    params.allData.listen({js =>
      val ids:Seq[(String,Json)] = lookupLabel.localIds.zip(lookupLabel.remoteIds).map{ case (localId,remoteId) =>
        remoteId -> js.js(localId)
      }
      monitoredFields.set(ids)
    },true)

    override protected def show(): JsDom.all.Modifier = {
      div(
        WidgetRegistry
        .forName(lookupLabel.widget)
        .create(params.copy(prop = remoteField))
        .render(false,Property(true))
      )
    }



    override protected def edit(): JsDom.all.Modifier = show()
  }
}