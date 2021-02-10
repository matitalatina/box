update box.field set params='{"fullWidth": true}',widget=null where widget='fullWidth' and params is null;

update box.field set widget='selectWidget' where widget is null and "lookupEntity" is not null;