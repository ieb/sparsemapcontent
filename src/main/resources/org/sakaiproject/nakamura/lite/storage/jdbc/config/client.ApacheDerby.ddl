
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

CREATE TABLE css_w (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  v0 varchar(780) NOT NULL,
  v1 varchar(780) NOT NULL,
  v2 varchar(780) NOT NULL,
  v3 varchar(780) NOT NULL,
  v4 varchar(780) NOT NULL,
  v5 varchar(780) NOT NULL,
  v6 varchar(780) NOT NULL,
  v7 varchar(780) NOT NULL,
  v8 varchar(780) NOT NULL,
  v9 varchar(780) NOT NULL,
  v10 varchar(780) NOT NULL,
  v11 varchar(780) NOT NULL,
  v12 varchar(780) NOT NULL,
  v13 varchar(780) NOT NULL,
  v14 varchar(780) NOT NULL,
  v15 varchar(780) NOT NULL,
  v16 varchar(780) NOT NULL,
  v17 varchar(780) NOT NULL,
  v18 varchar(780) NOT NULL,
  v19 varchar(780) NOT NULL,
  v20 varchar(780) NOT NULL,
  v21 varchar(780) NOT NULL,
  v22 varchar(780) NOT NULL,
  v23 varchar(780) NOT NULL,
  v24 varchar(780) NOT NULL,
  v25 varchar(780) NOT NULL,
  v26 varchar(780) NOT NULL,
  v27 varchar(780) NOT NULL,
  v28 varchar(780) NOT NULL,
  v29 varchar(780) NOT NULL,
  primary key(id));
CREATE UNIQUE INDEX css_w_rid ON css (rid);
CREATE INDEX css_w_v0 ON css_w (v0);
CREATE INDEX css_w_v1 ON css_w (v1);
CREATE INDEX css_w_v2 ON css_w (v2);
CREATE INDEX css_w_v3 ON css_w (v3);
CREATE INDEX css_w_v4 ON css_w (v4);
CREATE INDEX css_w_v5 ON css_w (v5);
CREATE INDEX css_w_v6 ON css_w (v6);
CREATE INDEX css_w_v7 ON css_w (v7);
CREATE INDEX css_w_v8 ON css_w (v8);
CREATE INDEX css_w_v9 ON css_w (v9);
CREATE INDEX css_w_v10 ON css_w (v10);
CREATE INDEX css_w_v11 ON css_w (v11);
CREATE INDEX css_w_v12 ON css_w (v12);
CREATE INDEX css_w_v13 ON css_w (v13);
CREATE INDEX css_w_v14 ON css_w (v14);
CREATE INDEX css_w_v15 ON css_w (v15);
CREATE INDEX css_w_v16 ON css_w (v16);
CREATE INDEX css_w_v17 ON css_w (v17);
CREATE INDEX css_w_v18 ON css_w (v18);
CREATE INDEX css_w_v19 ON css_w (v19);
CREATE INDEX css_w_v20 ON css_w (v20);
CREATE INDEX css_w_v21 ON css_w (v21);
CREATE INDEX css_w_v22 ON css_w (v22);
CREATE INDEX css_w_v23 ON css_w (v23);
CREATE INDEX css_w_v24 ON css_w (v24);
CREATE INDEX css_w_v25 ON css_w (v25);
CREATE INDEX css_w_v26 ON css_w (v26);
CREATE INDEX css_w_v27 ON css_w (v27);
CREATE INDEX css_w_v28 ON css_w (v28);
CREATE INDEX css_w_v29 ON css_w (v29);
  
CREATE TABLE ac_css_w (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  v0 varchar(780) NOT NULL,
  v1 varchar(780) NOT NULL,
  v2 varchar(780) NOT NULL,
  v3 varchar(780) NOT NULL,
  v4 varchar(780) NOT NULL,
  v5 varchar(780) NOT NULL,
  v6 varchar(780) NOT NULL,
  v7 varchar(780) NOT NULL,
  v8 varchar(780) NOT NULL,
  v9 varchar(780) NOT NULL,
  v10 varchar(780) NOT NULL,
  v11 varchar(780) NOT NULL,
  v12 varchar(780) NOT NULL,
  v13 varchar(780) NOT NULL,
  v14 varchar(780) NOT NULL,
  v15 varchar(780) NOT NULL,
  v16 varchar(780) NOT NULL,
  v17 varchar(780) NOT NULL,
  v18 varchar(780) NOT NULL,
  v19 varchar(780) NOT NULL,
  v20 varchar(780) NOT NULL,
  v21 varchar(780) NOT NULL,
  v22 varchar(780) NOT NULL,
  v23 varchar(780) NOT NULL,
  v24 varchar(780) NOT NULL,
  v25 varchar(780) NOT NULL,
  v26 varchar(780) NOT NULL,
  v27 varchar(780) NOT NULL,
  v28 varchar(780) NOT NULL,
  v29 varchar(780) NOT NULL,
  primary key(id));
