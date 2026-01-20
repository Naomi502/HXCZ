ALTER TABLE `hx_activity_participant`
    ADD COLUMN `proof_image_url` varchar(512) DEFAULT NULL COMMENT '活动证明图片' AFTER `status`,
    ADD COLUMN `proof_status` char(1) NOT NULL DEFAULT '0' COMMENT '证明状态（0未提交 1待审核 2已通过 3已驳回）' AFTER `proof_image_url`,
    ADD COLUMN `proof_submit_time` datetime DEFAULT NULL COMMENT '证明提交时间' AFTER `proof_status`,
    ADD COLUMN `proof_audit_by` varchar(64) DEFAULT NULL COMMENT '审核人' AFTER `proof_submit_time`,
    ADD COLUMN `proof_audit_time` datetime DEFAULT NULL COMMENT '审核时间' AFTER `proof_audit_by`,
    ADD COLUMN `proof_audit_remark` varchar(255) DEFAULT NULL COMMENT '审核意见' AFTER `proof_audit_time`,
    ADD COLUMN `reward_granted` char(1) NOT NULL DEFAULT '0' COMMENT '是否已发放积分（0否 1是）' AFTER `proof_audit_remark`,
    ADD COLUMN `reward_time` datetime DEFAULT NULL COMMENT '发放积分时间' AFTER `reward_granted`,
    ADD COLUMN `reward_points` int DEFAULT 0 COMMENT '发放积分数' AFTER `reward_time`;

UPDATE hx_activity_participant
SET proof_status = '0',
    reward_granted = '0',
    reward_points = 0
WHERE proof_status IS NULL;

