package ch.wsl.box.client.model

import ch.wsl.box.client.controllers.Container

case class Menu(name: String, route: (String,String) => Container)