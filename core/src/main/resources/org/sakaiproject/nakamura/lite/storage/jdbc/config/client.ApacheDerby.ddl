
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

CREATE TABLE lk_css (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v varchar(780) NOT NULL,
  primary key(id));
CREATE INDEX lk_css_i ON lk_css (rid, cid);

CREATE TABLE css_w (
  rid varchar(32) NOT NULL,
  primary key(rid));


  
CREATE TABLE ac_css_w (
  rid varchar(32) NOT NULL,
  primary key(rid));



  
  
CREATE TABLE au_css_w (
  rid varchar(32) NOT NULL,
  primary key(rid));


CREATE TABLE cn_css_w (
  rid varchar(32) NOT NULL,
  primary key(rid));

CREATE TABLE lk_css_w (
  rid varchar(32) NOT NULL,
  primary key(rid));


CREATE TABLE  css_wr (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  cf varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  cname varchar(64) NOT NULL,
  primary key(id));
  
CREATE UNIQUE INDEX css_r_cid ON css_wr (cf,cid);
CREATE UNIQUE INDEX css_r_cnam ON css_wr (cf,cname);



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

CREATE TABLE  lk_csb (
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v blob,
  primary key(rid,cid));

CREATE INDEX css_locate_idx ON css (v, cid);
CREATE INDEX au_css_locate_idx ON au_css (v, cid);
CREATE INDEX ac_css_locate_idx ON ac_css (v, cid);
CREATE INDEX cn_css_locate_idx ON cn_css (v, cid);
CREATE INDEX lk_css_locate_idx ON lk_css (v, cid);


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


CREATE TABLE  lk_css_b (
  rid varchar(32) NOT NULL,
  b blob,
  primary key(rid));
  


