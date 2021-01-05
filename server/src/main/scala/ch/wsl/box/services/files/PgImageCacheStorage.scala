package ch.wsl.box.services.files
import scala.concurrent.{ExecutionContext, Future}
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.model.boxentities.BoxImageCache
import ch.wsl.box.model.shared.JSONID
import ch.wsl.box.rest.utils.Auth
import ch.wsl.box.services.file.{FileCacheKey, FileId, ImageCacheStorage}

class PgImageCacheStorage extends ImageCacheStorage {
  override def save(fileId: FileCacheKey, data: Array[Byte])(implicit ex:ExecutionContext): Future[Boolean] = Auth.boxDB.run{
    BoxImageCache.Table.insertOrUpdate(BoxImageCache.BoxImageCache_row(fileId.asString,data))
  }.map(_ => true)

  override def delete(fileId: FileCacheKey)(implicit  ex:ExecutionContext): Future[Boolean] = Auth.boxDB.run{
    BoxImageCache.Table.filter(_.key === fileId.asString).delete
  }.map(_ => true)

  override def get(fileId: FileCacheKey)(implicit ex:ExecutionContext): Future[Option[Array[Byte]]] = Auth.boxDB.run{
    BoxImageCache.Table.filter(_.key === fileId.asString).take(1).result
  }.map(_.headOption.map(_.data))

  override def clearField(id: FileId)(implicit ex: ExecutionContext): Future[Boolean] = Auth.boxDB.run{
    BoxImageCache.Table.filter(_.key.startsWith(id.asString(""))).delete
  }.map(_ => true)
}
