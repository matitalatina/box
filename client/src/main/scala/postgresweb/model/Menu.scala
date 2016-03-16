package postgresweb.model

import postgresweb.controllers.Container

case class Menu(name: String, route: (String,String) => Container)