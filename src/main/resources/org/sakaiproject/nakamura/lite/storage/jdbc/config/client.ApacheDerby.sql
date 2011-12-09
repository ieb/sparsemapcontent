delete-string-row = delete from css where rid = ?
delete-string-row.n.au = delete from au_css where rid = ?
delete-string-row.n.ac = delete from ac_css where rid = ?
delete-string-row.n.cn = delete from cn_css where rid = ?
delete-string-row.n.lk = delete from lk_css where rid = ?
select-string-row = select cid, v from css where rid = ?
select-string-row.n.au = select cid, v from au_css where rid = ?
select-string-row.n.ac = select cid, v from ac_css where rid = ?
select-string-row.n.cn = select cid, v from cn_css where rid = ?
select-string-row.n.lk = select cid, v from lk_css where rid = ?
insert-string-column = insert into css ( v, rid, cid) values ( ?, ?, ? )
insert-string-column.n.au = insert into au_css ( v, rid, cid) values ( ?, ?, ?)
insert-string-column.n.ac = insert into ac_css ( v, rid, cid) values ( ?, ?, ?)
insert-string-column.n.cn = insert into cn_css ( v, rid, cid) values ( ?, ?, ?)
insert-string-column.n.lk = insert into lk_css ( v, rid, cid) values ( ?, ?, ?)
update-string-column = update css set v = ?  where rid = ? and cid = ?
update-string-column.n.au = update au_css set v = ?  where rid = ? and cid = ?
update-string-column.n.ac = update ac_css set v = ?  where rid = ? and cid = ?
update-string-column.n.cn = update cn_css set v = ?  where rid = ? and cid = ?
update-string-column.n.lk = update lk_css set v = ?  where rid = ? and cid = ?
remove-string-column = delete from css where rid = ? and cid = ?
remove-string-column.n.au = delete from au_css where rid = ? and cid = ?
remove-string-column.n.ac = delete from ac_css where rid = ? and cid = ?
remove-string-column.n.cn = delete from cn_css where rid = ? and cid = ?
remove-string-column.n.lk = delete from lk_css where rid = ? and cid = ?
check-schema = select count(*) from css

# base statement with paging ; table join ; where clause ; where clause for sort field (if needed) ; order by clause
find.n.au = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROW_NUMBER() OVER () AS R from au_css a {0} where {1} 1 = 1 {2}) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.ac = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROW_NUMBER() OVER () AS R from ac_css a {0} where {1} 1 = 1 {2}) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.cn = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROW_NUMBER() OVER () AS R from cn_css a {0} where {1} 1 = 1 {2}) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
find.n.lk = select TR.rid, TR.cid, TR.v from (select a.rid, a.cid, a.v, ROW_NUMBER() OVER () AS R from lk_css a {0} where {1} 1 = 1 {2}) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, lk_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1}
validate = values(1)
rowid-hash = SHA1



block-select-row = select b from css_b where rid = ?
block-delete-row = delete from css_b where rid = ?
block-insert-row = insert into css_b (rid,b) values (?, ?)
block-update-row = update css_b set b = ? where rid = ?
list-all = select rid, b from css_b
list-all-count = select count(*) from css_b

block-select-row.n.au = select b from au_css_b where rid = ?
block-delete-row.n.au = delete from au_css_b where rid = ?
block-insert-row.n.au = insert into au_css_b (rid,b) values (?, ?)
block-update-row.n.au = update au_css_b set b = ? where rid = ?
list-all.n.au = select rid, b from au_css_b
list-all-count.n.au = select count(*) from au_css_b

block-select-row.n.ac = select b from ac_css_b where rid = ?
block-delete-row.n.ac = delete from ac_css_b where rid = ?
block-insert-row.n.ac = insert into ac_css_b (rid,b) values (?, ?)
block-update-row.n.ac = update ac_css_b set b = ? where rid = ?
list-all.n.ac = select rid, b from ac_css_b
list-all-count.n.ac = select count(*) from ac_css_b

