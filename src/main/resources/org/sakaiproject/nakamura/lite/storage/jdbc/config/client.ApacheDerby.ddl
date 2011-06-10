
CREATE TABLE css (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v varchar(780) NOT NULL,
  primary key(id));
CREATE INDEX css_i ON css (rid, cid);

CREATE TABLE au_css (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v varchar(780) NOT NULL,
  primary key(id));
CREATE INDEX au_css_i ON au_css (rid, cid);

CREATE TABLE ac_css (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v varchar(780) NOT NULL,
  primary key(id));
CREATE INDEX ac_css_i ON ac_css (rid, cid);

CREATE TABLE cn_css (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v varchar(780) NOT NULL,
  primary key(id));
CREATE INDEX cn_css_i ON cn_css (rid, cid);



CREATE TABLE  csb (
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v blob,
  primary key(rid,cid));

CREATE TABLE  au_csb (
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v blob,
  primary key(rid,cid));

CREATE TABLE  ac_csb (
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v blob,
  primary key(rid,cid));

CREATE TABLE  cn_csb (
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v blob,
  primary key(rid,cid));

CREATE INDEX css_locate_idx ON css (v, cid);
CREATE INDEX au_css_locate_idx ON au_css (v, cid);
CREATE INDEX ac_css_locate_idx ON ac_css (v, cid);
CREATE INDEX cn_css_locate_idx ON cn_css (v, cid);


# Central Store for Object bodies, serialized content maps rather than columns
CREATE TABLE  css_b (
  rid varchar(32) NOT NULL,
  b blob,
  primary key(rid));

CREATE TABLE  au_css_b (
  rid varchar(32) NOT NULL,
  b blob,
  primary key(rid));

CREATE TABLE  ac_css_b (
  rid varchar(32) NOT NULL,
  b blob,
  primary key(rid));

CREATE TABLE  cn_css_b (
  rid varchar(32) NOT NULL,
  b blob,
  primary key(rid));

  
# Columns that need to be indexed
CREATE TABLE  index_cols (cid varchar(64) NOT NULL, primary key(cid));

insert into index_cols (cid) values ('au:rep:principalName');
insert into index_cols (cid) values ('au:type');
insert into index_cols (cid) values ('cn:sling:resourceType');
insert into index_cols (cid) values ('cn:sakai:pooled-content-manager');
# /var/search/comments/discussions/threaded.json
insert into index_cols (cid) values ('cn:sakai:messagestore');
insert into index_cols (cid) values ('cn:sakai:type');
insert into index_cols (cid) values ('cn:sakai:marker');
# *.tagged.json 
insert into index_cols (cid) values ('cn:sakai:tag-uuid');
# /var/contacts/findstate.json
insert into index_cols (cid) values ('cn:sakai:contactstorepath');
insert into index_cols (cid) values ('cn:sakai:state');
insert into index_cols (cid) values ('cn:firstName');
insert into index_cols (cid) values ('cn:lastName');
# content sorting
insert into index_cols (cid) values ('cn:_created');
# /var/message/boxcategory.json
insert into index_cols (cid) values ('cn:sakai:category');
insert into index_cols (cid) values ('cn:sakai:messagebox');
insert into index_cols (cid) values ('cn:sakai:from');
insert into index_cols (cid) values ('cn:sakai:subject');

