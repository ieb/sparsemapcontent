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
check-schema = select count(*) from css
validate = select 1
rowid-hash = SHA1
