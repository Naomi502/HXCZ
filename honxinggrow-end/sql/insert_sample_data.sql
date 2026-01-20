-- =================================================================
-- 样例数据插入脚本
-- 包含：暖心故事(4条)、志愿活动(4条)、问答(4条)
-- =================================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 1. 插入志愿活动数据 (hx_activity)
-- ----------------------------
INSERT INTO `hx_activity` (`title`, `summary`, `cover_url`, `description`, `start_time`, `end_time`, `location`, `max_participants`, `status`, `publish_time`, `publish_user_id`, `publish_user_name`, `participant_count`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
('校园环保志愿行', '清理校园死角，共建美丽校园', 'https://images.unsplash.com/photo-1550989460-0adf9ea622e2?q=80&w=600&auto=format&fit=crop', '本次活动旨在组织同学们对校园内的卫生死角进行清理，包括操场角落、教学楼后方等。请大家自带手套，学校提供垃圾袋和清理工具。让我们一起行动起来，为美丽的校园环境贡献一份力量！', DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 50 HOUR), '学校操场集合', 50, '1', NOW(), 1, 'admin', 12, '0', 'admin', NOW(), 'admin', NOW(), '热门活动'),
('敬老院爱心慰问', '走进敬老院，温暖老人心', 'https://images.unsplash.com/photo-1516307365426-bea591f05011?q=80&w=600&auto=format&fit=crop', '周末我们将前往市郊的“夕阳红”敬老院进行慰问演出和志愿服务。活动内容包括陪老人聊天、打扫卫生以及简单的文艺表演。欢迎有才艺的同学积极报名！', DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL -116 HOUR), '夕阳红敬老院', 30, '2', DATE_ADD(NOW(), INTERVAL -10 DAY), 1, 'admin', 30, '0', 'admin', DATE_ADD(NOW(), INTERVAL -10 DAY), 'admin', NOW(), '已结束活动'),
('社区图书整理小帮手', '协助社区图书馆整理书籍', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=600&auto=format&fit=crop', '社区图书馆近期新进了一批图书，急需志愿者帮忙分类、上架和贴标签。这是一个接触书籍、服务社区的好机会，工作相对轻松，但需要细心和耐心。', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 123 HOUR), '幸福社区图书馆', 10, '1', NOW(), 1, 'admin', 5, '0', 'admin', NOW(), 'admin', NOW(), '招募中'),
('义诊服务进万家', '配合校医院开展社区义诊', 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?q=80&w=600&auto=format&fit=crop', '配合校医院的医生团队，前往周边小区开展义诊活动。志愿者主要负责维持秩序、引导居民、分发健康宣传手册等工作。医学相关专业同学优先。', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 244 HOUR), '阳光花园小区广场', 20, '0', NULL, 1, 'admin', 0, '0', 'admin', NOW(), 'admin', NOW(), '草稿状态');

-- ----------------------------
-- 2. 插入暖心故事数据 (hx_story)
-- ----------------------------
INSERT INTO `hx_story` (`title`, `summary`, `cover_url`, `content`, `status`, `sort`, `publish_time`, `publish_user_id`, `publish_user_name`, `view_count`, `like_count`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
('第一次当志愿者的感悟', '那是初秋的一个下午，我第一次穿上红马甲...', 'https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?q=80&w=600&auto=format&fit=crop', '那是初秋的一个下午，我第一次穿上红马甲，站在路口协助交警维持交通秩序。起初我有些害羞，不敢大声说话，但看到一位老奶奶过马路不便时，我鼓起勇气走上前去搀扶。那一刻，她手中的温度传到了我的心里。虽然站了两个小时很累，但听到行人的每一声“谢谢”，我都觉得无比满足。志愿服务不仅仅是付出，更是一种收获，它让我感受到了人与人之间最纯粹的善意。', '1', 1, DATE_ADD(NOW(), INTERVAL -2 DAY), 1, 'admin', 156, 23, '0', 'admin', NOW(), 'admin', NOW(), '精选故事'),
('雨中送伞的小确幸', '一场突如其来的大雨，一次温暖的传递', 'https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?q=80&w=600&auto=format&fit=crop', '今天放学突然下起了倾盆大雨，我没带伞，正站在教学楼门口发愁。这时，一位陌生的同学走过来，微笑着问我要去哪里，说可以顺路送我一程。我们就这样撑着一把伞走到了宿舍楼下。虽然我的肩膀湿了一点，但心里却是暖暖的。这件事让我明白，善意是可以传递的，下一次，我也要做那个为别人撑伞的人。', '1', 2, DATE_ADD(NOW(), INTERVAL -5 DAY), 1, 'admin', 89, 12, '0', 'admin', NOW(), 'admin', NOW(), '日常点滴'),
('敬老院里的歌声', '用歌声传递快乐，陪伴是最长情的告白', 'https://images.unsplash.com/photo-1516307365426-bea591f05011?q=80&w=600&auto=format&fit=crop', '上周的敬老院之行让我印象深刻。我和几个同学准备了几首老歌，当我们唱起《东方红》时，几位爷爷奶奶也跟着哼唱起来，脸上洋溢着孩子般的笑容。演出结束后，一位奶奶拉着我的手说了好久的话，她说她想起了自己的孙女。那一刻我意识到，对于老人们来说，物质的馈赠或许重要，但真诚的陪伴和倾听才是他们最渴望的。', '1', 3, DATE_ADD(NOW(), INTERVAL -10 DAY), 1, 'admin', 230, 45, '0', 'admin', NOW(), 'admin', NOW(), '活动感悟'),
('图书馆的“神秘”角落', '在整理图书时发现的惊喜', 'https://images.unsplash.com/photo-1507842217121-9e96e4765949?q=80&w=600&auto=format&fit=crop', '作为图书馆志愿者，我负责整理最角落的文学书架。在整理过程中，我发现很多书里夹着以前同学写的便签，有的是读书心得，有的是对未来的期许，还有互相鼓励的话语。这些跨越时空的文字交流让我觉得图书馆不仅是藏书的地方，更是思想碰撞的火花之地。我把这些便签小心翼翼地收好，也许未来某一天，它们会成为学校珍贵的记忆档案。', '0', 4, NULL, 1, 'admin', 0, 0, '0', 'admin', NOW(), 'admin', NOW(), '草稿故事');

-- ----------------------------
-- 3. 插入问答数据 (hx_qa)
-- ----------------------------
INSERT INTO `hx_qa` (`title`, `content`, `video_url`, `status`, `sort`, `publish_time`, `publish_user_id`, `publish_user_name`, `view_count`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
('如何报名参加志愿活动？', '您可以登录红芯成长管理系统，点击“活动管理”菜单，在活动列表中选择感兴趣的活动，点击“报名”按钮即可。报名前请确认活动时间和地点，确保自己能够准时参加。报名成功后，请留意系统通知或短信提醒。', NULL, '1', 1, DATE_ADD(NOW(), INTERVAL -30 DAY), 1, 'admin', 560, '0', 'admin', NOW(), 'admin', NOW(), '常见问题'),
('参加活动后如何获得积分？', '活动结束后，活动负责人会根据您的签到和签退记录以及活动表现进行审核。审核通过后，系统会自动发放对应的积分到您的账户。一般情况下，活动结束后3个工作日内会完成积分发放。您可以在“个人中心”查看积分明细。', NULL, '1', 2, DATE_ADD(NOW(), INTERVAL -28 DAY), 1, 'admin', 420, '0', 'admin', NOW(), 'admin', NOW(), '积分规则'),
('发布的暖心故事需要审核吗？', '是的，为了维护良好的社区环境，所有用户发布的故事都需要经过管理员审核。审核内容主要包括故事内容的真实性、积极性以及是否包含违规信息。审核通常在提交后的24小时内完成，审核通过后故事将对所有用户可见。', NULL, '1', 3, DATE_ADD(NOW(), INTERVAL -25 DAY), 1, 'admin', 310, '0', 'admin', NOW(), 'admin', NOW(), '审核流程'),
('如果报名了活动但临时有事去不了怎么办？', '如果您报名了活动但因故无法参加，请务必在活动开始前至少24小时在系统上取消报名，或者直接联系活动负责人说明情况。无故缺席且未请假的，可能会被记录违约，影响后续活动的报名资格和信用积分。', NULL, '1', 4, DATE_ADD(NOW(), INTERVAL -20 DAY), 1, 'admin', 280, '0', 'admin', NOW(), 'admin', NOW(), '请假规则');

SELECT '样例数据插入完成' as result;