block-select-row.n.cn = select b from cn_css_b where rid = ?
block-delete-row.n.cn = delete from cn_css_b where rid = ?
block-insert-row.n.cn = insert into cn_css_b (rid,b) values (?, ?)
block-update-row.n.cn = update cn_css_b set b = ? where rid = ?
list-all.n.cn = select rid, b from cn_css_b
list-all-count.n.cn = select count(*) from cn_css_b

block-select-row.n.lk = select b from lk_css_b where rid = ?
block-delete-row.n.lk = delete from lk_css_b where rid = ?
block-insert-row.n.lk = insert into lk_css_b (rid,b) values (?, ?)
block-update-row.n.lk = update lk_css_b set b = ? where rid = ?
list-all.n.lk = select rid, b from lk_css_b
list-all-count.n.lk = select count(*) from lk_css_b

# base statement with paging ; table join ; where clause ; where clause for sort field (if needed) ; order by clause; sort field column( if needed)
## the subselect in the paging statement is required by Derby to do paging. http://db.apache.org/derby/docs/10.6/ref/rreffuncrownumber.html
block-find = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
block-find.n.au = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid  {5} from au_css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
block-find.n.ac = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid  {5} from ac_css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
block-find.n.cn = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid  {5} from cn_css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
block-find.n.lk = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select distinct a.rid  {5} from lk_css a {0} where {1} 1 = 1 {2}) as s) as TR where TR.R > {4,number,#} and TR.R <= {3,number,#}+{4,number,#};, lk_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v

# Optimized queries to find children
listchildren = select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2};, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
listchildren.n.au = select distinct a.rid  {5} from au_css a {0} where {1} 1 = 1 {2};, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
listchildren.n.ac = select distinct a.rid  {5} from ac_css a {0} where {1} 1 = 1 {2};, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
listchildren.n.cn = select distinct a.rid  {5} from cn_css a {0} where {1} 1 = 1 {2};, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
listchildren.n.lk = select distinct a.rid  {5} from lk_css a {0} where {1} 1 = 1 {2};, lk_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v

# Optimized queries estimate the count of any query.
countestimate = select count(*) from (select distinct a.rid {5} from css a {0} where {1} 1 = 1 {2}) as tocount;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1} ;, {0}.v
countestimate.n.au = select count(*) from (select distinct a.rid  {5} from au_css a {0} where {1} 1 = 1 {2}) as tocount;, au_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
countestimate.n.ac = select count(*) from (select distinct a.rid  {5} from ac_css a {0} where {1} 1 = 1 {2}) as tocount;, ac_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
countestimate.n.cn = select count(*) from (select distinct a.rid  {5} from cn_css a {0} where {1} 1 = 1 {2}) as tocount;, cn_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v
countestimate.n.lk = select count(*) from (select distinct a.rid  {5} from lk_css a {0} where {1} 1 = 1 {2}) as tocount;, lk_css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid ; {0}.cid = ? and {0}.rid = a.rid ; order by {0}.v {1};, {0}.v


use-batch-inserts = 0

# Queries that take longer than these times to execute will be logged with warn and error respectively.
# Logging is performed against org.sakaiproject.nakamura.lite.storage.spi.jdbc.JDBCStorageClient.SlowQueryLogger
slow-query-time = 50
very-slow-query-time = 100

index-column-name-select = select cf, cid, cname from css_wr
index-column-name-insert = insert into css_wr ( cf, cid, cname ) values ( ? , ? , ? )
alter-widestring-table = ALTER TABLE {0}_css_w ADD {1} varchar(780)
index-widestring-table = CREATE INDEX {0}_css_w_{1} ON {0}_css_w ({1})

exists-widestring-row = select rid from css_w where rid = ?
exists-widestring-row.n.cn = select rid from cn_css_w where rid = ?
exists-widestring-row.n.ac = select rid from ac_css_w where rid = ?
exists-widestring-row.n.au = select rid from au_css_w where rid = ?
exists-widestring-row.n.lk = select rid from lk_css_w where rid = ?


delete-widestring-row = delete from css_w where rid = ?
delete-widestring-row.n.cn = delete from cn_css_w where rid = ?
delete-widestring-row.n.ac = delete from ac_css_w where rid = ?
delete-widestring-row.n.au = delete from au_css_w where rid = ?
delete-widestring-row.n.lk = delete from lk_css_w where rid = ?

