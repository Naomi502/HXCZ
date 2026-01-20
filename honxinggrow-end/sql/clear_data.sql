-- =================================================================
-- 清除业务数据脚本
-- 警告：执行此脚本将清空所有 活动、故事、问答 及其关联数据！
-- =================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清除活动数据
TRUNCATE TABLE hx_activity;
TRUNCATE TABLE hx_activity_participant;

-- 2. 清除故事数据
TRUNCATE TABLE hx_story;
TRUNCATE TABLE hx_story_like;

-- 3. 清除问答数据
TRUNCATE TABLE hx_qa;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '所有业务数据已清除完毕' as result;
