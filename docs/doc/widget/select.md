---
title: Select
parent: Widgets
grand_parent: Documentation
nav_order: 15
---

# Select

This widget present a dropdown menu

#### Supported types
- **String**
- **Number**

#### Interface builder fields

| Entity             | Field name        | Description                                                        | Required          | Default           |
|:-------------------|:------------------|:-------------------------------------------------------------------|:------------------|:------------------|
| field              | lookupEntity      | Select the target entity                                           | `true`            | `none`            |
| field              | lookupValueField  | Column on the foreign entity                                       | `true`            | `none`            |
| field              | lookupQuery       | Filter the selectable result of the lookup table, JSONQuery format | `false`            | `none`            |
| field translations | lookupTextField   | Field on the child form that should be used as label               | `false`           | `none`            |