CREATE UNIQUE INDEX ac_css_w_rid ON css (rid);
CREATE INDEX ac_css_w_v0 ON ac_css_w (v0);
CREATE INDEX ac_css_w_v1 ON ac_css_w (v1);
CREATE INDEX ac_css_w_v2 ON ac_css_w (v2);
CREATE INDEX ac_css_w_v3 ON ac_css_w (v3);
CREATE INDEX ac_css_w_v4 ON ac_css_w (v4);
CREATE INDEX ac_css_w_v5 ON ac_css_w (v5);
CREATE INDEX ac_css_w_v6 ON ac_css_w (v6);
CREATE INDEX ac_css_w_v7 ON ac_css_w (v7);
CREATE INDEX ac_css_w_v8 ON ac_css_w (v8);
CREATE INDEX ac_css_w_v9 ON ac_css_w (v9);
CREATE INDEX ac_css_w_v10 ON ac_css_w (v10);
CREATE INDEX ac_css_w_v11 ON ac_css_w (v11);
CREATE INDEX ac_css_w_v12 ON ac_css_w (v12);
CREATE INDEX ac_css_w_v13 ON ac_css_w (v13);
CREATE INDEX ac_css_w_v14 ON ac_css_w (v14);
CREATE INDEX ac_css_w_v15 ON ac_css_w (v15);
CREATE INDEX ac_css_w_v16 ON ac_css_w (v16);
CREATE INDEX ac_css_w_v17 ON ac_css_w (v17);
CREATE INDEX ac_css_w_v18 ON ac_css_w (v18);
CREATE INDEX ac_css_w_v19 ON ac_css_w (v19);
CREATE INDEX ac_css_w_v20 ON ac_css_w (v20);
CREATE INDEX ac_css_w_v21 ON ac_css_w (v21);
CREATE INDEX ac_css_w_v22 ON ac_css_w (v22);
CREATE INDEX ac_css_w_v23 ON ac_css_w (v23);
CREATE INDEX ac_css_w_v24 ON ac_css_w (v24);
CREATE INDEX ac_css_w_v25 ON ac_css_w (v25);
CREATE INDEX ac_css_w_v26 ON ac_css_w (v26);
CREATE INDEX ac_css_w_v27 ON ac_css_w (v27);
CREATE INDEX ac_css_w_v28 ON ac_css_w (v28);
CREATE INDEX ac_css_w_v29 ON ac_css_w (v29);
  
  
CREATE TABLE au_css_w (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  v0 varchar(780) NOT NULL,
  v1 varchar(780) NOT NULL,
  v2 varchar(780) NOT NULL,
  v3 varchar(780) NOT NULL,
  v4 varchar(780) NOT NULL,
  v5 varchar(780) NOT NULL,
  v6 varchar(780) NOT NULL,
  v7 varchar(780) NOT NULL,
  v8 varchar(780) NOT NULL,
  v9 varchar(780) NOT NULL,
  v10 varchar(780) NOT NULL,
  v11 varchar(780) NOT NULL,
  v12 varchar(780) NOT NULL,
  v13 varchar(780) NOT NULL,
  v14 varchar(780) NOT NULL,
  v15 varchar(780) NOT NULL,
  v16 varchar(780) NOT NULL,
  v17 varchar(780) NOT NULL,
  v18 varchar(780) NOT NULL,
  v19 varchar(780) NOT NULL,
  v20 varchar(780) NOT NULL,
  v21 varchar(780) NOT NULL,
  v22 varchar(780) NOT NULL,
  v23 varchar(780) NOT NULL,
  v24 varchar(780) NOT NULL,
  v25 varchar(780) NOT NULL,
  v26 varchar(780) NOT NULL,
  v27 varchar(780) NOT NULL,
  v28 varchar(780) NOT NULL,
  v29 varchar(780) NOT NULL,
  primary key(id));
CREATE UNIQUE INDEX au_css_w_rid ON css (rid);
CREATE INDEX au_css_w_v0 ON au_css_w (v0);
CREATE INDEX au_css_w_v1 ON au_css_w (v1);
CREATE INDEX au_css_w_v2 ON au_css_w (v2);
CREATE INDEX au_css_w_v3 ON au_css_w (v3);
CREATE INDEX au_css_w_v4 ON au_css_w (v4);
CREATE INDEX au_css_w_v5 ON au_css_w (v5);
CREATE INDEX au_css_w_v6 ON au_css_w (v6);
CREATE INDEX au_css_w_v7 ON au_css_w (v7);
CREATE INDEX au_css_w_v8 ON au_css_w (v8);
CREATE INDEX au_css_w_v9 ON au_css_w (v9);
CREATE INDEX au_css_w_v10 ON au_css_w (v10);
CREATE INDEX au_css_w_v11 ON au_css_w (v11);
CREATE INDEX au_css_w_v12 ON au_css_w (v12);
CREATE INDEX au_css_w_v13 ON au_css_w (v13);
CREATE INDEX au_css_w_v14 ON au_css_w (v14);
CREATE INDEX au_css_w_v15 ON au_css_w (v15);
CREATE INDEX au_css_w_v16 ON au_css_w (v16);
CREATE INDEX au_css_w_v17 ON au_css_w (v17);
CREATE INDEX au_css_w_v18 ON au_css_w (v18);
CREATE INDEX au_css_w_v19 ON au_css_w (v19);
CREATE INDEX au_css_w_v20 ON au_css_w (v20);
CREATE INDEX au_css_w_v21 ON au_css_w (v21);
CREATE INDEX au_css_w_v22 ON au_css_w (v22);
CREATE INDEX au_css_w_v23 ON au_css_w (v23);
CREATE INDEX au_css_w_v24 ON au_css_w (v24);
CREATE INDEX au_css_w_v25 ON au_css_w (v25);
CREATE INDEX au_css_w_v26 ON au_css_w (v26);
CREATE INDEX au_css_w_v27 ON au_css_w (v27);
CREATE INDEX au_css_w_v28 ON au_css_w (v28);
CREATE INDEX au_css_w_v29 ON au_css_w (v29);


