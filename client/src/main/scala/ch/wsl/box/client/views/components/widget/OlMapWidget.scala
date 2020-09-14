package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.styles.Icons
import ch.wsl.box.client.styles.Icons.Icon
import ch.wsl.box.client.utils.GeoJson.{FeatureCollection, GeometryCollection}
import ch.wsl.box.client.utils.{BrowserConsole, ClientConf, Labels}
import ch.wsl.box.client.vendors.{DrawHole, DrawHoleOptions}
import ch.wsl.box.model.shared.{JSONField, JSONMetadata}
import io.circe.Json
import io.udash._
import org.scalablytyped.runtime.StringDictionary
import scalatags.JsDom
import scalatags.JsDom.all._
import scribe.Logging
import typings.ol._
import io.circe.scalajs._
import io.udash.bootstrap.utils.BootstrapStyles
import org.scalajs.dom.Event
import typings.geojson.mod.Geometry
import typings.ol.mod.Feature
import typings.ol.selectMod.SelectEvent
import io.circe._
import io.circe.syntax._

import scala.scalajs.js
import org.scalajs.dom._
import org.scalajs.dom.html.Div
import typings.ol.drawMod.DrawEvent
import typings.ol.modifyMod.ModifyEvent
import typings.ol.sourceVectorMod.VectorSourceEvent
import typings.ol.translateMod.TranslateEvent
import typings.ol.viewMod.FitOptions

import scala.concurrent.Future




