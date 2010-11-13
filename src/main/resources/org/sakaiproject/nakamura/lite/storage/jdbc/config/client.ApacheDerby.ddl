
CREATE TABLE css ( 
  id INT NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 0 ,INCREMENT BY 1 ),
  rid varchar(32) NOT NULL,
  cid varchar(20) NOT NULL,
  v varchar(780) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (rid,cid));



CREATE TABLE  csb (
  id INT NOT NULL GENERATED ALWAYS AS IDENTITY ( START WITH 0 ,INCREMENT BY 1 ),
  rid varchar(32) NOT NULL,
  cid varchar(20) NOT NULL,
  v blob,
  PRIMARY KEY (id),
  UNIQUE (rid,cid));

CREATE INDEX css_locate_idx ON css (v, cid);