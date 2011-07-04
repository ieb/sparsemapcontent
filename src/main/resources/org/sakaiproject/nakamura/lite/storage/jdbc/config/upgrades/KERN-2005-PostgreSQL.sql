
# This is only for postgresql instances.

alter table css_b add CONSTRAINT css_b_rid_uk UNIQUE (rid);
alter table au_css_b add CONSTRAINT au_css_b_rid_uk UNIQUE (rid);
alter table ac_css_b add CONSTRAINT ac_css_b_rid_uk UNIQUE (rid);
alter table cn_css_b add CONSTRAINT cn_css_b_rid_uk UNIQUE (rid);