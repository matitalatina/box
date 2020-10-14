package ch.wsl.box.client.utils

object TestHooks {
  def langSwitch(lang:String) = s"langSwitch_$lang"
  def tableChildId(id:Int) = s"tableChildFormId$id"

  val logoutButton = "logoutButton"
}
