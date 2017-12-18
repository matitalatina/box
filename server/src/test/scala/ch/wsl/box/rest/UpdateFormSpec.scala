package ch.wsl.box.rest

import ch.wsl.box.rest.logic.{FormActions, JSONFormMetadataFactory}
import io.circe.Json
import ch.wsl.box.model.tables._
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class UpdateFormSpec extends FlatSpec with ScalaFutures {

  val jsonString =
    """
      |{
      |  "data_from_history" : null,
      |  "d_pinus" : null,
      |  "altitude" : 2260,
      |  "x_coord" : 636840,
      |  "start_date" : "2016-12-29 18:58:00.0",
      |  "alarm_forestdep" : "2016-12-29 18:58:00.0",
      |  "s_coppice_mixed" : 0,
      |  "damage_forest_id" : 2,
      |  "x_bush" : null,
      |  "fire_crown" : null,
      |  "site_id" : 2,
      |  "height_damage" : 0.9,
      |  "d_quercus" : null,
      |  "y_coord" : 118350,
      |  "checked" : 1,
      |  "a_total" : 0.01,
      |  "d_other_hardwood" : null,
      |  "s_high_forest_mixed" : 0,
      |  "a_nonproductive" : 0,
      |  "litter_id" : 2,
      |  "x_selva" : null,
      |  "x_forestation" : null,
      |  "s_forestation" : 0,
      |  "x_coppice" : null,
      |  "x_coppice_mixed" : null,
      |  "d_larix" : 1,
      |  "damage_soil_id" : 5,
      |  "fire_surface" : 1,
      |  "a_grassland" : 0,
      |  "a_forest" : 0.01,
      |  "fire_municipality_touched" : [
      |  ],
      |  "data_from_firemandep" : null,
      |  "f_recreation" : null,
      |  "bush_layer_id" : 1,
      |  "definition_id" : 1,
      |  "herb_layer_id" : 1,
      |  "s_selva" : 0,
      |  "remarks_starting_point" : [
      |    {
      |      "remark" : "starting point",
      |      "fire_id" : 201612292301,
      |      "remark_type_id" : 1
      |    }
      |  ],
      |  "x_high_forest_softwood" : 1,
      |  "d_castanea" : null,
      |  "fire_single_tree" : null,
      |  "x_high_forest_hardwood" : null,
      |  "cause_reliability_id" : null,
      |  "remark_data_from" : [
      |  ],
      |  "end_date" : "2016-12-30 16:00:00.0",
      |  "expo_id" : 2,
      |  "f_economic" : null,
      |  "data_from_forestdep" : 1,
      |  "fire_municipality_start" : [
      |    {
      |      "municipality_type_id" : 1,
      |      "municipality_id" : 10574,
      |      "fire_id" : 201612292301
      |    }
      |  ],
      |  "s_pioneer" : 0,
      |  "d_picea" : null,
      |  "d_fagus" : null,
      |  "s_coppice" : 0,
      |  "coord_reliability_id" : 4,
      |  "diameter" : 30,
      |  "s_high_forest_hardwood" : 0,
      |  "d_other_softwood" : null,
      |  "fire_subsurface" : null,
      |  "other_cause" : null,
      |  "end_date_reliability_id" : 1,
      |  "fire_id" : 201612292301,
      |  "locality" : "bei Sänntum",
      |  "d_betula" : null,
      |  "start_date_reliability_id" : 1,
      |  "remark_notes" : [
      |  ],
      |  "s_high_forest_softwood" : 0.01,
      |  "s_bush" : 0,
      |  "x_pioneer" : null,
      |  "f_protection" : 1,
      |  "remark_cause" : [
      |  ],
      |  "slope" : 80,
      |  "cause_id" : 1,
      |  "x_high_forest_mixed" : null
      |}
    """.stripMargin.trim

  import io.circe._, io.circe.parser._

  val json = parse(jsonString).right.get

  import profile.api._

  implicit val db = Database.forURL("jdbc:postgresql:swissfire", "postgres", "postgres", driver="org.postgresql.Driver")



  "The service" should "query update nested subforms" in {

    val remarkQuery = Remark.filter(_.fire_id === 201612292301L)

    whenReady(for{
      form <- JSONFormMetadataFactory().of("fire","it")
      shaper = FormActions(form)
      i <- shaper.updateAll(json)
      remarks <- db.run(remarkQuery.result)
    } yield remarks, timeout(100000 seconds)){ remarks =>
      assert(remarks.length > 0)
    }

  }

}
