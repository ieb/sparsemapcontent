
CREATE TABLE  `css` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(20) NOT NULL,
  `v` varchar(780) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`v`(255),`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE  `csb` (
  `id` int(11) NOT NULL auto_increment,
  `rid` varchar(32) NOT NULL,
  `cid` varchar(20) NOT NULL,
  `v` longblob,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `rowkey` USING BTREE (`rid`,`cid`),
  KEY `cid_locate_i` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


