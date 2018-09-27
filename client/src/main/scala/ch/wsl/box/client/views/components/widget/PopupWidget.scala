package ch.wsl.box.client.views.components.widget
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.model.shared.{JSONFieldLookup, JSONLookup}
import io.circe._
import io.circe.syntax._
import io.udash._
import io.udash.properties.single.Property
import org.scalajs.dom.Event
import ch.wsl.box.shared.utils.JsonUtils._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.button.{ButtonStyle, UdashButton, UdashButtonGroup}
import io.udash.bootstrap.modal.UdashModal.ModalHiddenEvent
import io.udash.bootstrap.modal.{ModalSize, UdashModal}
import org.scalajs.dom
import scalatags.JsDom
import scribe.Logging

import scala.concurrent.duration._



case class PopupWidget(lookup:JSONFieldLookup, label: String, data: Property[Json]) extends LookupWidget with Logging {

import ch.wsl.box.client.Context._
  import scalacss.ScalatagsCss._

  import scalatags.JsDom.all.{label => lab}
  import scalatags.JsDom.all._
  import io.udash.css.CssView._


  val sortedOptions = lookup.lookup //.sortBy(_.value)


  override protected def show(): JsDom.all.Modifier = autoRelease(WidgetUtils.showNotNull(data){ _ =>
    val selectedItem: Property[String] = data.transform(value2Label,label2Value)
    div(BootstrapCol.md(12),GlobalStyles.noPadding)(
      if(label.length >0) lab(label) else {},
      div(BootstrapStyles.pullRight,
        bind(selectedItem)
      ),
      div(BootstrapStyles.Visibility.clearfix)
    ).render
  })

  override def edit() = {

    object Status{
      val Closed = "closed"
      val Open = "open"
    }

    val searchProp = Property("")

    val modalStatus = Property(Status.Closed)

    val selectedItem: Property[String] = data.transform(value2Label,label2Value)

    val optionList:Modifier = div(
      lab("Search"),br,
      TextInput(searchProp,Some(500 milliseconds)),br,br,
      autoRelease(showIf(modalStatus.transform(_ == Status.Open)) {
        div(autoRelease(produce(searchProp) { searchTerm =>
          div(
              sortedOptions.filter(opt => searchTerm == "" || opt.value.toLowerCase.contains(searchTerm.toLowerCase)).map { case JSONLookup(key, value) =>
                div(a(value, onclick :+= ((e: Event) => {
                  modalStatus.set(Status.Closed)
                  selectedItem.set(value)
                })))
              }
          ).render
        })).render
      }
    ))

    val header = () => div(
      label,
      UdashButton()(
        UdashModal.CloseButtonAttr,
        BootstrapStyles.close, "×"
      ).render
    ).render

    val body = () => div(
      div(
        optionList
      )
    ).render

    val footer = () => div(
      button(onclick :+= ((e:Event) => modalStatus.set(Status.Closed),true),"Close")
    ).render

    val modal:UdashModal = UdashModal(modalSize = ModalSize.Small)(
      headerFactory = Some(header),
      bodyFactory = Some(body),
      footerFactory = Some(footer)
    )

    modal.listen { case ev:ModalHiddenEvent => modalStatus.set(Status.Closed) }

    modalStatus.listen{ state =>
      logger.info(s"State changed to:$state")
      state match {
        case Status.Open => modal.show()
        case Status.Closed => modal.hide()
      }
    }


    div(BootstrapCol.md(12),GlobalStyles.noPadding)(
      modal.render,
      if(label.length >0) lab(label) else {},
      div(style := "text-align: right",
        button(GlobalStyles.largeButton,onclick :+= ((e:Event) => modalStatus.set(Status.Open),true),autoRelease(bind(selectedItem)))
      )
    )
  }
}
