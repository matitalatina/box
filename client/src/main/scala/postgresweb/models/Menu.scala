package postgresweb.models

import postgresweb.routes.AppRouter.AppPage

case class Menu(name: String, route: AppPage)