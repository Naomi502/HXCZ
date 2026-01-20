-- ----------------------------
-- 6、问答表
-- ----------------------------
drop table if exists hx_qa;
create table hx_qa (
  qa_id               bigint(20)      not null auto_increment    comment '问答ID',
  title               varchar(200)    default ''                 comment '问题标题',
  content             text                                       comment '解答内容',
  video_url           varchar(500)    default ''                 comment '视频链接',
  status              char(1)         default '0'                comment '状态（0草稿 1发布 2下架）',
  sort                int(4)          default 0                  comment '排序值',
  publish_time        datetime                                   comment '发布时间',
  publish_user_id     bigint(20)      default null               comment '发布人ID',
  publish_user_name   varchar(64)     default ''                 comment '发布人名称',
  view_count          int(11)         default 0                  comment '阅读次数',
  del_flag            char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (qa_id)
) engine=innodb auto_increment=1 comment = '成长问答表';