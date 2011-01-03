# If using mySQL 5.1 you can use innodb_autoinc_lock_mode=1 and have an  an autoinc PK.
# Having and autoink PK in 5.0 and earlier will lead to table serialization as the key generation requires a full table lock which is why we have no
# PK in these tables
# The access mechanism must be update then insert to allow no PK and no Unique key.
# Please read http://harrison-fisk.blogspot.com/2009/02/my-favorite-new-feature-of-mysql-51.html for info.

DROP TABLE IF EXISTS `css`;

CREATE TABLE  `css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `au_css`;

CREATE TABLE  `au_css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cn_css`;

CREATE TABLE  `cn_css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `ac_css`;

CREATE TABLE  `ac_css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



