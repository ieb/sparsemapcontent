# If using mySQL 5.1 you can use innodb_autoinc_lock_mode=1 and have an  an autoinc PK.
# Having and autoink PK in 5.0 and earlier will lead to table serialization as the key generation requires a full table lock which is why we have no
# PK in these tables
# The access mechanism must be update then insert to allow no PK and no Unique key.
# Please read http://harrison-fisk.blogspot.com/2009/02/my-favorite-new-feature-of-mysql-51.html for info.
#
# Please read for proper UTF-8 encoding support:
# http://rentzsch.tumblr.com/post/9133498042/howto-use-utf-8-throughout-your-web-stack

#### DROP TABLE IF EXISTS `css`;

CREATE TABLE  `css` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey`  (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


##### DROP TABLE IF EXISTS `au_css`;

CREATE TABLE  `au_css` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey`  (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

###### DROP TABLE IF EXISTS `cn_css`;

CREATE TABLE  `cn_css` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey`  (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


###### DROP TABLE IF EXISTS `ac_css`;

CREATE TABLE  `ac_css` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey`  (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

###### DROP TABLE IF EXISTS `lk_css`;

CREATE TABLE  `lk_css` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey`  (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE css_w (
  `rid` varchar(32) NOT NULL,
  primary key(`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE ac_css_w (
  `rid` varchar(32) NOT NULL,
  primary key(`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE au_css_w (
  `rid` varchar(32) NOT NULL,
  primary key(`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE cn_css_w (
  `rid` varchar(32) NOT NULL,
  primary key(`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE lk_css_w (
  `rid` varchar(32) NOT NULL,
  primary key(`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
 
CREATE TABLE  css_wr (
  `id` INT NOT NULL AUTO_INCREMENT,
  `cf` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `cname` varchar(64) NOT NULL,
  primary key(`id`),
  unique key css_r_cid (`cf`,`cid`),
  unique key css_r_cnam (`cf`,`cname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


# Body Store. In some cases we want to store the bodies of the objects in a binary serialized lump
# This allows us to load and save the sparse map without using multiple records in the above tables and hence is more compact
# And uses less bandwidth to the DB.
# Where this is done, we still index certain fields as defined in index_cols

CREATE TABLE  `css_b` (
  `rid` varchar(32) NOT NULL,
  `b` mediumblob,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

# Central Store for Object bodies, serialized content maps rather than columns
CREATE TABLE  `cn_css_b` (
  `rid` varchar(32) NOT NULL,
  `b` mediumblob,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

# Central Store for Object bodies, serialized content maps rather than columns
CREATE TABLE  `au_css_b` (
  `rid` varchar(32) NOT NULL,
  `b` mediumblob,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

# Central Store for Object bodies, serialized content maps rather than columns
CREATE TABLE  `ac_css_b` (
  `rid` varchar(32) NOT NULL,
  `b` mediumblob,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE  `lk_css_b` (
  `rid` varchar(32) NOT NULL,
  `b` mediumblob,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;



