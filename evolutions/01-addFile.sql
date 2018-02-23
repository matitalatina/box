create table file
(
	file_id serial not null
		constraint file_pkey
			primary key,
	file bytea not null,
	name text not null,
	mime text not null
);

