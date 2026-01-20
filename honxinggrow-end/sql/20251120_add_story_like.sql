-- 为故事表添加点赞数字段
ALTER TABLE hx_story 
ADD COLUMN like_count INT(11) DEFAULT 0 COMMENT '点赞数';

-- 创建故事点赞记录表
DROP TABLE IF EXISTS hx_story_like;
CREATE TABLE hx_story_like (
  like_id          BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '点赞ID',
  story_id         BIGINT(20)      NOT NULL                   COMMENT '故事ID',
  user_id          BIGINT(20)      NOT NULL                   COMMENT '用户ID',
  user_name        VARCHAR(64)     DEFAULT ''                 COMMENT '用户名称',
  create_time      DATETIME                                   COMMENT '点赞时间',
  PRIMARY KEY (like_id),
  UNIQUE KEY uk_story_user (story_id, user_id),
  KEY idx_story_id (story_id),
  KEY idx_user_id (user_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '故事点赞记录表';

