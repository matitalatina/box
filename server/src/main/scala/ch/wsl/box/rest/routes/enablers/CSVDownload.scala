package ch.wsl.box.rest.routes.enablers

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.ContentTypes
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import ch.wsl.box.model.shared.JSONData
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}

trait CSVDownload {


  implicit def asCsv[M <: Product] = Marshaller.strict[M, ByteString] { t =>
    Marshalling.WithFixedContentType(ContentTypes.`text/csv(UTF-8)`, () => {
      import JSONData._
      ByteString(CSV.writeRow(t.values()))
    })
  }

  implicit val asCsvText = Marshaller.strict[String, ByteString] { t =>
    Marshalling.WithFixedContentType(ContentTypes.`text/csv(UTF-8)`, () => {
      ByteString(t)
    })
  }

  // [2] enable csv streaming:
  implicit val csvStreaming = EntityStreamingSupport.csv()
    .withFramingRenderer(Flow[ByteString].map(bs => bs)) //no new line, let the CSV library manage the new lines
}
