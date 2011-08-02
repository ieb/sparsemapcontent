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
insert-string-column = insert into css (id, v, rid, cid) values (seq_css_id.NEXTVAL, ?, ?, ? )
insert-string-column.n.ac = insert into ac_css (id, v, rid, cid) values (seq_ac_css_id.NEXTVAL, ?, ?, ? )
insert-string-column.n.au = insert into au_css (id, v, rid, cid) values (seq_au_css_id.NEXTVAL, ?, ?, ? )
insert-string-column.n.cn = insert into cn_css (id, v, rid, cid) values (seq_cn_css_id.NEXTVAL, ?, ?, ? )
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
find.n.au = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROWNUM rnum from au_css where {1} 1 = 1 {2}) TR where rnum > {4,number,#} and rnum <= {3,number,#}+{4,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.ac = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROWNUM rnum from ac_css where {1} 1 = 1 {2}) TR where rnum > {4,number,#} and rnum <= {3,number,#}+{4,number,#};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.cn = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROWNUM rnum from cn_css where {1} 1 = 1 {2}) TR where rnum > {4,number,#} and rnum <= {3,number,#}+{4,number,#};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}

block-select-row = select b from css_b where rid = ?
block-delete-row = delete from css_b where rid = ?
block-insert-row = insert into css_b (rid,b) values (?, ?)
block-update-row = update css_b set b = ? where rid = ?

block-select-row.n.ac = select b from ac_css_b where rid = ?
block-delete-row.n.ac = delete from ac_css_b where rid = ?
block-insert-row.n.ac = insert into ac_css_b (rid,b) values (?, ?)
block-update-row.n.ac = update ac_css_b set b = ? where rid = ?

block-select-row.n.cn = select b from cn_css_b where rid = ?
block-delete-row.n.cn = delete from cn_css_b where rid = ?
block-insert-row.n.cn = insert into cn_css_b (rid,b) values (?, ?)
block-update-row.n.cn = update cn_css_b set b = ? where rid = ?

block-select-row.n.au = select b from au_css_b where rid = ?
block-delete-row.n.au = delete from au_css_b where rid = ?
block-insert-row.n.au = insert into au_css_b (rid,b) values (?, ?)
block-update-row.n.au = update au_css_b set b = ? where rid = ?

# base statement with paging ; table join ; where clause ; where clause for sort field (if needed) ; order by clause; sort field column( if needed)
## the subselect in the paging statement is required by Oracle to do paging. http://www.oracle.com/technetwork/issue-archive/2006/06-sep/o56asktom-086197.html
block-find = select TR.rid from ( select s.rid, ROWNUM rnum from (select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2}) s where ROWNUM <= {3,number,#}+{4,number,#}) TR where rnum  >= {4,number,#};, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
block-find.n.au = select TR.rid from ( select s.rid, ROWNUM rnum from (select distinct a.rid {5} from au_css a {0} where {1} 1 = 1 {2}) s where ROWNUM <= {3,number,#}+{4,number,#}) TR where rnum  > {4,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
block-find.n.cn = select TR.rid from ( select s.rid, ROWNUM rnum from (select distinct a.rid {5} from cn_css a {0} where {1} 1 = 1 {2}) s where ROWNUM <= {3,number,#}+{4,number,#}) TR where rnum  > {4,number,#};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
block-find.n.ac = select TR.rid from ( select s.rid, ROWNUM rnum from (select distinct a.rid {5} from ac_css a {0} where {1} 1 = 1 {2}) s where ROWNUM <= {3,number,#}+{4,number,#}) TR where rnum  > {4,number,#};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v

listchildren = select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2};, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
listchildren.n.au = select distinct a.rid {5} from au_css a {0} where {1} 1 = 1 {2};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
listchildren.n.cn = select distinct a.rid {5} from cn_css a {0} where {1} 1 = 1 {2};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
listchildren.n.ac = select distinct a.rid {5} from ac_css a {0} where {1} 1 = 1 {2};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v

countestimate = select count(*) from (select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2}) as tocount;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
countestimate.n.au = select count(*) from (select distinct a.rid {5} from au_css a {0} where {1} 1 = 1 {2}) as tocount;, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
countestimate.n.cn = select count(*) from (select distinct a.rid {5} from cn_css a {0} where {1} 1 = 1 {2}) as tocount;, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
countestimate.n.ac = select count(*) from (select distinct a.rid {5} from ac_css a {0} where {1} 1 = 1 {2}) as tocount;, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v


# statement to validate the connection
validate = select 1 from DUAL

# What type of rowID has should be used. Must be non colliding (reasonable probability), cant be changed once set without data migration.
# SHA-1 has a 1:10E14 probability of collision, so IMVHO is Ok here. Do not use MD5, it will collide.
rowid-hash = SHA1

# statement to check that the schema exists
check-schema = select count(*) from css

# Use batch Inserts means that update operations will be performed as batches rather than single SQL statements. This only really effects the update of
# Index tables and not the content store but it will reduce the number of SQL operations where more than one field is indexed per content item.
use-batch-inserts = 1

# this property indicates which version of the JRE your JDBC driver targets
# e.g. the driver for Oracle 10g does not support JDBC methods introduced in JRE 1.6
jdbc-support-level = 1.5
