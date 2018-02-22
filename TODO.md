TODO
====

1. add CSV download
1. on session expired go to login & save actual page
1. Reduced interface for users, conf object for customization
1. thumbnails of images/pdf
1. export page (DB driven)
1. visualizzazione campi su condizione di altri campi (agginugere a box.field 2 colonne `conditionFieldId` che fa riferimento al field da "osservare" e `conditionValue` con i valori per cui il campo deve essere visualizzato )
1. sostituire all println with logger
1. errors management:
    - Data insert validation

##### Details
1. Back to table always go to the first page of the table
1. add invalid password message on login



##### To check
1. Change save behaviour, let the user chose between 
    - `Save and continue edit`
    - `Save and back to table`
    - `Save and create new`

##### Someday
1. aggiungere lookupFilter in box.field
1. visualizzazione/modifica/insert dati geografici
1. custom Search mask



BUGS
===
1. Subform propagation id on error (ie. saving a fire with a fire_municipality_start without fire_id then adding fire_id and saving again)