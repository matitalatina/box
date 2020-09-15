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
import io.circe.generic.auto._

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

  /*
  {
    "features": {
        "point":  true,
        "line": false,
        "polygon": true
    },
    "multiGeometry": false,
    "projection": {
        "name": "EPSG:21781",
        "proj": "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=600000 +y_0=200000 +ellps=bessel +towgs84=674.4,15.1,405.3,0,0,0,0 +units=m +no_defs",
        "extent": [485071.54,75346.36,828515.78,299941.84]
    }
}
   */
  case class MapParamsFeatures(
                              point: Boolean,
                              multiPoint: Boolean,
                              line: Boolean,
                              multiLine:Boolean,
                              polygon: Boolean,
                              multiPolygon: Boolean,
                              geometryCollection: Boolean
                              )

  case class MapParamsProjection(
                                name:String,
                                proj:String,
                                extent: Seq[Double],
                                unit: String
                                )

  case class MapParams(
                        features: MapParamsFeatures,
                        defaultProjection: String,
                        projections: Seq[MapParamsProjection],
                      )

  val defaultParams = MapParams(
    features = MapParamsFeatures(true,true,true,true,true,true,true),
    defaultProjection = "EPSG:3857",
    projections = Seq(MapParamsProjection(
      name = "EPSG:3857",
      proj = "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs",
      extent = Seq(-20026376.39, -20048966.10, 20026376.39, 20048966.10),
      unit = "m"
    ))
  )

  val jsonOption:Json = ClientConf.mapOptions.deepMerge(field.params.getOrElse(JsonObject().asJson))
  val options:MapParams = jsonOption.as[MapParams] match {
    case Right(value) => value
    case Left(error) => {
      logger.warn(s"Using default params - ${error} on json: $jsonOption")
      defaultParams
    }
  }

  logger.info(s"$options")

  def loadMap(mapDiv:Div) = {

    options.projections.map { projection =>
      //typings error need to map it manually
      typings.proj4.mod.^.asInstanceOf[js.Dynamic].default.defs(
        projection.name,
        projection.proj
      )
    }

    proj4Mod.register(typings.proj4.mod.^.asInstanceOf[js.Dynamic].default)

    val projections = options.projections.map { projection =>
      projection.name -> new projectionMod.default(projectionMod.Options(projection.name)
        .setUnits(projection.unit)
        .setExtent(js.Tuple4(
          projection.extent.lift(0).getOrElse(0),
          projection.extent.lift(1).getOrElse(0),
          projection.extent.lift(2).getOrElse(0),
          projection.extent.lift(3).getOrElse(0),
        ))
      )

    }.toMap

    val defaultProjection = projections(options.defaultProjection)



    val raster = new layerMod.Tile(baseTileMod.Options().setSource(new sourceMod.OSM()))
    val swisstopo1000 = new imageMod.default(baseImageMod.Options()
        .setExtent(defaultProjection.getExtent())
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
        .setProjection(defaultProjection)
    )


    val controls = controlMod.defaults().extend(js.Array(mousePosition))//new controlMod.ScaleLine()))


    val view = new viewMod.default(viewMod.ViewOptions()
      .setZoom(3)
      .setProjection(defaultProjection)
      .setCenter(extentMod.getCenter(defaultProjection.getExtent()))
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
          view.fit(defaultProjection.getExtent())
        }

        vectorSource.on_addfeature(olStrings.addfeature,onAddFeature)
      }, immediate)
    }



    def changedFeatures() = {
      listener.cancel()
      val geoJson = new geoJSONMod.default().writeFeaturesObject(vectorSource.getFeatures())
      convertJsToJson(geoJson).flatMap(FeatureCollection.decode).foreach{ collection =>
        import ch.wsl.box.client.utils.GeoJson._
        import ch.wsl.box.client.utils.GeoJson.Geometry._
        val geometries = collection.features.map(_.geometry)
        logger.info(s"$geometries")
        geometries.length match {
          case 0 => prop.set(Json.Null)
          case 1 => prop.set(geometries.head.asJson)
          case _ => {
            val multiPoint = geometries.map{
              case g:Point => Some(Seq(g.coordinates))
              case g:MultiPoint => Some(g.coordinates)
              case _ => None
            }
            val multiLine = geometries.map{
              case g:LineString => Some(Seq(g.coordinates))
              case g:MultiLineString => Some(g.coordinates)
              case _ => None
            }
            val multiPolygon = geometries.map{
              case g:Polygon => Some(Seq(g.coordinates))
              case g:MultiPolygon => Some(g.coordinates)
              case _ => None
            }

            val collection:Option[ch.wsl.box.client.utils.GeoJson.Geometry] = if(multiPoint.forall(_.isDefined) && options.features.multiPoint) {
              Some(MultiPoint(multiPoint.flatMap(_.get)))
            } else if(multiLine.forall(_.isDefined) && options.features.multiLine) {
              Some(MultiLineString(multiLine.flatMap(_.get)))
            } else if(multiPolygon.forall(_.isDefined) && options.features.multiPolygon) {
              Some(MultiPolygon(multiPolygon.flatMap(_.get)))
            } else if(options.features.geometryCollection){
              Some(GeometryCollection(geometries))
            }else {
              None
            }
            prop.set(collection.asJson)


          }
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

    div(
      label(field.title),
      produce(prop) { geo =>
        import ch.wsl.box.client.utils.GeoJson.Geometry._
        import ch.wsl.box.client.utils.GeoJson._
        val geometry = geo.as[ch.wsl.box.client.utils.GeoJson.Geometry].toOption

        val enablePoint = {
          options.features.point && geometry.isEmpty ||
          options.features.multiPoint && geometry.forall{
            case g: Point => true
            case g: MultiPoint => true
            case _ => false
          } ||
          options.features.geometryCollection
        }

        val enableLine = {
          options.features.line && geometry.isEmpty ||
          options.features.multiLine && geometry.forall{
            case g: LineString => true
            case g: MultiLineString => true
            case _ => false
          } ||
          options.features.geometryCollection
        }

        val enablePolygon = {
          options.features.polygon && geometry.isEmpty ||
          options.features.multiPolygon && geometry.forall{
            case g: Polygon => true
            case g: MultiPolygon => true
            case _ => false
          } ||
          options.features.geometryCollection
        }

        val enablePolygonHole = geometry.exists{
              case g: Polygon => true
              case g: MultiPolygon => true
              case _ => false
          }

        if(!enablePoint && activeControl.get == Control.POINT) activeControl.set(Control.VIEW)
        if(!enableLine && activeControl.get == Control.LINESTRING) activeControl.set(Control.VIEW)
        if(!enablePolygon && Seq(Control.POLYGON,Control.POLYGON_HOLE).contains(activeControl.get)) activeControl.set(Control.VIEW)
        if(!enablePolygonHole && Seq(Control.POLYGON_HOLE).contains(activeControl.get)) activeControl.set(Control.VIEW)
        if(geometry.isEmpty && Seq(Control.EDIT,Control.MOVE,Control.DELETE).contains(activeControl.get)) activeControl.set(Control.VIEW)

        div(
          BootstrapStyles.Button.group,
          BootstrapStyles.Button.groupSize(BootstrapStyles.Size.Small),
          ClientConf.style.controlButtons
        )( //controls
          controlButton(Icons.hand, "Pan & Zoom", Control.VIEW),
          if (geometry.isDefined) controlButton(Icons.pencil, "Edit", Control.EDIT) else frag(),
          if (enablePoint) controlButton(Icons.point, "Add Point", Control.POINT) else frag(),
          if (enableLine) controlButton(Icons.line, "Add Linestring", Control.LINESTRING) else frag(),
          if (enablePolygon) controlButton(Icons.polygon, "Add Polygon", Control.POLYGON) else frag(),
          if (enablePolygonHole) controlButton(Icons.hole, "Add Polygon", Control.POLYGON_HOLE) else frag(),
          if (geometry.isDefined) controlButton(Icons.move, "Move", Control.MOVE) else frag(),
          if (geometry.isDefined) controlButton(Icons.trash, "Delete", Control.DELETE) else frag(),
          if (geometry.isDefined) button(BootstrapStyles.Button.btn, BootstrapStyles.Button.color())(
            onclick :+= ((e: Event) => map.getView().fit(vectorSource.getExtent(), FitOptions().setPaddingVarargs(10, 10, 10, 10).setMinResolution(0.5)))
          )(Icons.search).render else frag()
        ).render
      },
      mapDiv
    )
  }
}

object OlMapWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = OlMapWidget(id,field,prop)
}
