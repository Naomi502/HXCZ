-- 为活动表添加可见岗位字段
-- 如果字段已存在，请先执行删除：ALTER TABLE hx_activity DROP COLUMN visible_post_ids;
ALTER TABLE hx_activity
    ADD COLUMN visible_post_ids varchar(255) DEFAULT '' COMMENT '可见岗位ID列表，逗号分隔，为空表示所有岗位可见' AFTER del_flag;
