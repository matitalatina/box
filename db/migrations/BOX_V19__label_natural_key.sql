
alter table box.labels drop constraint labels_pkey;

alter table box.labels drop column id;

alter table box.labels
    add constraint labels_pkey
        primary key (lang, key);