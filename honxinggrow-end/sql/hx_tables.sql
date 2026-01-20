-- ----------------------------
-- 成长系统相关表
-- ----------------------------

-- ----------------------------
-- 1、成长故事表
-- ----------------------------
drop table if exists hx_story;
create table hx_story (
  story_id            bigint(20)      not null auto_increment    comment '故事ID',
  title               varchar(200)    default ''                 comment '故事标题',
  summary             varchar(500)    default ''                 comment '摘要',
  cover_url           varchar(500)    default ''                 comment '封面地址',
  content             text                                       comment '正文内容',
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
  primary key (story_id)
) engine=innodb auto_increment=1 comment = '成长故事表';

-- ----------------------------
-- 2、成长活动表
-- ----------------------------
drop table if exists hx_activity;
create table hx_activity (
  activity_id         bigint(20)      not null auto_increment    comment '活动ID',
  title               varchar(200)    default ''                 comment '活动名称',
  summary             varchar(500)    default ''                 comment '活动摘要',
  cover_url           varchar(500)    default ''                 comment '封面地址',
  description         text                                       comment '活动详情',
  start_time          datetime                                   comment '开始时间',
  end_time            datetime                                   comment '结束时间',
  location            varchar(200)    default ''                 comment '活动地点',
  max_participants    int(11)         default null               comment '人数上限',
  status              char(1)         default '0'                comment '状态（0草稿 1进行中 2结束）',
  publish_time        datetime                                   comment '发布时间',
  publish_user_id     bigint(20)      default null               comment '发布人ID',
  publish_user_name   varchar(64)     default ''                 comment '发布人名称',
  participant_count   int(11)         default 0                  comment '报名人数',
  del_flag            char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (activity_id)
) engine=innodb auto_increment=1 comment = '成长活动表';

-- ----------------------------
-- 3、活动参与记录表
-- ----------------------------
drop table if exists hx_activity_participant;
create table hx_activity_participant (
  participant_id      bigint(20)      not null auto_increment    comment '参与记录ID',
  activity_id         bigint(20)      not null                   comment '活动ID',
  user_id             bigint(20)      not null                   comment '学生ID',
  user_name           varchar(64)     default ''                 comment '学生姓名',
  join_time           datetime                                   comment '报名时间',
  status              char(1)         default '1'                comment '状态（1已报名 2取消）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (participant_id),
  unique key uk_activity_user (activity_id, user_id)
) engine=innodb auto_increment=1 comment = '活动参与记录表';

-- ----------------------------
-- 4、学生积分账户表
-- ----------------------------
drop table if exists hx_student_profile;
create table hx_student_profile (
  profile_id                  bigint(20)      not null auto_increment    comment '主键ID',
  user_id                     bigint(20)      not null                   comment '学生ID',
  user_name                   varchar(64)     default ''                 comment '学生姓名',
  points_total                int(11)         default 0                  comment '当前积分',
  last_story_point_time       datetime                                   comment '最近故事积分时间',
  last_activity_point_time    datetime                                   comment '最近活动积分时间',
  create_by                   varchar(64)     default ''                 comment '创建者',
  create_time                 datetime                                   comment '创建时间',
  update_by                   varchar(64)     default ''                 comment '更新者',
  update_time                 datetime                                   comment '更新时间',
  remark                      varchar(500)    default null               comment '备注',
  primary key (profile_id),
  unique key uk_user_id (user_id)
) engine=innodb auto_increment=1 comment = '学生积分账户表';

-- ----------------------------
-- 5、学生积分流水表
-- ----------------------------
drop table if exists hx_point_log;
create table hx_point_log (
  log_id              bigint(20)      not null auto_increment    comment '主键ID',
  user_id             bigint(20)      not null                   comment '学生ID',
  user_name           varchar(64)     default ''                 comment '学生姓名',
  biz_type            varchar(50)     default ''                 comment '业务类型',
  biz_id              bigint(20)      default null               comment '业务主键',
  points              int(11)         not null                   comment '积分变动值',
  remark              varchar(500)    default ''                 comment '备注',
  event_time          datetime                                   comment '事件时间',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  primary key (log_id),
  key idx_user_id (user_id),
  key idx_biz (biz_type, biz_id),
  key idx_event_time (event_time)
) engine=innodb auto_increment=1 comment = '学生积分流水表';

