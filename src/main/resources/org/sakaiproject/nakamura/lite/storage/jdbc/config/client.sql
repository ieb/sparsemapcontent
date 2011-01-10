delete-string-row = delete from css where rid = ?
select-string-row = select cid, v from css where rid = ?
insert-string-column = insert into css ( v, rid, cid) values ( ?, ?, ? )
update-string-column = update css set v = ?  where rid = ? and cid = ?
remove-string-column = delete from css where rid = ? and cid = ?
check-schema = select count(*) from css
find = select a.rid, a.cid, a.v from css a {0} where {1} 1 = 1 ;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid and
validate = values(1)
rowid-hash = SHA1


select-index-columns = select cid from index_cols

block-select-row = select b from css_b where rid = ?
block-delete-row = delete from css_b where rid = ?
block-insert-row = insert into css_b (rid,b) values (?, ?)
block-update-row = update css_b set b = ? where rid = ?

block-find = select a.rid, a.b from css_b a {0} where {1} 1 = 1;, css {0} ; {0}.cid = ? and {0}.v = ? and {0}.rid = a.rid and

use-batch-inserts = 0
