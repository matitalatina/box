package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.JSONField

/**
  * Created by andreaminetti on 10/03/16.
  */
object Forms {
  def list = Seq("test_form")

  def apply(id:String) = Seq(JSONField("string","a","test"))
}
