package ch.wsl.box.services.files

import ch.wsl.box.model.shared.JSONID
import ch.wsl.box.services.file.{FileCacheKey, FileId, ImageCacheStorage}

import scala.concurrent.{ExecutionContext, Future}




class InMemoryImageCacheStorage extends ImageCacheStorage {

  private val store:scala.collection.mutable.Map[FileCacheKey,Array[Byte]] = scala.collection.mutable.Map[FileCacheKey,Array[Byte]]()


  override def clearField(id: FileId)(implicit ex: ExecutionContext): Future[Boolean] = Future.successful{
    store.filter(_._1.id == id).map{ r =>
      store.remove(r._1)
    }
    true
  }

  override def save(fileId: FileCacheKey, data: Array[Byte])(implicit ex:ExecutionContext): Future[Boolean] = Future.successful{
    store.update(fileId,data)
    true
  }

  override def delete(fileId: FileCacheKey)(implicit ex:ExecutionContext): Future[Boolean] = Future.successful{
    store.remove(fileId)
    true
  }

  override def get(fileId: FileCacheKey)(implicit ex:ExecutionContext): Future[Option[Array[Byte]]] = Future.successful{
    store.get(fileId)
  }
}