update-widestring-row = update css_w set {0} where rid = ?; {0} = ?
update-widestring-row.n.cn = update cn_css_w set {0} where rid = ?; {0} = ?
update-widestring-row.n.ac = update ac_css_w set {0} where rid = ?; {0} = ?
update-widestring-row.n.au = update au_css_w set {0} where rid = ?; {0} = ?
update-widestring-row.n.lk = update lk_css_w set {0} where rid = ?; {0} = ?


insert-widestring-row = insert into css_w ( rid {0} ) values ( ? {1} )
insert-widestring-row.n.cn = insert into cn_css_w ( rid {0} ) values ( ? {1} )
insert-widestring-row.n.ac = insert into ac_css_w ( rid {0} ) values ( ? {1} )
insert-widestring-row.n.au = insert into au_css_w ( rid {0} ) values ( ? {1} )
insert-widestring-row.n.lk = insert into lk_css_w ( rid {0} ) values ( ? {1} )


##         * Part 0 basic SQL template; {0} is the where clause {1} is the sort clause {2} is the from {3} is the to record
##         *   eg select rid from css where {0} {1} LIMIT {2} ROWS {3}
##         * Part 1 where clause for non array matches; {0} is the columnName
##         *   eg {0} = ?
##         * Part 2 where clause for array matches (not possible to sort on array matches) {0} is the table alias, {1} is the where clause
##         *   eg rid in ( select {0}.rid from css {0} where {1} )
##         * Part 3 the where clause for array matches {0} is the table alias
##         *   eg {0}.cid = ? and {0}.v = ?  
##         * Part 3 sort clause {0} is the list to sort by
##         *   eg sort by {0}
##         * Part 4 sort elements, {0} is the column, {1} is the order
##         *   eg {0} {1}
##         * Dont include , AND or OR, the code will add those as appropriate. 
wide-block-find = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select a.rid from css_w a where {0} {1} ) as s) as TR where TR.R > {3,number,#} and TR.R <= {2,number,#}+{3,number,#};a.{0} = ?;a.rid in ( select {0}.rid from css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-block-find.n.cn = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select a.rid from cn_css_w a where {0} {1} ) as s) as TR where TR.R > {3,number,#} and TR.R <= {2,number,#}+{3,number,#};a.{0} = ?;a.rid in ( select {0}.rid from cn_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-block-find.n.ac = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select a.rid from ac_css_w a where {0} {1} ) as s) as TR where TR.R > {3,number,#} and TR.R <= {2,number,#}+{3,number,#};a.{0} = ?;a.rid in ( select {0}.rid from ac_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-block-find.n.au = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select a.rid from au_css_w a where {0} {1} ) as s) as TR where TR.R > {3,number,#} and TR.R <= {2,number,#}+{3,number,#};a.{0} = ?;a.rid in ( select {0}.rid from au_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-block-find.n.lk = select TR.rid from (select s.rid, ROW_NUMBER() OVER () AS R from (select a.rid from lk_css_w a where {0} {1} ) as s) as TR where TR.R > {3,number,#} and TR.R <= {2,number,#}+{3,number,#};a.{0} = ?;a.rid in ( select {0}.rid from lk_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}

wide-listchildren = select a.rid from css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-listchildren.n.cn = select a.rid from cn_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from cn_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-listchildren.n.ac = select a.rid from ac_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from ac_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-listchildren.n.au = select a.rid from au_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from au_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-listchildren.n.lk = select a.rid from lk_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from lk_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}

wide-countestimate = select count(*) from css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-countestimate.n.cn = select count(*) from cn_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from cn_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-countestimate.n.ac = select count(*) from ac_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from ac_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-countestimate.n.au = select count(*) from au_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from au_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}
wide-countestimate.n.lk = select count(*) from lk_css_w a where {0} {1} ;a.{0} = ?;a.rid in ( select {0}.rid from lk_css {0} where {1} );{0}.cid = ? and {0}.v = ?;sort by {0};{0} {1}

