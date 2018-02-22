package ch.wsl.box.rest.routes.enablers

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.ContentTypes
import akka.util.ByteString
import ch.wsl.box.model.shared.JSONData
import ch.wsl.box.shared.utils.CSV

trait CSVDownload {


  implicit def asCsv[M <: Product] = Marshaller.strict[M, ByteString] { t =>
    Marshalling.WithFixedContentType(ContentTypes.`text/csv(UTF-8)`, () => {
      import JSONData._
      ByteString(CSV.row(t.values()))
    })
  }

  implicit val asCsvText = Marshaller.strict[String, ByteString] { t =>
    Marshalling.WithFixedContentType(ContentTypes.`text/csv(UTF-8)`, () => {
      ByteString(t)
    })
  }

  // [2] enable csv streaming:
  implicit val csvStreaming = EntityStreamingSupport.csv()
}
