delete-binary-row = delete from csb where rid = ?
delete-string-row = delete from css where rid = ?
select-string-row = select cid, v from css where rid = ?
select-binary-row = select cid, v from csb where rid = ?
insert-string-column = insert into css ( v, rid, cid) values ( ?, ?, ? )
insert-binary-column = insert into csb ( v, rid, cid) values ( ?, ?, ? )
update-string-column = update css set v = ?  where rid = ? and cid = ?
update-binary-column = update csb set v = ?  where rid = ? and cid = ?
remove-string-column = delete from css where rid = ? and cid = ?
remove-binary-column = delete from csb where rid = ? and cid = ?
validate = values(1)
rowid-hash = SHA1
