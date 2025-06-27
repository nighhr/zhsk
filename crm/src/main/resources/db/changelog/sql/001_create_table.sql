-- liquibase formatted sql
-- UPDATE DATABASECHANGELOG
-- SET MD5SUM = '8:da87877b4a479eb10f0315c4b0db94af'
-- WHERE ID = '1.02' AND AUTHOR = 'zq' AND FILENAME = 'db/changelog/sql/001_create_table.sql';
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

CREATE TABLE `department` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
                              `HCid` bigint NOT NULL COMMENT '华创部门id',
                              `code` varchar(50) NOT NULL COMMENT '编码',
                              `name` varchar(100) NOT NULL COMMENT '名称',
                              `area_id` bigint DEFAULT NULL COMMENT '区域',
                              `type` varchar(50) DEFAULT NULL COMMENT '类型',
                              `parent_id` bigint DEFAULT NULL COMMENT '上级机构id',
                              `parent_ids` varchar(1000) DEFAULT NULL COMMENT '所有上级机构id',
                              `create_date` datetime NOT NULL COMMENT '创建时间',
                              `update_date` datetime NOT NULL COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_hcid` (`HCid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- changeset zq:1.03
CREATE TABLE IF NOT EXISTS `supplier` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                            `hc_id` bigint(20) NOT NULL COMMENT '供应商id',
                            `no` varchar(50) NOT NULL COMMENT '编码',
                            `name` varchar(100) NOT NULL COMMENT '名称',
                            `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态:1->启用,2->暂停',
                            `data_type` varchar(20) NOT NULL COMMENT '类型:internal 内部, external:外部',
                            `business_category` varchar(20) NOT NULL COMMENT '经营类别:SELL->经销,AGENCY->代销,POOL->联营',
                            `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_no` (`no`),
                            KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

CREATE TABLE IF NOT EXISTS `stock` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                         `hc_id` bigint(20) NOT NULL COMMENT '部门id',
                         `code` varchar(50) NOT NULL COMMENT '编码',
                         `name` varchar(100) NOT NULL COMMENT '名称',
                         `area_id` bigint(20) DEFAULT NULL COMMENT '区域id',
                         `type` varchar(20) DEFAULT NULL COMMENT '类型',
                         `parent_id` bigint(20) DEFAULT NULL COMMENT '上级机构id',
                         `parent_ids` varchar(500) DEFAULT NULL COMMENT '所有上级机构id(格式如: ,1,2,3,)',
                         `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_code` (`code`),
                         KEY `idx_parent_id` (`parent_id`),
                         KEY `idx_parent_ids` (`parent_ids`(255)),
                         KEY `idx_area_id` (`area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- changeset zq:1.04
CREATE TABLE IF NOT EXISTS table_mapping (
                               id INT PRIMARY KEY AUTO_INCREMENT,
                               rule_name VARCHAR(255) NOT NULL COMMENT '映射名称',
                               type VARCHAR(100) COMMENT '映射类型',
                               is_active TINYINT(1) DEFAULT 1 COMMENT '是否激活',
                               description VARCHAR(500) COMMENT '映射描述',
                               creator VARCHAR(100) COMMENT '创建人',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               updater VARCHAR(100) COMMENT '更新人',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表映射配置';

CREATE TABLE IF NOT EXISTS `column_mapping` (
                                  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `table_mapping_id` int(11) NOT NULL COMMENT '关联的表映射ID',
                                  `source_column_name` varchar(100) NOT NULL COMMENT '源列名',
                                  `target_column_name` varchar(100) NOT NULL COMMENT '目标列名',
                                  `description` varchar(500) DEFAULT NULL COMMENT '字段描述',
                                  `creator` varchar(50) DEFAULT NULL COMMENT '创建人',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(50) DEFAULT NULL COMMENT '更新人',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_table_mapping_id` (`table_mapping_id`),
                                  KEY `idx_source_column` (`source_column_name`),
                                  KEY `idx_target_column` (`target_column_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段映射表';

CREATE TABLE IF NOT EXISTS `value_mapping` (
                                 `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `column_mapping_id` int(11) NOT NULL COMMENT '列映射ID',
                                 `source_value` varchar(255) NOT NULL COMMENT '源值',
                                 `target_value` varchar(255) NOT NULL COMMENT '目标值',
                                 `creator` varchar(50) DEFAULT NULL COMMENT '创建人',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updater` varchar(50) DEFAULT NULL COMMENT '更新人',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_column_mapping_id` (`column_mapping_id`),
                                 CONSTRAINT `fk_column_mapping` FOREIGN KEY (`column_mapping_id`) REFERENCES `column_mapping` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值映射表';

-- changeset zq:1.05
CREATE TABLE `db_connections` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `connection_name` varchar(100) NOT NULL COMMENT '连接名称',
                                  `connection_type` varchar(50) NOT NULL COMMENT '连接类型',
                                  `db_host` varchar(100) NOT NULL COMMENT '数据库主机',
                                  `db_port` int NOT NULL COMMENT '数据库端口',
                                  `db_name` varchar(100) NOT NULL COMMENT '数据库名称',
                                  `username` varchar(100) NOT NULL COMMENT '用户名',
                                  `password` varchar(255) NOT NULL COMMENT '密码(加密存储)',
                                  `charset` varchar(255) NOT NULL COMMENT '字符集',
                                  `creator` varchar(100) NOT NULL COMMENT '创建人',
                                  `create_time` datetime NOT NULL COMMENT '创建时间',
                                  `updater` varchar(100) NOT NULL COMMENT '更新人',
                                  `update_time` datetime NOT NULL COMMENT '更新时间',
                                  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除(0-否,1-是)',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_connection_name` (`connection_name`),
                                  KEY `idx_db_name` (`db_name`),
                                  KEY `idx_connection_type` (`connection_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据库连接信息表';

