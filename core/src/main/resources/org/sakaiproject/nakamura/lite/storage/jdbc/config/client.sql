#
# This SQL file is here for reference, and will only be used if a specific file for the DB type is not available.
# The finder SQL in this file is unlikely to function correctly for the database in question as paging is non standard
# for SQL. 
#

delete-string-row = delete from css where rid = ?
delete-string-row.n.au = delete from au_css where rid = ?
delete-string-row.n.ac = delete from ac_css where rid = ?
delete-string-row.n.cn = delete from cn_css where rid = ?
select-string-row = select cid, v from css where rid = ?
select-string-row.n.au = select cid, v from au_css where rid = ?
select-string-row.n.ac = select cid, v from ac_css where rid = ?
select-string-row.n.cn = select cid, v from cn_css where rid = ?
insert-string-column = insert into css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.au = insert into au_css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.ac = insert into ac_css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.cn = insert into cn_css ( v, rid, cid) values ( ?, ?, ? )
update-string-column = update css set v = ?  where rid = ? and cid = ?
update-string-column.n.au = update au_css set v = ?  where rid = ? and cid = ?
update-string-column.n.ac = update ac_css set v = ?  where rid = ? and cid = ?
update-string-column.n.cn = update cn_css set v = ?  where rid = ? and cid = ?
remove-string-column = delete from css where rid = ? and cid = ?
remove-string-column.n.au = delete from au_css where rid = ? and cid = ?
remove-string-column.n.ac = delete from ac_css where rid = ? and cid = ?
remove-string-column.n.cn = delete from cn_css where rid = ? and cid = ?
check-schema = select count(*) from css

# 0: select
# 1: table join
# 2: where clause
# 3: where clause for sort field (if needed)
# 4: order by clause
find.n.au = select a.rid, a.cid, a.v from au_css a {0} where {1} 1 = 1 ;, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.ac = select a.rid, a.cid, a.v from ac_css a {0} where {1} 1 = 1 ;, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.cn = select a.rid, a.cid, a.v from cn_css a {0} where {1} 1 = 1 ;, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
validate = values(1)
rowid-hash = SHA1


select-index-columns = select cid from index_cols


block-select-row = select b from css_b where rid = ?
block-delete-row = delete from css_b where rid = ?
block-insert-row = insert into css_b (rid,b) values (?, ?)
block-update-row = update css_b set b = ? where rid = ?
list-all = select rid, b from css_b

block-select-row.n.au = select b from au_css_b where rid = ?
block-delete-row.n.au = delete from au_css_b where rid = ?
block-insert-row.n.au = insert into au_css_b (rid,b) values (?, ?)
block-update-row.n.au = update au_css_b set b = ? where rid = ?
list-all.n.au = select rid, b from au_css_b

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

# 0: base statement
# 1: table join
# 2: where clause
# 3: where clause for sort field (if needed)
# 4: order by clause
block-find = select distinct a.rid from css a {0} where {1} 1 = 1;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
block-find.n.au = select distinct a.rid from au_css a {0} where {1} 1 = 1;, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
block-find.n.ac = select distinct a.rid from ac_css a {0} where {1} 1 = 1;, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
block-find.n.cn = select distinct a.rid from cn_css a {0} where {1} 1 = 1;, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}

use-batch-inserts = 0

# Queries that take longer than these times to execute will be logged with warn and error respectively.
# Logging is performed against org.sakaiproject.nakamura.lite.storage.spi.jdbc.JDBCStorageClient.SlowQueryLogger
slow-query-time = 50
very-slow-query-time = 100

