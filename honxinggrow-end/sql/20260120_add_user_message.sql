-- 用户消息表
drop table if exists hx_user_message;
create table hx_user_message (
  message_id        bigint(20)      not null auto_increment    comment '消息ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  title             varchar(100)    default ''                 comment '标题',
  content           varchar(500)    default ''                 comment '内容',
  type              char(1)         default '0'                comment '类型（0系统 1活动报名 2活动证明）',
  is_read           char(1)         default '0'                comment '是否已读（0未读 1已读）',
  create_time       datetime                                   comment '创建时间',
  primary key (message_id)
) engine=innodb auto_increment=1 comment = '用户消息表';

-- 索引
create index idx_hx_message_user on hx_user_message(user_id);
