package ch.wsl.box.client.views.components.widget.lookup

import ch.wsl.box.client.services.Labels
import ch.wsl.box.client.views.components.widget.{HasData, Widget}
import ch.wsl.box.model.shared.{JSONField, JSONLookup}
import ch.wsl.box.shared.utils.JSONUtils.EnhancedJson
import io.circe._
import io.circe.syntax._
import io.udash.{SeqProperty, bind}
import io.udash.properties.single.Property
import scalatags.JsDom

trait LookupWidget extends Widget with HasData {

  import ch.wsl.box.client.Context._

  def allData:Property[Json]

  def field:JSONField
  val lookup:SeqProperty[JSONLookup] = {
    SeqProperty(toSeq(field.lookup.toSeq.flatMap(_.lookup)))
  }

  val model:Property[JSONLookup] = Property(JSONLookup("",""))

  autoRelease(field.`type` match {
    case "number" =>  data.sync[JSONLookup](model)(
      {json:Json =>
        logger.debug(json.toString())
        val id = jsonToString(json)
        lookup.get.find(_.id == jsonToString(json)).getOrElse(JSONLookup(id,id + " NOT FOUND"))
      },
      {jsonLookup:JSONLookup => strToNumericJson(jsonLookup.id)}
    )
    case _ => data.sync[JSONLookup](model)(
      {json:Json =>
        logger.debug(json.toString())
        val id = jsonToString(json)
        logger.debug(id.toString())
        logger.debug(lookup.toString())
        logger.debug(lookup.get.toString())
        val result = lookup.get.find(_.id == id).getOrElse(JSONLookup(id,id + " NOT FOUND"))
        logger.debug(result.toString)
        result
      },
      {jsonLookup:JSONLookup => strToJson(field.nullable)(jsonLookup.id)}
    )
  })



  val selectModel = Property("")
  autoRelease(data.sync(selectModel)(value2Label,label2Value))


  override def showOnTable(): JsDom.all.Modifier = autoRelease(bind(selectModel))




  private def toSeq(s:Seq[JSONLookup]):Seq[JSONLookup] = if(field.nullable) {
    Seq(JSONLookup("","")) ++ s
  } else {
    s
  }

  private def setNewLookup(newLookup:Seq[JSONLookup]) = {
    logger.info(newLookup.toString())
    if (newLookup.length != lookup.get.length || newLookup.exists(lu => lookup.get.exists(_.id != lu.id))) {
      logger.info("Lookup list changed")
      lookup.set(newLookup, true)
      if(!newLookup.exists(_.id == data.get.string)) {
        logger.info("Old value not exists")
        newLookup.headOption.foreach{x =>
          logger.info(s"Setting model to $x")
          model.set(x,true)
        }
      }

    }
  }

  field.lookup.get.lookupExtractor.foreach{case extractor =>
    allData.listen({ all =>
      logger.debug(all.toString())
      val newLookup = toSeq(extractor.map.getOrElse(all.js(extractor.key), Seq()))
      setNewLookup(newLookup)
    },true)
  }


  for{
    look <- field.lookup
    query <- look.lookupQuery
  } yield {
    if(query.find(_ == '#').nonEmpty) {

      val variables =extractVariables(query)
      val queryWithSubstitutions = allData.transform({ json =>
        variables.foldRight(query){(variable, finalQuery) =>
          finalQuery.replaceAll("#" + variable, "\"" + json.js(variable).string + "\"")
        }
      })
      queryWithSubstitutions.listen({ q =>
        lookup.set(Seq(), true) //reset lookup state

        val jsonQuery = parser.parse(q) match {
          case Left(e) => {
            logger.error(e.message)
            Json.Null
          }
          case Right(j) => j
        }

        services.rest.lookup(services.clientSession.lang(),look.lookupEntity, look.map, jsonQuery).map { lookups =>
          setNewLookup(toSeq(lookups))
        }

      }, true)
    }
  }

  private def extractVariables(query:String):Seq[String] = {
    query.zipWithIndex.filter(_._1 == '#').map{ case (_,i) =>
      val nextIndex = Seq(query.length,query.indexOf(' ',i),query.indexOf('}',i),query.indexOf(',',i)).min
      query.substring(i+1,nextIndex).replaceAll("\n","").trim
    }.distinct
  }



  private def value2Label(org:Json):String = {

    val lookupValue = allData.get.get(field.lookup.get.map.localValueProperty)

    lookup.get.find(_.id == lookupValue).map(_.value)
      .orElse(field.lookup.get.lookup.find(_.id == org.string).map(_.value))
      .getOrElse(Labels.lookup.not_found)
  }
  private def label2Value(v:String):Json = lookup.get.find(_.value == v).map(_.id.asJson).orElse(field.lookup.get.lookup.find(_.value == v).map(_.id.asJson)).getOrElse(Json.Null)
}