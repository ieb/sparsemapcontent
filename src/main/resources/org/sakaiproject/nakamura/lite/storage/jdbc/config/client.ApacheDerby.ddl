
CREATE TABLE css ( 
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v varchar(780) NOT NULL,
  primary key(rid,cid));



CREATE TABLE  csb (
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v blob,
  primary key(rid,cid));

CREATE INDEX css_locate_idx ON css (v, cid);


# Central Store for Object bodies, serialized content maps rather than columns
CREATE TABLE  css_b (
  rid varchar(32) NOT NULL,
  b blob,
  primary key(rid));

  
# Columns that need to be indexed
CREATE TABLE  index_cols (cid varchar(64) NOT NULL);

insert into index_cols (cid) values ('au:rep:principalName');
insert into index_cols (cid) values ('au:type');
