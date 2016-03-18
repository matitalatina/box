package ch.wsl.box.client.model

import ch.wsl.box.client.controllers.Container
import ch.wsl.box.model.shared.JSONKeys

case class Menu(name: String, route: (String,JSONKeys) => Container)