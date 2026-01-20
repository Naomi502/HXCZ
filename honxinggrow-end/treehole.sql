-- 心灵树洞表
CREATE TABLE IF NOT EXISTS `hx_tree_hole` (
  `tree_hole_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户名',
  `content` text NOT NULL COMMENT '树洞内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `status` varchar(1) NOT NULL DEFAULT '0' COMMENT '状态（0：未读，1：已读）',
  `del_flag` varchar(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0：正常，1：删除）',
  PRIMARY KEY (`tree_hole_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='心灵树洞表';