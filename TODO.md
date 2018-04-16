TODO
====


1. export page (DB driven)
1. visualizzazione campi su condizione di altri campi (agginugere a box.field 2 colonne `conditionFieldId` che fa riferimento al field da "osservare" e `conditionValue` con i valori per cui il campo deve essere visualizzato )
1. thumbnails of images/pdf
1. performance review
1. column filtering on lookup values


##### Details


##### Someday

1. custom Search mask
1. visualizzazione/modifica/insert dati geografici
1. aggiungere lookupFilter in box.field
1. errors management:
    - Data insert validation field to field


FRAMEWORK
====
1. Generation BOX Schema
1. Automatic generation of form stub
1. Form Mask to modify forms
1. Access to box schema through box

BUGS
===
1. Subform propagation id on error (ie. saving a fire with a fire_municipality_start without fire_id then adding fire_id and saving again) -- needs trigger to be properly tested
1. Insert fire, fire_id default
1. Update image, array out of bound

DB
===
1. triggers for fire_id