create table if not exists documents
(
	id bigserial not null
		constraint documents_pkey
			primary key,
	checksum varchar(255),
	error_message text,
	filename varchar(255),
	import_date timestamp,
	page_count integer not null,
	payload oid,
	state varchar(255)
)
;

create table if not exists jobs
(
	id bigserial not null
		constraint jobs_pkey
			primary key,
	type varchar(255) not null,
	complete_time timestamp,
	create_time timestamp,
	error_message text,
	start_time timestamp,
	state varchar(255),
	source varchar(255),
	document_id bigint
		constraint fk_documents_jobs_document_id
			references documents
)
;

create table if not exists logs
(
	id bigserial not null
		constraint logs_pkey
			primary key,
	date timestamp not null,
	level varchar(255) not null,
	message varchar(255) not null,
	job_id bigint not null
		constraint fk_job_logs_job_id
			references jobs
)
;

create table if not exists pages
(
	id bigserial not null
		constraint pages_pkey
			primary key,
	checksum varchar(255),
	error_message text,
	name varchar(255),
	page_number integer not null,
	state varchar(255),
	payload oid,
	thumbnail oid,
	document_id bigint
		constraint fk_documents_pages_document_id
			references documents
)
;

create table if not exists roles
(
	id bigserial not null
		constraint roles_pkey
			primary key,
	name varchar(255)
)
;

create table if not exists users
(
	id bigserial not null
		constraint users_pkey
			primary key,
	avatar oid,
	email varchar(255) not null
		constraint uk_users_email
			unique,
	name varchar(255) not null,
	password varchar(255) not null,
	register_date timestamp not null,
	state varchar(255) not null
)
;