case class OlMapWidget(id: Property[String], field: JSONField, prop: Property[Json]) extends Widget with Logging {

  import scalacss.ScalatagsCss._

  import io.udash.css.CssView._


  var map:mod.Map = null



  override def afterRender(): Unit = {
    if(map != null) {
      map.updateSize()
    }
    prop.touch()
  }

  def loadMap(mapDiv:Div) = {


    //typings error need to map it manually
    typings.proj4.mod.^.asInstanceOf[js.Dynamic].default.defs(
      "EPSG:21781",
      "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=600000 +y_0=200000 +ellps=bessel +towgs84=674.4,15.1,405.3,0,0,0,0 +units=m +no_defs"
    )

    typings.proj4.mod.^.asInstanceOf[js.Dynamic].default.defs(
      "EPSG:2056",
      "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs"
    )


    proj4Mod.register(typings.proj4.mod.^.asInstanceOf[js.Dynamic].default)


    val projectionLV03 = new projectionMod.default(projectionMod.Options("EPSG:21781")
        .setUnits("m")
        .setExtent(js.Tuple4(485071.54,75346.36,828515.78,299941.84))
    )

    val projectionLV95 = new projectionMod.default(projectionMod.Options("EPSG:2056")
      .setUnits("m")
      .setExtent(js.Tuple4(2485071.58, 1075346.31, 2828515.82, 1299941.79))
    )

    val raster = new layerMod.Tile(baseTileMod.Options().setSource(new sourceMod.OSM()))
    val swisstopo1000 = new imageMod.default(baseImageMod.Options()
        .setExtent(projectionLV03.getExtent())
        .setSource(new imageWMSMod.default(imageWMSMod.Options(
          url = "https://wms.geo.admin.ch/",
          params = StringDictionary(
            "LAYERS" -> "ch.swisstopo.pixelkarte-farbe-pk1000.noscale",
            "FORMAT" -> "image/jpeg"
          )
        )))
    )

    val vectorSource = new sourceMod.Vector[geometryMod.default](sourceVectorMod.Options())


    //red #ed1c24

    val simpleStyle = new styleMod.Style(styleStyleMod.Options()
      .setFill(new styleMod.Fill(fillMod.Options().setColor("rgb(237, 28, 36,0.2)")))
      .setStroke(new styleMod.Stroke(strokeMod.Options().setColor("#ed1c24").setWidth(2)))
      .setImage(
        new styleMod.Circle(styleCircleMod.Options(3)
          .setFill(
            new styleMod.Fill(fillMod.Options().setColor("rgba(237, 28, 36)"))
          )
        )
      )
    )

    val vectorStyle:js.Array[typings.ol.styleStyleMod.Style] = js.Array(
      simpleStyle,
      new styleMod.Style(styleStyleMod.Options()
        .setImage(
          new styleMod.Circle(styleCircleMod.Options(8)
            .setStroke(
              new styleMod.Stroke(strokeMod.Options().setColor("#ed1c24").setWidth(2))
            )
          )
        )
      )
    )

    val vector = new layerMod.Vector(baseVectorMod.Options()
      .setSource(vectorSource)
      .setStyle(vectorStyle)
    )

    val mousePosition = new mousePositionMod.default(mousePositionMod.Options()
        .setCoordinateFormat(coordinateMod.createStringXY())
        .setProjection(projectionLV03)
    )


    val controls = controlMod.defaults().extend(js.Array(mousePosition))//new controlMod.ScaleLine()))


    val view = new viewMod.default(viewMod.ViewOptions()
      .setZoom(3)
      .setProjection(projectionLV03)
      .setCenter(extentMod.getCenter(projectionLV03.getExtent()))
    )



    map = new mod.Map(pluggableMapMod.MapOptions()
      .setLayers(js.Array[baseMod.default](raster,vector))
      .setTarget(mapDiv)
      .setControls(controls.getArray())
      .setView(view)
    )

    BrowserConsole.log(map)
    BrowserConsole.log(mapDiv)

    var listener:Registration = null

    var onAddFeature:js.Function1[VectorSourceEvent[typings.ol.geometryMod.default], Unit] = null

    def registerListener(immediate:Boolean) = {
      listener = prop.listen({ geoData =>
        vectorSource.removeEventListener("addfeature",onAddFeature.asInstanceOf[eventsMod.Listener])
        vectorSource.getFeatures().foreach(f => vectorSource.removeFeature(f))

        if(!geoData.isNull) {
          val geom = new geoJSONMod.default().readFeature(convertJsonToJs(geoData).asInstanceOf[js.Object]).asInstanceOf[olFeatureMod.default[geometryMod.default]]
          vectorSource.addFeature(geom)
          view.fit(geom.getGeometry().getExtent(),FitOptions().setPaddingVarargs(150,50,50,150).setMinResolution(2))
        } else {
          view.fit(projectionLV03.getExtent())
        }

        vectorSource.on_addfeature(olStrings.addfeature,onAddFeature)
      }, immediate)
    }



    def changedFeatures() = {
      listener.cancel()
      val geoJson = new geoJSONMod.default().writeFeaturesObject(vectorSource.getFeatures())
      convertJsToJson(geoJson).flatMap(FeatureCollection.decode).foreach{ collection =>
        import ch.wsl.box.client.utils.GeoJson.Geometry._
        val geometries = collection.features.map(_.geometry)
        geometries.length match {
          case 0 => prop.set(Json.Null)
          case 1 => prop.set(geometries.head.asJson)
          case _ => prop.set(GeometryCollection(geometries).asJson)
        }
      }
      registerListener(false)

    }

    onAddFeature = (e:VectorSourceEvent[geometryMod.default]) => changedFeatures()

    registerListener(true)


    vectorSource.on_changefeature(olStrings.changefeature,(e:VectorSourceEvent[geometryMod.default]) => changedFeatures())


    val modify = new modifyMod.default(modifyMod.Options()
      .setSource(vectorSource)
      .setStyle(simpleStyle)
    )
    //modify.on_modifyend(olStrings.modifyend,(e:ModifyEvent) => changedFeatures())

    val drawPoint = new drawMod.default(drawMod.Options(geometryTypeMod.default.POINT)
      .setSource(vectorSource)
      .setStyle(vectorStyle)
    )
    //drawPoint.on_change(olStrings.change,e => changedFeatures())

    val drawLineString = new drawMod.default(drawMod.Options(geometryTypeMod.default.LINE_STRING)
      .setSource(vectorSource)
      .setStyle(simpleStyle)
    )
    //drawLineString.on_change(olStrings.change,e => changedFeatures())

    val drawPolygon = new drawMod.default(drawMod.Options(geometryTypeMod.default.POLYGON)
      .setSource(vectorSource)
      .setStyle(simpleStyle)
    )
    //drawPolygon.on_change(olStrings.change,e => changedFeatures())

    val drag = new translateMod.default(translateMod.Options())
    //drag.on_translateend(olStrings.translateend, (e:TranslateEvent) => changedFeatures())


    val snap = new snapMod.default(snapMod.Options().setSource(vectorSource))

    val delete = new selectMod.default(selectMod.Options())

    delete.on_select(olStrings.select,(e:SelectEvent) => {
      if(window.confirm(Labels.form.removeMap)) {
        e.selected.foreach(x => vectorSource.removeFeature(x))
        changedFeatures()
      }
    })

    val drawHole = new DrawHole(DrawHoleOptions().setStyle(simpleStyle))

    val dynamicInteraction = Seq(
      modify,
      drawPoint,
      drawLineString,
      drawPolygon,
      snap,
      drag,
      delete,
      drawHole
    )

    dynamicInteraction.foreach(x => {
      map.addInteraction(x)
      x.setActive(false)
    })

    activeControl.listen({section =>
      dynamicInteraction.foreach(x => x.setActive(false))

      section match {
        case Control.EDIT => {
          modify.setActive(true)
          snap.setActive(true)
        }
        case Control.POINT => {
          drawPoint.setActive(true)
          modify.setActive(true)
          snap.setActive(true)
        }
        case Control.LINESTRING => {
          drawLineString.setActive(true)
          modify.setActive(true)
          snap.setActive(true)
        }
        case Control.POLYGON => {
          drawPolygon.setActive(true)
          modify.setActive(true)
          snap.setActive(true)
        }
        case Control.POLYGON_HOLE => {
          drawHole.setActive(true)
          modify.setActive(true)
        }
        case Control.MOVE => {
          drag.setActive(true)
          snap.setActive(true)
        }
        case Control.DELETE => {
          delete.setActive(true)
          snap.setActive(true)
        }
        case _ => {}
      }

    }, true)


    (map,vectorSource)

  }

  override protected def show(): JsDom.all.Modifier = {

    val mapDiv: Div = div(height := 400).render

    loadMap(mapDiv)

    div(
      label(field.title),
      mapDiv
    )
  }

  object Control {

    sealed trait Section
    case object VIEW extends Section
    case object EDIT extends Section
    case object POINT extends Section
    case object LINESTRING extends Section
    case object POLYGON extends Section
    case object POLYGON_HOLE extends Section
    case object MOVE extends Section
    case object DELETE extends Section
  }
  val activeControl:Property[Control.Section] = Property(Control.VIEW)

  def controlButton(icon:Icon,title:String,section:Control.Section) = {
    produce(activeControl) { c =>
      val isActive = if(c == section) "active" else ""
      button(
        cls := isActive,
        BootstrapStyles.Button.btn,
        BootstrapStyles.Button.color()
      )(
       onclick :+= ((e:Event) => activeControl.set(section) )
      )(icon).render //modify
    }
  }

  override protected def edit(): JsDom.all.Modifier = {

    val mapDiv: Div = div(height := 400).render

    val (map,vectorSource) = loadMap(mapDiv)

    vectorSource.getExtent()

    div(
      label(field.title),
      div(
        BootstrapStyles.Button.group,
        BootstrapStyles.Button.groupSize(BootstrapStyles.Size.Small),
        ClientConf.style.controlButtons
      )(//controls
        controlButton(Icons.hand,"Pan & Zoom",Control.VIEW),
        controlButton(Icons.pencil,"Edit",Control.EDIT),
        controlButton(Icons.point,"Add Point",Control.POINT),
        controlButton(Icons.line,"Add Linestring",Control.LINESTRING),
        controlButton(Icons.polygon,"Add Polygon",Control.POLYGON),
        controlButton(Icons.hole,"Add Polygon",Control.POLYGON_HOLE),
        controlButton(Icons.move,"Move",Control.MOVE),
        controlButton(Icons.trash,"Delete",Control.DELETE),
        button(BootstrapStyles.Button.btn, BootstrapStyles.Button.color())(
          onclick :+= ((e:Event) => map.getView().fit(vectorSource.getExtent(),FitOptions().setPaddingVarargs(10,10,10,10).setMinResolution(0.5)) )
        )(Icons.search).render
      ),
      mapDiv
    )
  }
}

object OlMapWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = OlMapWidget(id,field,prop)
}
