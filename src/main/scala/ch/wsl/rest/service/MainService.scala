package ch.wsl.rest.service


import ch.wsl.rest.service.Auth.CustomUserPassAuthenticator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import akka.actor.Actor
import spray.http.MediaTypes.{ `text/html` }
import spray.routing.Directive.pimpApply
import spray.routing.{Route, HttpService, RejectionHandler}
import spray.routing.authentication.BasicAuth
import spray.routing.directives.AuthMagnet.fromContextAuthenticator

import ch.wsl.model.Tables._



// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MainServiceActor extends Actor with MainService  {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  
  implicit val myRejectionHandler = RejectionHandler {
    case t => {
      println(t)

      complete("Something went wrong here: " + t)
    }
    case _ => complete("Something went wrong here")
  }

  
  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(s4Route)
}




/**
 *  this trait defines our service behavior independently from the service actor
 */
trait MainService extends HttpService with CORSSupport with ModelRoutes {
  


  
  
  val index = get { ctx =>
          respondWithMediaType(`text/html`) {  // XML is marshalled to `text/xml` by default, so we simply override here
            complete {
              <html>
                <body>
                  <h1>The <b>S4</b> - <i>Slick Spray Scala Stack</i> is running :-)</h1>
                </body>
              </html>
            }
          }
        }
  
  val s4Route:Route = {
    
      import JsonProtocol._

    
      pathEnd {
        index
      } ~
      cors{
        options {
           complete(spray.http.StatusCodes.OK)
        } ~
        authenticate(BasicAuth(CustomUserPassAuthenticator, "person-security-realm")) { userProfile =>
            implicit val db = userProfile.db
            model[Canton,CantonRow]("canton",Canton) ~
            model[CatCause,CatCauseRow]("cat_cause", CatCause) ~
            model[CatCauseBafu,CatCauseBafuRow]("cat_cause_bafu", CatCauseBafu) ~
            model[Days,DaysRow]("days", Days) ~
            model[Fire,FireRow]("fire",Fire)  ~
            model[ValAttribute,ValAttributeRow]("val_attribute",ValAttribute)  ~
            model[ValBafuForestType,ValBafuForestTypeRow]("val_bafu_forest_type",ValBafuForestType) ~
            model[ValCause,ValCauseRow]("val_cause",ValCause)  ~
            model[ValCauseReliability,ValCauseReliabilityRow]("val_cause_reliability",ValCauseReliability)  ~
            model[ValCoordReliability,ValCoordReliabilityRow]("val_coord_reliability",ValCoordReliability)  ~
            model[ValDamage,ValDamageRow]("val_damage",ValDamage)  ~
            model[ValDateReliability,ValDateReliabilityRow]("val_date_reliability",ValDateReliability)  ~
            model[ValDefinition,ValDefinitionRow]("val_definition",ValDefinition)  ~
            model[ValExposition,ValExpositionRow]("val_exposition",ValExposition)  ~
            model[ValLayerAbundance,ValLayerAbundanceRow]("val_layer_abundance",ValLayerAbundance)  ~
            model[ValMonth,ValMonthRow]("val_month",ValMonth)  ~
            model[ValSite,ValSiteRow]("val_site",ValSite)  ~
            model[SysForm,SysFormRow]("sys_form",SysForm)  ~
          //viewRoute[VRegionMunicipality,VRegionMunicipalityRow]("v_region_municipality",VRegionMunicipality)  ~
          path("models") {
            get{
              complete(models)
            }
          }
        }
      }
    
  }


}

