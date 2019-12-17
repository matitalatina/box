package ch.wsl.box.client.views.components.widget
import ch.wsl.box.client.styles.{BootstrapCol, GlobalStyles}
import ch.wsl.box.client.utils.{ClientConf, Labels}
import ch.wsl.box.model.shared._
import io.circe._
import io.circe.syntax._
import io.udash._
import io.udash.properties.single.Property
import org.scalajs.dom.Event
import ch.wsl.box.shared.utils.JSONUtils._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.button.{ButtonStyle, UdashButton, UdashButtonGroup}
import io.udash.bootstrap.modal.UdashModal.ModalHiddenEvent
import io.udash.bootstrap.modal.{ModalSize, UdashModal}
import org.scalajs.dom
import scalatags.JsDom
import scribe.Logging

import scala.concurrent.duration._



case class PopupWidgetFactory(allData:Property[Json]) extends ComponentWidgetFactory  {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField): Widget = PopupWidget(field,prop,allData)
}

case class PopupWidget(field:JSONField, data: Property[Json],allData:Property[Json]) extends LookupWidget with Logging {

import ch.wsl.box.client.Context._
  import scalacss.ScalatagsCss._

  import scalatags.JsDom.all.{label => lab}
  import scalatags.JsDom.all._
  import io.udash.css.CssView._



  override protected def show(): JsDom.all.Modifier = autoRelease(WidgetUtils.showNotNull(data){ _ =>
    val selectedItem: Property[String] = data.transform(value2Label,label2Value)

    div(BootstrapCol.md(12),ClientConf.style.noPadding,ClientConf.style.smallBottomMargin)(
      label(field.title),
      div(BootstrapStyles.pullRight, ClientConf.style.largeButton,
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
      lab(Labels.popup.search),br,
      TextInput(searchProp,500.milliseconds)(),br,br,
      autoRelease(showIf(modalStatus.transform(_ == Status.Open)) {
        div(autoRelease(produce(searchProp) { searchTerm =>
          div(
            autoRelease(produce(lookup) { lu =>
              div(
                lu.filter(opt => searchTerm == "" || opt.value.toLowerCase.contains(searchTerm.toLowerCase)).map { case JSONLookup(key, value) =>
                  div(a(value, onclick :+= ((e: Event) => {
                    modalStatus.set(Status.Closed)
                    selectedItem.set(value)
                  })))
                }
              ).render
            })
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
      button(onclick :+= ((e:Event) => modalStatus.set(Status.Closed),true), Labels.popup.close)
    ).render

    val modal:UdashModal = UdashModal(modalSize = ModalSize.Large)(
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
    val tooltip = WidgetUtils.addTooltip(field.tooltip) _

    div(BootstrapCol.md(12),ClientConf.style.noPadding, ClientConf.style.smallBottomMargin)(
      WidgetUtils.toLabel(field),
      tooltip(button(BootstrapStyles.pullRight, ClientConf.style.largeButton, onclick :+= ((e:Event) => modalStatus.set(Status.Open),true),bind(selectedItem)).render),
      modal.render,
      div(BootstrapStyles.Visibility.clearfix)
    )
  }
}
