# Fixes https://github.com/ieb/sparsemapcontent/issues/125 migrating data for existing schema

ALTER TABLE `css_b` MODIFY `b` mediumblob;
ALTER TABLE `cn_css_b` MODIFY `b` mediumblob;
ALTER TABLE `au_css_b` MODIFY `b` mediumblob;
ALTER TABLE `ac_css_b` MODIFY `b` mediumblob;
ALTER TABLE `lk_css_b` MODIFY `b` mediumblob;