-- ----------------------------
-- 6、文件管理表
-- ----------------------------
drop table if exists hx_file;
create table hx_file (
  file_id             bigint(20)      not null auto_increment    comment '文件ID',
  file_name           varchar(255)    default ''                 comment '文件名',
  original_name       varchar(255)    default ''                 comment '原始文件名',
  file_path           varchar(500)    default ''                 comment '文件路径',
  file_url            varchar(500)    default ''                 comment '访问URL',
  file_type           varchar(50)     default ''                 comment '文件类型（image/audio/video/document）',
  file_category       varchar(50)     default ''                 comment '文件分类（cover/audio/video等）',
  content_type        varchar(50)     default ''                 comment '文件MIME类型',
  file_size           bigint(20)      default 0                  comment '文件大小（字节）',
  upload_time         datetime                                   comment '上传时间',
  upload_by           varchar(64)     default ''                 comment '上传者',
  business_type       varchar(50)     default ''                 comment '关联业务类型（activity/story/qa）',
  business_id         bigint(20)      default null               comment '关联业务ID',
  business_field      varchar(50)     default ''                 comment '关联业务字段（coverUrl/videoUrl等）',
  status              char(1)         default '0'                comment '状态（0正常 1删除）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (file_id),
  key idx_business (business_type, business_id),
  key idx_file_type (file_type, file_category),
  key idx_upload_time (upload_time)
) engine=innodb auto_increment=1 comment = '文件管理表';

-- ----------------------------
-- 7、菜单配置
-- ----------------------------
-- 文件管理菜单
insert into sys_menu values(null,  '文件管理',  '1200','4', 'file',     'hx/file/index',            '', '', 1, 0, 'C', '0', '0', 'hx:file:list',             'folder',        'admin', sysdate(), '', null, '文件管理菜单');
-- 文件管理权限点
insert into sys_menu values(null, '文件查询',  (select menu_id from sys_menu where menu_name = '文件管理' and parent_id = '1200'),'1', '',          '',                         '', '', 1, 0, 'F', '0', '0', 'hx:file:query',            '#',             'admin', sysdate(), '', null, '');
insert into sys_menu values(null, '文件上传',  (select menu_id from sys_menu where menu_name = '文件管理' and parent_id = '1200'),'2', '',          '',                         '', '', 1, 0, 'F', '0', '0', 'hx:file:add',              '#',             'admin', sysdate(), '', null, '');
insert into sys_menu values(null, '文件删除',  (select menu_id from sys_menu where menu_name = '文件管理' and parent_id = '1200'),'3', '',          '',                         '', '', 1, 0, 'F', '0', '0', 'hx:file:remove',           '#',             'admin', sysdate(), '', null, '');

-- 或者使用以下方式，先删除可能存在的菜单，再重新插入
-- delete from sys_menu where menu_name = '文件管理' or parent_id in (select menu_id from sys_menu where menu_name = '文件管理');
-- insert into sys_menu values('1204',  '文件管理',  '1200','4', 'file',     'hx/file/index',            '', '', 1, 0, 'C', '0', '0', 'hx:file:list',             'folder',        'admin', sysdate(), '', null, '文件管理菜单');
-- insert into sys_menu values('12041', '文件查询',  '1204','1', '',          '',                         '', '', 1, 0, 'F', '0', '0', 'hx:file:query',            '#',             'admin', sysdate(), '', null, '');
-- insert into sys_menu values('12042', '文件上传',  '1204','2', '',          '',                         '', '', 1, 0, 'F', '0', '0', 'hx:file:add',              '#',             'admin', sysdate(), '', null, '');
-- insert into sys_menu values('12043', '文件删除',  '1204','3', '',          '',                         '', '', 1, 0, 'F', '0', '0', 'hx:file:remove',           '#',             'admin', sysdate(), '', null, '');