CREATE TABLE cn_css_w (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  rid varchar(32) NOT NULL,
  v0 varchar(780) NOT NULL,
  v1 varchar(780) NOT NULL,
  v2 varchar(780) NOT NULL,
  v3 varchar(780) NOT NULL,
  v4 varchar(780) NOT NULL,
  v5 varchar(780) NOT NULL,
  v6 varchar(780) NOT NULL,
  v7 varchar(780) NOT NULL,
  v8 varchar(780) NOT NULL,
  v9 varchar(780) NOT NULL,
  v10 varchar(780) NOT NULL,
  v11 varchar(780) NOT NULL,
  v12 varchar(780) NOT NULL,
  v13 varchar(780) NOT NULL,
  v14 varchar(780) NOT NULL,
  v15 varchar(780) NOT NULL,
  v16 varchar(780) NOT NULL,
  v17 varchar(780) NOT NULL,
  v18 varchar(780) NOT NULL,
  v19 varchar(780) NOT NULL,
  v20 varchar(780) NOT NULL,
  v21 varchar(780) NOT NULL,
  v22 varchar(780) NOT NULL,
  v23 varchar(780) NOT NULL,
  v24 varchar(780) NOT NULL,
  v25 varchar(780) NOT NULL,
  v26 varchar(780) NOT NULL,
  v27 varchar(780) NOT NULL,
  v28 varchar(780) NOT NULL,
  v29 varchar(780) NOT NULL,
  primary key(id));
CREATE UNIQUE INDEX cn_css_w_rid ON css (rid);
CREATE INDEX cn_css_w_v0 ON cn_css_w (v0);
CREATE INDEX cn_css_w_v1 ON cn_css_w (v1);
CREATE INDEX cn_css_w_v2 ON cn_css_w (v2);
CREATE INDEX cn_css_w_v3 ON cn_css_w (v3);
CREATE INDEX cn_css_w_v4 ON cn_css_w (v4);
CREATE INDEX cn_css_w_v5 ON cn_css_w (v5);
CREATE INDEX cn_css_w_v6 ON cn_css_w (v6);
CREATE INDEX cn_css_w_v7 ON cn_css_w (v7);
CREATE INDEX cn_css_w_v8 ON cn_css_w (v8);
CREATE INDEX cn_css_w_v9 ON cn_css_w (v9);
CREATE INDEX cn_css_w_v10 ON cn_css_w (v10);
CREATE INDEX cn_css_w_v11 ON cn_css_w (v11);
CREATE INDEX cn_css_w_v12 ON cn_css_w (v12);
CREATE INDEX cn_css_w_v13 ON cn_css_w (v13);
CREATE INDEX cn_css_w_v14 ON cn_css_w (v14);
CREATE INDEX cn_css_w_v15 ON cn_css_w (v15);
CREATE INDEX cn_css_w_v16 ON cn_css_w (v16);
CREATE INDEX cn_css_w_v17 ON cn_css_w (v17);
CREATE INDEX cn_css_w_v18 ON cn_css_w (v18);
CREATE INDEX cn_css_w_v19 ON cn_css_w (v19);
CREATE INDEX cn_css_w_v20 ON cn_css_w (v20);
CREATE INDEX cn_css_w_v21 ON cn_css_w (v21);
CREATE INDEX cn_css_w_v22 ON cn_css_w (v22);
CREATE INDEX cn_css_w_v23 ON cn_css_w (v23);
CREATE INDEX cn_css_w_v24 ON cn_css_w (v24);
CREATE INDEX cn_css_w_v25 ON cn_css_w (v25);
CREATE INDEX cn_css_w_v26 ON cn_css_w (v26);
CREATE INDEX cn_css_w_v27 ON cn_css_w (v27);
CREATE INDEX cn_css_w_v28 ON cn_css_w (v28);
CREATE INDEX cn_css_w_v29 ON cn_css_w (v29);


CREATE TABLE  css_wr (
  id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  cf varchar(32) NOT NULL,
  cid varchar(64) NOT NULL,
  cname char(3) NOT NULL,
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

  


