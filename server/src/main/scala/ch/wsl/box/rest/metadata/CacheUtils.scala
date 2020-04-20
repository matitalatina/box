package ch.wsl.box.rest.metadata

import akka.actor.Actor
import ch.wsl.box.model.shared.JSONMetadata
import ch.wsl.box.rest.metadata.CacheUtils.ResetCacheForEntity

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object CacheUtils {
  def checkIfHasForeignKeys(entity:String,metadata: Future[JSONMetadata]) = {
    //futures when stored in the cache should already be resolved so thats non blocking
    val m = Await.result(metadata,10 seconds)
    m.fields.exists(_.lookup.exists(_.lookupEntity == entity))
  }

  case class ResetCacheForEntity(e:String)
}

class CacheWorker extends Actor {
  override def receive: Receive = {
    case ResetCacheForEntity(e) => {
      EntityMetadataFactory.resetCacheForEntity(e)
      FormMetadataFactory.resetCacheForEntity(e)
    }
    case _ => {}
  }
}
