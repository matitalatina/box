package ch.wsl.box.services.file

import scala.concurrent.{ExecutionContext, Future}
import ch.wsl.box.model.shared.JSONID

case class FileId(rowId:JSONID,column:String) {
  def name(mime:Option[String],tpe:String):String = {
    val extension = mime match {
      case Some(s) if s.endsWith("jpeg") => ".jpg"
      case Some(s) if s.endsWith("png") => ".png"
      case _ => ""
    }

    asString(tpe) + extension

  }

  def asString(tpe:String):String = s"$column-${rowId.asString}-$tpe"
}

sealed trait FileCacheKey{
  def id:FileId

  def asString() = id.asString(this.getClass.getSimpleName.toLowerCase)

}
case class Thumbnail(id:FileId,width:Int,height:Int) extends FileCacheKey
case class Width(id:FileId,width:Int) extends FileCacheKey
case class Cover(id:FileId,width:Int,height:Int) extends FileCacheKey
case class Fit(id:FileId,width:Int,height:Int,color:String) extends FileCacheKey {
  override def asString(): String = super.asString() + "-" + color
}

trait ImageCacheStorage {
  def clearField(id:FileId)(implicit ex:ExecutionContext):Future[Boolean]
  def save(fileId: FileCacheKey,data:Array[Byte])(implicit ex:ExecutionContext):Future[Boolean]
  def delete(fileId: FileCacheKey)(implicit ex:ExecutionContext):Future[Boolean]
  def get(fileId: FileCacheKey)(implicit ex:ExecutionContext):Future[Option[Array[Byte]]]
}