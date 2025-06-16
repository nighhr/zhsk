-- liquibase formatted sql

-- changeset zq:1.01
CREATE TABLE IF NOT EXISTS `kerneluser` (
    `id` int NOT NULL AUTO_INCREMENT,
    `tenantId` bigint NOT NULL,
    `userName` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户姓名',
    `userCode` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '工号',
    `userType` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户类型 1 业务员',
    `mobileNo` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '手机号',
    `departmentId` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '部门编码',
    `departmentName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '部门名称',
    `desc` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
    `enabled` bit(1) DEFAULT NULL COMMENT '是否在职',
    `lastLoginTime` datetime DEFAULT NULL COMMENT '上次登录时间',
    `deleted` bit(1) DEFAULT NULL,
    `creator` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
    `createTime` datetime DEFAULT NULL,
    `updater` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
    `updateTime` datetime DEFAULT NULL,
    `parents` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '上级(存工号，逗号分隔)',
    `unionId` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户在微信开放平台的unionId',
    `openId` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户关注服务号openId',
    `wechatUrl` varchar(255) COLLATE utf8mb4_bin DEFAULT 'https://q1.itc.cn/q_70/images03/20240925/e0b698097f41425893a4f7583e2997d3.jpeg',
    PRIMARY KEY (`id`,`tenantId`),
    KEY `idx_user_userCode` (`mobileNo`)
    ) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='中台用户表';

