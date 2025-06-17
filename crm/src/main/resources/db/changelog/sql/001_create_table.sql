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

-- changeset zq:1.02
CREATE TABLE IF NOT EXISTS `product` (
                           `id` INT AUTO_INCREMENT NOT NULL COMMENT '商品id',
                           `HCid` varchar(50) NOT NULL COMMENT '华创商品id',
                           `code` varchar(50) NOT NULL COMMENT '编号',
                           `name` varchar(255) NOT NULL COMMENT '名称',
                           `bar_code` varchar(100) DEFAULT NULL COMMENT '条码',
                           `provider_no` varchar(50) DEFAULT NULL COMMENT '供应商',
                           `provider_Number` varchar(50) DEFAULT NULL COMMENT '供应商编码',
                           `provider_Name` varchar(255) DEFAULT NULL COMMENT '供应商名称',
                           `brand_id` varchar(50) DEFAULT NULL COMMENT '品牌',
                           `brand_Number` varchar(50) DEFAULT NULL COMMENT '品牌编码',
                           `brand_Name` varchar(255) DEFAULT NULL COMMENT '品牌名称',
                           `producer` varchar(100) DEFAULT NULL COMMENT '产地',
                           `modal_id` varchar(50) DEFAULT NULL COMMENT '规格',
                           `kind_id` varchar(50) DEFAULT NULL COMMENT '分类',
                           `kind_Name` varchar(255) DEFAULT NULL COMMENT '分类名称',
                           `kind_Number` varchar(50) DEFAULT NULL COMMENT '分类编码',
                           `unit` varchar(20) DEFAULT NULL COMMENT '单位',
                           `medium_unit` varchar(20) DEFAULT NULL COMMENT '中等单位',
                           `max_unit` varchar(20) DEFAULT NULL COMMENT '最大单位',
                           `medium_unit_num` int DEFAULT NULL COMMENT '商品中等单位件含量',
                           `max_unit_num` int DEFAULT NULL COMMENT '商品最大单位件含量',
                           `company` varchar(50) DEFAULT NULL COMMENT '所属公司',
                           `company_Name` varchar(255) DEFAULT NULL COMMENT '所属公司名称',
                           `company_Number` varchar(50) DEFAULT NULL COMMENT '所属公司编码',
                           `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted` tinyint(1) DEFAULT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='商品信息表';
