package ch.wsl.box.services.files

import java.io.{ByteArrayInputStream, InputStream}

import akka.stream.scaladsl.StreamConverters
import ch.wsl.box.model.shared.JSONID
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.{Color, RGBColor, X11Colorlist}
import com.sksamuel.scrimage.nio.JpegWriter
import nz.co.rossphillips.thumbnailer.Thumbnailer
import nz.co.rossphillips.thumbnailer.thumbnailers.{DOCXThumbnailer, ImageThumbnailer, PDFThumbnailer, TextThumbnailer}
import org.apache.tika.Tika
import scribe.Logging
import wvlet.airframe.bind

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

case class FileId(rowId:JSONID,column:String) {
  def name(mime:Option[String]):String = {
    val extenstion = mime match {
      case Some(s) if s.endsWith("jpeg") => ".jpg"
      case Some(s) if s.endsWith("png") => ".png"
      case _ => ""
    }

    s"${rowId.asString}-$column$extenstion"

  }
}

sealed trait FileCacheKey{
  def id:FileId
}
case class ThumbnailCacheKey(id:FileId,width:Int,height:Int) extends FileCacheKey
case class WidthCacheKey(id:FileId,width:Int) extends FileCacheKey
case class CoverCacheKey(id:FileId,width:Int,height:Int) extends FileCacheKey
case class FitCacheKey(id:FileId,width:Int,height:Int,color:String) extends FileCacheKey


trait ImageCache extends Logging {
  private val tika = new Tika()

  private val storage = bind[ImageCacheStorage]

  private def createThumbnail(width:Int,height:Int)(data:Array[Byte])(implicit ec:ExecutionContext):Future[Array[Byte]] = {
    Future {

        val thumbnailer = new Thumbnailer(new PDFThumbnailer, new TextThumbnailer, new ImageThumbnailer, new DOCXThumbnailer)
        thumbnailer.setSize(width, height)
        thumbnailer.generateThumbnail(new ByteArrayInputStream(data), mime(data))
    }
  }



  private def createWidth(width:Int)(data:Array[Byte])(implicit ec:ExecutionContext):Future[Array[Byte]] = Future{
    ImmutableImage.loader().fromBytes(data).scaleToWidth(width).bytes(new JpegWriter().withCompression(80))
  }

  private def createCover(width:Int,height:Int)(data:Array[Byte])(implicit ec:ExecutionContext):Future[Array[Byte]] = Future{
    ImmutableImage.loader().fromBytes(data).cover(width, height).bytes(new JpegWriter().withCompression(80))
  }

  private def createFit(width:Int,height:Int,color:String)(data:Array[Byte])(implicit ec:ExecutionContext):Future[Array[Byte]] = Future{

    val c:Color = Try(java.awt.Color.decode(color)) match {
      case Success(value) => new RGBColor(value.getRed, value.getGreen, value.getGreen)
      case _ => X11Colorlist.White
    }

    ImmutableImage.loader().fromBytes(data)
      .fit(width, height, c.toAWT)
      .bytes(new JpegWriter().withCompression(80))

  }

  private def process(data: => Future[Option[Array[Byte]]], key:FileCacheKey, action: Array[Byte] => Future[Array[Byte]])(implicit ec:ExecutionContext):Future[Array[Byte]] = {
      for{
        cached <- storage.get(key)
        result <- cached match {
          case Some(value) => Future.successful(value)
          case None => for{
            d <- data
            obj <- action(d.get)
            _ <- storage.save(key,obj)
          } yield obj
        }
      } yield result
  }

  private def processImage(data: => Future[Option[Array[Byte]]], key:FileCacheKey, action: Array[Byte] => Future[Array[Byte]])(implicit ec:ExecutionContext):Future[Array[Byte]] = {

    def f(d:Array[Byte]):Future[Array[Byte]] = {
      if(mime(d).startsWith("image")) {
        action(d)
      } else {
        for{
          img <- createThumbnail(1000,1000)(d)
          result <- action(img)
        } yield result
      }
    }

    process(data,key,f)
  }

  def mime(data:Array[Byte]):String = {
    val mime = tika.detect(data.take(4096))
    logger.info(s"Detected file with mime: $mime")
    mime
  }

  def thumbnail(id:FileId,data: => Future[Option[Array[Byte]]],width:Int,height:Int)(implicit ec:ExecutionContext):Future[Array[Byte]] = {
    process(data,ThumbnailCacheKey(id, width, height),createThumbnail(width, height))
  }

  def width(id:FileId,data: => Future[Option[Array[Byte]]],width:Int)(implicit ec:ExecutionContext):Future[Array[Byte]] = {
    processImage(data,WidthCacheKey(id, width),createWidth(width))
  }

  def cover(id:FileId,data: => Future[Option[Array[Byte]]],width:Int,height:Int)(implicit ec:ExecutionContext):Future[Array[Byte]] = {
    processImage(data,CoverCacheKey(id, width,height),createCover(width, height))
  }

  def fit(id:FileId,data: => Future[Option[Array[Byte]]],width:Int,height:Int,color:String)(implicit ec:ExecutionContext):Future[Array[Byte]] = {
    processImage(data,FitCacheKey(id, width, height, color),createFit(width, height, color))
  }
}

trait ImageCacheStorage {
  def save(fileId: FileCacheKey,data:Array[Byte]):Future[Boolean]
  def delete(fileId: FileCacheKey):Future[Boolean]
  def get(fileId: FileCacheKey):Future[Option[Array[Byte]]]
}

class InMemoryImageCacheStorage extends ImageCacheStorage {

  private val store:scala.collection.mutable.Map[FileCacheKey,Array[Byte]] = scala.collection.mutable.Map[FileCacheKey,Array[Byte]]()

  override def save(fileId: FileCacheKey, data: Array[Byte]): Future[Boolean] = Future.successful{
    store.update(fileId,data)
    true
  }

  override def delete(fileId: FileCacheKey): Future[Boolean] = Future.successful{
    store.remove(fileId)
    true
  }

  override def get(fileId: FileCacheKey): Future[Option[Array[Byte]]] = Future.successful{
    store.get(fileId)
  }
}
