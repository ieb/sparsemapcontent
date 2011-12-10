#!/bin/sh

mysql -f -v -u root << EOSQL
drop database sakai22;
create database sakai22  default character set utf8;
grant all on sakai22.* to sakai22@'127.0.0.1' identified by 'sakai22';
grant all on sakai22.* to sakai22@'localhost' identified by 'sakai22';
exit
EOSQL


