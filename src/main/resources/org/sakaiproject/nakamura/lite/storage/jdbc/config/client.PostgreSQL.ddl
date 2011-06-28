########### DROP TABLE css;


CREATE TABLE css
(
  id serial,
  rid character varying(32) NOT NULL,
  cid character varying(64) NOT NULL,
  v character varying(780) NOT NULL,
  CONSTRAINT css_pk PRIMARY KEY (id)
);

ALTER TABLE css OWNER TO nakamura;
GRANT ALL ON TABLE css TO nakrole;

CREATE INDEX css_cid_locate_i ON css (v, cid);
CREATE INDEX css_rowkey ON css (rid, cid);



############ DROP TABLE au_css;


CREATE TABLE au_css
(
  id serial,
  rid character varying(32) NOT NULL,
  cid character varying(64) NOT NULL,
  v character varying(780) NOT NULL,
  CONSTRAINT au_css_pk PRIMARY KEY (id)
);

ALTER TABLE au_css OWNER TO nakamura;
GRANT ALL ON TABLE au_css TO nakrole;

CREATE INDEX au_css_cid_locate_i ON au_css (v, cid);
CREATE INDEX au_css_rowkey ON au_css (rid, cid);


######### DROP TABLE cn_css;



CREATE TABLE cn_css
(
  id serial,
  rid character varying(32) NOT NULL,
  cid character varying(64) NOT NULL,
  v character varying(780) NOT NULL,
  CONSTRAINT cn_css_pk PRIMARY KEY (id)
);

ALTER TABLE cn_css OWNER TO nakamura;
GRANT ALL ON TABLE cn_css TO nakrole;

CREATE INDEX cn_css_cid_locate_i ON cn_css (v, cid);
CREATE INDEX cn_css_rowkey ON cn_css (rid, cid);



########### DROP TABLE ac_css;



CREATE TABLE ac_css
(
  id serial,
  rid character varying(32) NOT NULL,
  cid character varying(64) NOT NULL,
  v character varying(780) NOT NULL,
  CONSTRAINT ac_css_pk PRIMARY KEY (id)
);

ALTER TABLE ac_css OWNER TO nakamura;
GRANT ALL ON TABLE ac_css TO nakrole;

CREATE INDEX ac_css_cid_locate_i ON ac_css (v, cid);
CREATE INDEX ac_css_rowkey ON ac_css (rid, cid);

########### DROP TABLE css_b;


CREATE TABLE css_b
(
  id serial,
  rid character varying(32) NOT NULL,
  b bytea,
  CONSTRAINT css_b_pk PRIMARY KEY (id)
);

ALTER TABLE css_b OWNER TO nakamura;
GRANT ALL ON TABLE css_b TO nakrole;

CREATE INDEX css_b_rowkey ON css_b (rid);


########## DROP TABLE cn_css_b;


CREATE TABLE cn_css_b
(
  id serial,
  rid character varying(32) NOT NULL,
  b bytea,
  CONSTRAINT cn_css_b_pk PRIMARY KEY (id)
);

ALTER TABLE cn_css_b OWNER TO nakamura;
GRANT ALL ON TABLE cn_css_b TO nakrole;

CREATE INDEX cn_css_b_rowkey ON cn_css_b (rid);


########### DROP TABLE au_css_b;


CREATE TABLE au_css_b
(
  id serial,
  rid character varying(32) NOT NULL,
  b bytea,
  CONSTRAINT au_css_b_pk PRIMARY KEY (id)
);

ALTER TABLE au_css_b OWNER TO nakamura;
GRANT ALL ON TABLE au_css_b TO nakrole;

CREATE INDEX au_css_b_rowkey ON au_css_b (rid);



########### DROP TABLE ac_css_b;


CREATE TABLE ac_css_b
(
  id serial,
  rid character varying(32) NOT NULL,
  b bytea,
  CONSTRAINT ac_css_b_pk PRIMARY KEY (id)
);

ALTER TABLE ac_css_b OWNER TO nakamura;
GRANT ALL ON TABLE ac_css_b TO nakrole;

CREATE INDEX ac_css_b_rowkey ON ac_css_b (rid);


