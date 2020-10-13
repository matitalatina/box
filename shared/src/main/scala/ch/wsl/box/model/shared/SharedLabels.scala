package ch.wsl.box.model.shared

object SharedLabels {
  object form{
    def save = "form.save"
    def save_add = "form.save_add"
    def save_table = "form.save_table"
  }


  object entities{
    def `new` = "entity.new"
    def table = "entity.table"
    def duplicate = "entity.duplicate"
  }

  object entity{
    def delete = "table.delete"
    def revert = "table.revert"
    def confirmDelete = "table.confirmDelete"
    def confirmRevert = "table.confirmRevert"
  }

  object header{
    def lang = "header.lang"
  }

}
