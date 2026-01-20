-- 微信快捷登录绑定表
create table if not exists hx_user_wechat (
  bind_id         bigint(20)   not null auto_increment comment '主键ID',
  user_id         bigint(20)   not null comment '用户ID',
  openid          varchar(64)  not null comment '微信openid',
  session_key     varchar(128) default null comment '最近一次session_key',
  bind_time       datetime     default current_timestamp comment '绑定时间',
  last_login_time datetime     default null comment '最近快捷登录时间',
  create_time     datetime     default current_timestamp comment '创建时间',
  update_time     datetime     default current_timestamp on update current_timestamp comment '更新时间',
  primary key (bind_id),
  unique key uk_user_id (user_id),
  unique key uk_openid (openid)
) engine=innodb default charset=utf8mb4 comment='用户微信绑定表';


