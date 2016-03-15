package postgresweb.models

import postgresweb.routes.Container

case class Menu(name: String, route: (String,String) => Container)