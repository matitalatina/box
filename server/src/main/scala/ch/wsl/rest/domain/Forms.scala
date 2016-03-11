package ch.wsl.rest.domain

import ch.wsl.jsonmodels.JSONField

/**
  * Created by andreaminetti on 10/03/16.
  */
object Forms {
  def list = Seq("test_form")

  def apply(id:String) = Seq(JSONField("string","a","test"))
}
