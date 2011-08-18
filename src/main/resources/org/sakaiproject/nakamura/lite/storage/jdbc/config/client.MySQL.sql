# SQL statements of the form key[.keyspace.columnfamily.[rowID0-2]]
# the based key should always be present
# the keyspace.columnfamily selectors are used to shard the column family (optional)
# the rowID0-2 is to shard on rowID, you can selectively shard hot rowID areas.
# If sharding ensure that any exiting data is migrated (using SQL DML) and that the finder statements are adjusted to incorporate the shards (warning, might be hard)
# Indexer statements
delete-string-row = delete from css where rid = ?
delete-string-row.n.ac = delete from ac_css where rid = ?
delete-string-row.n.au = delete from au_css where rid = ?
delete-string-row.n.cn = delete from cn_css where rid = ?
select-string-row = select cid, v from css where rid = ?
select-string-row.n.ac = select cid, v from ac_css where rid = ?
select-string-row.n.au = select cid, v from au_css where rid = ?
select-string-row.n.cn = select cid, v from cn_css where rid = ?
insert-string-column = insert into css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.ac = insert into ac_css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.au = insert into au_css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.cn = insert into cn_css ( v, rid, cid) values ( ?, ?, ? )
update-string-column = update css set v = ?  where rid = ? and cid = ?
update-string-column.n.ac = update ac_css set v = ?  where rid = ? and cid = ?
update-string-column.n.au = update au_css set v = ?  where rid = ? and cid = ?
update-string-column.n.cn = update cn_css set v = ?  where rid = ? and cid = ?
remove-string-column = delete from css where rid = ? and cid = ?
remove-string-column.n.ac = delete from ac_css where rid = ? and cid = ?
remove-string-column.n.au = delete from au_css where rid = ? and cid = ?
remove-string-column.n.cn = delete from cn_css where rid = ? and cid = ?
# Example of a sharded query, rowIDs starting with x will use this
### remove-string-column.n.cn._X = delete from cn_css_X where rid = ? and cid = ?

# base statement with paging ; table join ; where clause ; where clause for sort field (if needed) ; order by clause
find.n.au = select a.rid, a.cid, a.v from au_css a {0} where {1} 1 = 1 limit {2,number,#} offset {3,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}


block-select-row = select b from css_b where rid = ?
block-delete-row = delete from css_b where rid = ?
block-insert-row = insert into css_b (rid,b) values (?, ?)
block-update-row = update css_b set b = ? where rid = ?
list-all = select rid, b from css_b

block-select-row.n.ac = select b from ac_css_b where rid = ?
block-delete-row.n.ac = delete from ac_css_b where rid = ?
block-insert-row.n.ac = insert into ac_css_b (rid,b) values (?, ?)
block-update-row.n.ac = update ac_css_b set b = ? where rid = ?
list-all.n.ac = select rid, b from ac_css_b

block-select-row.n.cn = select b from cn_css_b where rid = ?
block-delete-row.n.cn = delete from cn_css_b where rid = ?
block-insert-row.n.cn = insert into cn_css_b (rid,b) values (?, ?)
block-update-row.n.cn = update cn_css_b set b = ? where rid = ?
list-all.n.cn = select rid, b from cn_css_b

block-select-row.n.au = select b from au_css_b where rid = ?
block-delete-row.n.au = delete from au_css_b where rid = ?
block-insert-row.n.au = insert into au_css_b (rid,b) values (?, ?)
block-update-row.n.au = update au_css_b set b = ? where rid = ?
list-all.n.au = select rid, b from au_css_b

#
# These are finder statements
# base statement with paging ; table join ; where clause ; where clause for sort field (if needed) ; order by clause
block-find = select distinct a.rid from css a {0} where {1} 1 = 1 {2} limit {3,number,#} offset {4,number,#};, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
block-find.n.au = select distinct a.rid from au_css a {0} where {1} 1 = 1 {2} limit {3,number,#} offset {4,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
block-find.n.cn = select distinct a.rid from cn_css a {0} where {1} 1 = 1 {2} limit {3,number,#} offset {4,number,#};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
block-find.n.ac = select distinct a.rid from ac_css a {0} where {1} 1 = 1 {2} limit {3,number,#} offset {4,number,#};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}

listchildren = select distinct a.rid from css a {0} where {1} 1 = 1 {2} ;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
listchildren.n.au = select distinct a.rid from au_css a {0} where {1} 1 = 1 {2} ;, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
listchildren.n.cn = select distinct a.rid from cn_css a {0} where {1} 1 = 1 {2} ;, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
listchildren.n.ac = select distinct a.rid from ac_css a {0} where {1} 1 = 1 {2} ;, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}

# This custom finder statement outputs 1 row which is the count of number of rows.
countestimate = select count(*) from (select distinct a.rid from css a {0} where {1} 1 = 1 {2}) as tocount ;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
countestimate.n.au = select count(*) from (select distinct a.rid from au_css a {0} where {1} 1 = 1 {2}) as tocount ;, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
countestimate.n.cn = select count(*) from (select distinct a.rid from cn_css a {0} where {1} 1 = 1 {2}) as tocount ;, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
countestimate.n.ac = select count(*) from (select distinct a.rid from ac_css a {0} where {1} 1 = 1 {2}) as tocount ;, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}

# statement to validate the connection
validate = select 1

# What type of rowID has should be used. Must be non colliding (reasonable probability), cant be changed once set without data migration.
# SHA-1 has a 1:10E14 probability of collision, so IMVHO is Ok here. Do not use MD5, it will collide.
rowid-hash = SHA1

# statement to check that the schema exists
check-schema = select count(*) from css

# Use batch Inserts means that update operations will be performed as batches rather than single SQL statements. This only really effects the update of 
# Index tables and not the content store but it will reduce the number of SQL operations where more than one field is indexed per content item.
use-batch-inserts = 1

# Queries that take longer than these times to execute will be logged with warn and error respectively.
# Logging is performed against org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClient.SlowQueryLogger
slow-query-time = 50
very-slow-query-time = 100

