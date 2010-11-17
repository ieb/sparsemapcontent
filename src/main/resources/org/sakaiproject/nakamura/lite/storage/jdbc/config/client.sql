delete-string-row = delete from css where rid = ?
select-string-row = select cid, v from css where rid = ?
insert-string-column = insert into css ( v, rid, cid) values ( ?, ?, ? )
update-string-column = update css set v = ?  where rid = ? and cid = ?
remove-string-column = delete from css where rid = ? and cid = ?
check-schema = select count(*) from css
validate = values(1)
rowid-hash = SHA1
