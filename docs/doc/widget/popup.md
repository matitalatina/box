---
title: Popup
parent: Widgets
grand_parent: Documentation
nav_order:11
---

# Popup

Widget for lookup fields, in order to choose the lookup opens a modal (popup windows) with all possibile choises and a search field.
Use this widget when the list it too long to be handles by the select widget.


#### Supported types
- **String**
- **Number**

#### Interface builder fields

| Entity          | Field name        | Description       | Required       | Default       |
|:----------------|:------------------|:------------------|:------------------|:------------------|
| field           | lookupEntity      | Select the table/view to look at           | `true`           | `none`           |
| field           | lookupValueField  | Field on the external table to match with the field of `name`, usually `name` is the foreign key refferint to `lookupValueField`       | `true`           | `none`           |
| field           | lookupQuery  | JSONQuery to filter and/or sort the possible selectable results       | `false`           | `none`           |
| field translations | lookupTextField  | JSONQuery to filter and/or sort the possible selectable results       | `false`           | `none`           |