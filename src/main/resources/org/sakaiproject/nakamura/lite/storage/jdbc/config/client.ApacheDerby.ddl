
CREATE TABLE css ( 
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v varchar(780) NOT NULL);



CREATE TABLE  csb (
  rid varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  v blob);

CREATE INDEX css_rc_idx ON css (rid, cid);
CREATE INDEX css_rc_idx ON csb (rid, cid);
CREATE INDEX css_locate_idx ON css (v, cid);