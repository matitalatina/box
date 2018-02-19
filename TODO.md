TODO
====

1. Reduced interface for users
1. errors management:
    - Data insert validation
    - server side error response in json and correct return codes
1. add CSV download
1. custom Search mask

##### Details
1. Back to table always go to the first page of the table


##### To check
1. Change save behaviour, let the user chose between 
    - `Save and continue edit`
    - `Save and back to table`
    - `Save and create new`

##### Someday
1. visualizzazione/modifica/insert dati geografici

BUGS
===
1. Double authentication for Images/Downloads/CSV
1. Subform propagation id on error (ie. saving a fire with a fire_municipality_start without fire_id then adding fire_id and saving again)