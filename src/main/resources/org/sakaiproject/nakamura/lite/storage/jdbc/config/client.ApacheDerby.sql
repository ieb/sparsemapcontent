delete-string-row = delete from css where rid = ?
delete-string-row.n.au = delete from au_css where rid = ?
delete-string-row.n.ac = delete from ac_css where rid = ?
delete-string-row.n.cn = delete from cn_css where rid = ?
select-string-row = select cid, v from css where rid = ?
select-string-row.n.au = select cid, v from au_css where rid = ?
select-string-row.n.ac = select cid, v from ac_css where rid = ?
select-string-row.n.cn = select cid, v from cn_css where rid = ?
insert-string-column = insert into css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.au = insert into au_css ( v, rid, cid) values ( ?, ?, ?)
insert-string-column.n.ac = insert into ac_css ( v, rid, cid) values ( ?, ?, ?)
insert-string-column.n.cn = insert into cn_css ( v, rid, cid) values ( ?, ?, ?)
update-string-column = update css set v = ?  where rid = ? and cid = ?
update-string-column.n.au = update au_css set v = ?  where rid = ? and cid = ?
update-string-column.n.ac = update ac_css set v = ?  where rid = ? and cid = ?
update-string-column.n.cn = update cn_css set v = ?  where rid = ? and cid = ?
remove-string-column = delete from css where rid = ? and cid = ?
remove-string-column.n.au = delete from au_css where rid = ? and cid = ?
remove-string-column.n.ac = delete from ac_css where rid = ? and cid = ?
remove-string-column.n.cn = delete from cn_css where rid = ? and cid = ?
check-schema = select count(*) from css

# base statement with paging ; table join ; where clause ; where clause for sort field (if needed) ; order by clause
find.n.au = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROW_NUMBER() OVER () AS R from au_css a {0} where {1} 1 = 1 {2}) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.ac = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROW_NUMBER() OVER () AS R from ac_css a {0} where {1} 1 = 1 {2}) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.cn = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROW_NUMBER() OVER () AS R from cn_css a {0} where {1} 1 = 1 {2}) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
validate = values(1)
rowid-hash = SHA1



block-select-row = select b from css_b where rid = ?
block-delete-row = delete from css_b where rid = ?
block-insert-row = insert into css_b (rid,b) values (?, ?)
block-update-row = update css_b set b = ? where rid = ?

block-select-row.n.au = select b from au_css_b where rid = ?
block-delete-row.n.au = delete from au_css_b where rid = ?
block-insert-row.n.au = insert into au_css_b (rid,b) values (?, ?)
block-update-row.n.au = update au_css_b set b = ? where rid = ?

block-select-row.n.ac = select b from ac_css_b where rid = ?
block-delete-row.n.ac = delete from ac_css_b where rid = ?
block-insert-row.n.ac = insert into ac_css_b (rid,b) values (?, ?)
block-update-row.n.ac = update ac_css_b set b = ? where rid = ?

block-select-row.n.cn = select b from cn_css_b where rid = ?
block-delete-row.n.cn = delete from cn_css_b where rid = ?
block-insert-row.n.cn = insert into cn_css_b (rid,b) values (?, ?)
block-update-row.n.cn = update cn_css_b set b = ? where rid = ?

# base statement with paging ; table join ; where clause ; where clause for sort field (if needed) ; order by clause; sort field column( if needed)
## the subselect in the paging statement is required by Derby to do paging. http://db.apache.org/derby/docs/10.6/ref/rreffuncrownumber.html
block-find = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
block-find.n.au = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid  {5} from au_css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
block-find.n.ac = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid  {5} from ac_css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
block-find.n.cn = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid  {5} from cn_css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v

# Optimized queries to find children
listchildren = select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2};, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
listchildren.n.au = select distinct a.rid  {5} from au_css a {0} where {1} 1 = 1 {2};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
listchildren.n.ac = select distinct a.rid  {5} from ac_css a {0} where {1} 1 = 1 {2};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
listchildren.n.cn = select distinct a.rid  {5} from cn_css a {0} where {1} 1 = 1 {2};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v

# Optimized queries estimate the count of any query.
countestimate = select count(*) from (select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2}) as tocount;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
countestimate.n.au = select count(*) from (select distinct a.rid  {5} from au_css a {0} where {1} 1 = 1 {2}) as tocount;, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
countestimate.n.ac = select count(*) from (select distinct a.rid  {5} from ac_css a {0} where {1} 1 = 1 {2}) as tocount;, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
countestimate.n.cn = select count(*) from (select distinct a.rid  {5} from cn_css a {0} where {1} 1 = 1 {2}) as tocount;, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v

use-batch-inserts = 0

# Queries that take longer than these times to execute will be logged with warn and error respectively.
# Logging is performed against org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClient.SlowQueryLogger
slow-query-time = 50
very-slow-query-time = 100
