-- DROP TABLE lk_css cascade constraints;

CREATE TABLE  lk_css (
  id NUMBER NOT NULL,
  rid varchar2(32) NOT NULL,
  cid varchar2(64) NOT NULL,
  v varchar2(780) NOT NULL,
  PRIMARY KEY  (id))
;

CREATE SEQUENCE seq_lk_css_id;

CREATE INDEX lk_css_rowkey ON lk_css(rid,cid);
CREATE INDEX lk_css_cid_locate_i ON lk_css(v,cid);

-- DROP TABLE css_w cascade constraints;

CREATE TABLE css_w (
  rid varchar2(32) NOT NULL,
  PRIMARY KEY(rid))
;

-- DROP TABLE ac_css_w cascade constraints;

CREATE TABLE ac_css_w (
  rid varchar2(32) NOT NULL,
  PRIMARY KEY(rid))
;

-- DROP TABLE au_css_w cascade constraints;

CREATE TABLE au_css_w (
  rid varchar2(32) NOT NULL,
  PRIMARY KEY(rid))
;

-- DROP TABLE cn_css_w cascade constraints;

CREATE TABLE cn_css_w (
  rid varchar2(32) NOT NULL,
  PRIMARY KEY(rid))
;

-- DROP TABLE lk_css_w cascade constraints;

CREATE TABLE lk_css_w (
  rid varchar2(32) NOT NULL,
  PRIMARY KEY(rid))
;


-- DROP TABLE css_wr cascade constraints;

CREATE TABLE  css_wr (
  id  NUMBER NOT NULL,
  cf varchar2(32) NOT NULL,
  cid varchar2(64) NOT NULL,
  cname varchar2(64) NOT NULL,
  primary key(id));


CREATE SEQUENCE seq_css_wr_id;

CREATE UNIQUE INDEX css_wr_cid ON css_wr(cf,cid);
CREATE UNIQUE INDEX css_wr_cnam ON css_wr(cf,cname);


-- DROP TABLE lk_css_b cascade constraints;

CREATE TABLE  lk_css_b (
  rid varchar2(32) NOT NULL,
  b blob,
  PRIMARY KEY (rid) )
;
