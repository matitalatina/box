package ch.wsl.box.rest.metadata

import akka.stream.Materializer
import ch.wsl.box.model.boxentities.Field.{Field_i18n_row, Field_row}
import ch.wsl.box.model.boxentities.{Field, Form}
import ch.wsl.box.model.boxentities.Form.{Form_i18n_row, Form_row}
import ch.wsl.box.model.shared.{Layout, LayoutBlock}
import ch.wsl.box.rest.utils.{BoxConf, UserProfile}

import scala.concurrent.{ExecutionContext, Future}

object StubMetadataFactory {

  import io.circe._
  import io.circe.syntax._
  import io.circe.generic.auto._
  import ch.wsl.box.model.Entities.profile.api._

  def forEntity(entity:String)(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext):Future[Boolean] = {

    for{
      langs <- Future.sequence(BoxConf.langs.map{ lang =>
        EntityMetadataFactory.of(entity,lang).map(x => (lang,x))
      })
      metadata = langs.head._2
      form <- {
        val newForm = Form_row(
          form_id = None,
          name = entity,
          description = None,
          entity = entity,
          layout = Some(metadata.layout.asJson.toString()),
          tabularFields = Some(metadata.tabularFields.mkString(",")),
          query = None,
          exportFields = Some(metadata.exportFields.mkString(","))
        )

        up.boxDb.run{
          (Form.table.returning(Form.table) += newForm).transactionally
        }
      }
      formI18n <- Future.sequence(langs.map{ lang =>
        val newFormI18n = Form_i18n_row(
          form_id = form.form_id,
          lang = Some(lang._1),
          label = Some(entity)
        )
        up.boxDb.run{
          (Form.Form_i18n.returning(Form.Form_i18n) += newFormI18n).transactionally
        }
      })
      fields <- Future.sequence(metadata.fields.map{ field =>
        val newField = Field_row(
          form_id = form.form_id.get,
          `type` = field.`type`,
          name = field.name,
          widget = field.widget,
          lookupEntity = field.lookup.map(_.lookupEntity),
          lookupValueField = field.lookup.map(_.map.valueProperty),
          default = field.default,
        )

        up.boxDb.run{
          (Field.table.returning(Field.table) += newField).transactionally
        }
      })
      fieldsI18n <- {
        val t1: Future[Seq[Seq[Field_i18n_row]]] = Future.sequence(langs.map{ lang =>
          val t: Future[Seq[Field_i18n_row]] = Future.sequence(lang._2.fields.map{ case jsonField =>

            val field = fields.find(_.name == jsonField.name ).get

            val newFieldI18n = Field_i18n_row(
              field_id = field.field_id,
              lang = Some(lang._1),
              label = jsonField.label,
              placeholder = jsonField.placeholder,
              tooltip = jsonField.tooltip,
              lookupTextField = jsonField.lookup.map(_.map.textProperty)
            )

            up.boxDb.run{
              (Field.Field_i18n.returning(Field.Field_i18n) += newFieldI18n).transactionally
            }

          })
          t
        })
        t1
      }
    } yield {
      true
    }

  }

}
