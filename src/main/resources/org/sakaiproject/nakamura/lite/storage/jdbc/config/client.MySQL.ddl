
DROP TABLE IF EXISTS `css`;

CREATE TABLE  `css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `au_css`;

CREATE TABLE  `au_css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cn_css`;

CREATE TABLE  `cn_css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `ac_css`;

CREATE TABLE  `ac_css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(64) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



