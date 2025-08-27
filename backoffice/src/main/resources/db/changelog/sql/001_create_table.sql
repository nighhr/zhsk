--liquibase formatted sql

-- changeset zq:1.01
CREATE TABLE IF NOT EXISTS `table_mapping`  (
    `id` int NOT NULL AUTO_INCREMENT,
    `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '映射名称',
    `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '映射类型',
    `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否激活',
    `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '映射描述',
    `creator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '表映射配置' ROW_FORMAT = Dynamic;

-- changeset zq:1.02
CREATE TABLE IF NOT EXISTS `column_mapping`  (
   `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `table_mapping_id` int NOT NULL COMMENT '关联的表映射ID',
   `source_column_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '源列名',
   `target_column_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标列名',
   `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段描述',
   `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   PRIMARY KEY (`id`) USING BTREE,
   INDEX `idx_table_mapping_id`(`table_mapping_id` ASC) USING BTREE,
   INDEX `idx_source_column`(`source_column_name` ASC) USING BTREE,
   INDEX `idx_target_column`(`target_column_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字段映射表' ROW_FORMAT = Dynamic;

-- changeset zq:1.03
CREATE TABLE IF NOT EXISTS `value_mapping`  (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `column_mapping_id` int NOT NULL COMMENT '列映射ID',
    `source_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '源值',
    `target_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标值',
    `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_column_mapping_id`(`column_mapping_id` ASC) USING BTREE,
    CONSTRAINT `fk_column_mapping` FOREIGN KEY (`column_mapping_id`) REFERENCES `column_mapping` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '值映射表' ROW_FORMAT = Dynamic;

-- changeset zq:1.04
CREATE TABLE IF NOT EXISTS `department`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
    `HCid` bigint NOT NULL COMMENT '华创部门id',
    `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
    `area_id` bigint NULL DEFAULT NULL COMMENT '区域',
    `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
    `data_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据来源',
    `parent_id` bigint NULL DEFAULT NULL COMMENT '上级机构id',
    `parent_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所有上级机构id',
    `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_hcid`(`HCid` ASC) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- changeset zq:1.05
CREATE TABLE IF NOT EXISTS `supplier`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
    `hc_id` bigint NOT NULL COMMENT '供应商id',
    `no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
    `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态:1->启用,2->暂停',
    `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型:internal 内部, external:外部',
    `business_category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '经营类别:SELL->经销,AGENCY->代销,POOL->联营',
    `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_no`(`no` ASC) USING BTREE,
    INDEX `idx_name`(`name` ASC) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商表' ROW_FORMAT = Dynamic;

-- changeset zq:1.06
CREATE TABLE IF NOT EXISTS  `goods` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `hcid` bigint NOT NULL COMMENT '华创ID',
    `code` varchar(50) NOT NULL COMMENT '编号',
    `name` varchar(100) NOT NULL COMMENT '名称',
    `bar_code` varchar(100) DEFAULT NULL COMMENT '条码',
    `provider_no` varchar(50) DEFAULT NULL COMMENT '供应商',
    `provider_number` varchar(50) DEFAULT NULL COMMENT '供应商编码',
    `provider_name` varchar(100) DEFAULT NULL COMMENT '供应商名称',
    `brand_id` varchar(50) DEFAULT NULL COMMENT '品牌ID',
    `brand_number` varchar(50) DEFAULT NULL COMMENT '品牌编码',
    `brand_name` varchar(100) DEFAULT NULL COMMENT '品牌名称',
    `producer` varchar(100) DEFAULT NULL COMMENT '产地',
    `modal_id` varchar(50) DEFAULT NULL COMMENT '规格ID',
    `kind_id` varchar(50) DEFAULT NULL COMMENT '分类ID',
    `kind_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
    `kind_number` varchar(50) DEFAULT NULL COMMENT '分类编码',
    `unit` varchar(20) DEFAULT NULL COMMENT '单位',
    `medium_unit` varchar(20) DEFAULT NULL COMMENT '中等单位',
    `max_unit` varchar(20) DEFAULT NULL COMMENT '最大单位',
    `medium_unit_num` int DEFAULT NULL COMMENT '商品中等单位件含量',
    `max_unit_num` int DEFAULT NULL COMMENT '商品最大单位件含量',
    `company` varchar(50) DEFAULT NULL COMMENT '所属公司',
    `company_name` varchar(100) DEFAULT NULL COMMENT '所属公司名称',
    `company_number` varchar(50) DEFAULT NULL COMMENT '所属公司编码',
    `create_date` datetime NOT NULL COMMENT '创建时间',
    `update_date` datetime NOT NULL COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';

-- changeset zq:1.07
CREATE TABLE IF NOT EXISTS `stock`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `hc_id` bigint NOT NULL COMMENT '华创ID',
    `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
    `area_id` bigint NULL DEFAULT NULL COMMENT '区域ID',
    `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
    `parent_id` bigint NULL DEFAULT NULL COMMENT '上级机构ID',
    `parent_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所有上级机构ID(格式如: ,1,2,3,)',
    `create_date` datetime NOT NULL COMMENT '创建时间',
    `update_date` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_hcid`(`hc_id` ASC) USING BTREE,
    INDEX `idx_area_id`(`area_id` ASC) USING BTREE,
    INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
    INDEX `idx_parent_ids`(`parent_ids`(255) ASC) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '库存/部门表' ROW_FORMAT = Dynamic;

-- changeset zq:1.08
CREATE TABLE IF NOT EXISTS `task`  (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '任务名称',
    `source_db_id` bigint NOT NULL COMMENT '源数据id',
    `source_db` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '源数据库',
    `source_table` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '源数据表',
    `detail_table` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '明细表名称',
    `start_time` datetime NULL DEFAULT NULL COMMENT '起始时间',
    `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
    `target_app_id` bigint NOT NULL COMMENT '目标应用id',
    `target_app` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '目标应用',
    `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '任务类型',
    `execute_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '执行方式: MANUAL/FIXED_TIME/FIXED_INTERVAL',
    `execute_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '执行时间或间隔',
    `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: true=启用(1), false=停用(0)',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- changeset zq:1.09
CREATE TABLE IF NOT EXISTS `task_voucher_head`  (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `task_id` bigint NOT NULL COMMENT '关联的任务ID',
    `db_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '数据库名称',
    `account_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '账套名称',
    `voucher_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '凭证识别主键',
    `voucher_word` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '凭证字',
    `voucher_date` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '凭证日期',
    `business_date` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '业务日期',
    `attachment_count` int NULL DEFAULT NULL COMMENT '附件张数',
    `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '制单人',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_task_id`(`task_id` ASC) USING BTREE,
    INDEX `task_id`(`task_id` ASC) USING BTREE,
    CONSTRAINT `task_config_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- changeset zq:1.10
CREATE TABLE IF NOT EXISTS `task_voucher_entries`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
    `direction` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '借贷方向',
    `amount` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本位币金额',
    `supplier_related` tinyint(1) NULL DEFAULT NULL COMMENT '供应商往来(0否1是)',
    `department_accounting` tinyint(1) NULL DEFAULT NULL COMMENT '部门核算(0否1是)',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分录表' ROW_FORMAT = Dynamic;

-- changeset zq:1.11
CREATE TABLE IF NOT EXISTS `operation_log`  (
                                                `id` bigint NOT NULL AUTO_INCREMENT,
                                                `task_id` bigint NOT NULL,
                                                `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `primary_key_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `log_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `input_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    `log_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;


-- changeset zq:1.12
CREATE TABLE IF NOT EXISTS `db_connections`  (
                                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                 `connection_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '连接名称',
    `connection_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '连接类型',
    `db_host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库主机',
    `db_port` int NOT NULL COMMENT '数据库端口',
    `db_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库名称',
    `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
    `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(加密存储)',
    `charset` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字符集',
    `creator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `updater` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_connection_name`(`connection_name` ASC) USING BTREE,
    INDEX `idx_db_name`(`db_name` ASC) USING BTREE,
    INDEX `idx_connection_type`(`connection_type` ASC) USING BTREE
    ) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据库连接信息表' ROW_FORMAT = Dynamic;


-- changeset zq:1.13
CREATE TABLE IF NOT EXISTS `at_pur_in` (
    `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `FID` varchar(50) NOT NULL COMMENT '主表ID',
    `FOrderNo` varchar(50) DEFAULT NULL COMMENT '订单单号',
    `FDate` datetime DEFAULT NULL COMMENT '日期',
    `FDepID` varchar(50) DEFAULT NULL COMMENT '采购组织ID',
    `FDepNumber` varchar(50) DEFAULT NULL COMMENT '采购组织编码',
    `FDepName` varchar(100) DEFAULT NULL COMMENT '采购组织名称',
    `FSupplierID` varchar(50) DEFAULT NULL COMMENT '供应商ID',
    `FSupplierNumber` varchar(50) DEFAULT NULL COMMENT '供应商编码',
    `FSupplierName` varchar(100) DEFAULT NULL COMMENT '供应商名称',
    `FRemark` varchar(500) DEFAULT NULL COMMENT '备注',
    `FCreateBy` varchar(50) DEFAULT NULL COMMENT '创建人',
    `FCreateDate` varchar(20) DEFAULT NULL COMMENT '创建时间',
    `FUpdateBy` varchar(50) DEFAULT NULL COMMENT '修改人',
    `FUpdateDate` varchar(20) DEFAULT NULL COMMENT '修改时间',
    `FBillType` varchar(20) DEFAULT NULL COMMENT '单据类型',
    `sync_flag` tinyint DEFAULT '0' COMMENT '同步标志(0:未同步 1:已同步)',
    `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `idx_fid` (`FID`),
    KEY `idx_order_no` (`FOrderNo`),
    KEY `idx_date` (`FDate`),
    KEY `idx_supplier` (`FSupplierID`,`FSupplierNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购入库单主表';

-- changeset zq:1.14
CREATE TABLE IF NOT EXISTS  `at_pur_in_line`  (
    `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主表ID',
    `FEntryID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '明细ID',
    `FMaterialID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料ID',
    `FMaterialNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料编码',
    `FMaterialName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料名称',
    `FUnit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
    `FInQty` double NULL DEFAULT 0 COMMENT '入库数量',
    `FQty` double NULL DEFAULT 0 COMMENT '入库正品数量',
    `FGiftQty` double NULL DEFAULT 0 COMMENT '入库赠品数量',
    `FBatch` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入库批号',
    `FStockID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入库仓库ID',
    `FStockNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入库仓库编码',
    `FStockName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入库仓库名称',
    `FRate` double NULL DEFAULT 0 COMMENT '税率（%）',
    `FTaxPrice` double NULL DEFAULT 0 COMMENT '含税单价',
    `FAllAmount` double NULL DEFAULT 0 COMMENT '含税金额',
    `FNote` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
    `FOrderEntryID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '采购订单明细内码',
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `idx_fid_entry`(`FEntryID` ASC, `FID` ASC) USING BTREE,
    INDEX `idx_order_entry`(`FOrderEntryID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '采购入库单明细表' ROW_FORMAT = Dynamic;

-- changeset zq:1.15
CREATE TABLE IF NOT EXISTS  `at_pur_ret`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '主表ID',
    `FBillType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单据类型(external：外部返厂单/门店退回; internal：内部返厂单/公司退回)',
    `FFactoryType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '返厂单类型(1：强制返厂，2：批次返厂，3：定价返厂，4：调整返厂，5：按单返厂)',
    `FDepID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '采购组织ID',
    `FDepNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '采购组织编码',
    `FDepName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '采购组织名称',
    `FDate` datetime NULL DEFAULT NULL COMMENT '日期',
    `FSupplierID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '供应商ID',
    `FSupplierNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '供应商编码',
    `FSupplierName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '供应商名称',
    `FReason` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退料原因(1:次品返厂，2:过季返厂，3:清退返厂，4:门店收错货返厂，5:代销产品返厂，6:其它原因)',
    `FRemark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
    `FCreateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
    `FCreateDate` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `FUpdateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '修改人',
    `FUpdateDate` datetime NULL DEFAULT NULL COMMENT '修改时间',
    `sync_flag` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '同步标志',
    `sync_time` datetime NULL DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uq_fid`(`FID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '采购返厂单主表' ROW_FORMAT = Dynamic;
-- changeset zq:1.16
CREATE TABLE IF NOT EXISTS  `at_pur_ret_line`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '主表ID',
    `FEntryID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '明细ID',
    `FMaterialID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '物料ID',
    `FMaterialNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '物料编码',
    `FMaterialName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '物料名称',
    `FMaterialTypeNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '物料类型编码',
    `FMaterialTypeName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '物料类型名称',
    `FUnit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单位',
    `FQty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退货数量',
    `FBatch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退货批号',
    `FStockID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退货仓库ID',
    `FStockNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退货仓库编码',
    `FStockName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退货仓库名称',
    `FRate` double NULL DEFAULT NULL COMMENT '税率（%）',
    `FTaxPrice` double NULL DEFAULT NULL COMMENT '含税单价',
    `FAllAmount` double NULL DEFAULT NULL COMMENT '含税金额',
    `FNote` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uq_fentryid`(`FEntryID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '采购返厂单明细表' ROW_FORMAT = Dynamic;

-- changeset zq:1.17
CREATE TABLE IF NOT EXISTS  `at_sale`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `FID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主表ID',
    `FBillNo` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单据编码',
    `FSalesType` int NULL DEFAULT NULL COMMENT '销售类型(0：销售(非会员)，1：销售(会员)，2：退货，3：挂单，4：维修，5：批发)',
    `FDate` datetime NULL DEFAULT NULL COMMENT '业务日期',
    `FOrgType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售组织类型',
    `FOrgID` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售组织ID',
    `FOrgNumber` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售组织编码',
    `FOrgName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售组织名称',
    `FGuideID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导购员ID',
    `FGuideNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导购员编码',
    `FGuideName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导购员名称',
    `FRemark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
    `FCreateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
    `FCreateDate` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `FUpdateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
    `FUpdateDate` datetime NULL DEFAULT NULL COMMENT '修改时间',
    `sync_flag` tinyint(1) NULL DEFAULT NULL COMMENT '同步标志',
    `sync_time` datetime NULL DEFAULT NULL COMMENT '同步时间',
    `FSetTypeName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结算方式名称',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_at_sale_fid`(`FID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '销售主表' ROW_FORMAT = Dynamic;

-- changeset zq:1.18
CREATE TABLE IF NOT EXISTS  `at_sale_line`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `FID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主表ID',
    `FEntryID` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '明细ID',
    `FMaterialID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料ID',
    `FMaterialNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料编码',
    `FMaterialName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料名称',
    `FMaterialTypeNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料类型编码',
    `FMaterialTypeName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料类型名称',
    `FUnit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
    `FSaleNum` decimal(10, 2) NULL DEFAULT NULL COMMENT '计价数量',
    `FSalesPrice` decimal(18, 6) NULL DEFAULT NULL COMMENT '零售价',
    `FDealPrice` decimal(18, 6) NULL DEFAULT NULL COMMENT '成交价（含税）',
    `FRate` decimal(5, 2) NULL DEFAULT NULL COMMENT '税率（%）',
    `FDiscount` decimal(10, 2) NULL DEFAULT NULL COMMENT '折扣额',
    `FAllAmount` decimal(18, 6) NULL DEFAULT NULL COMMENT '价税合计',
    `FStockPrice` decimal(18, 6) NULL DEFAULT NULL COMMENT '销售成本',
    `FStockID` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出库仓库ID',
    `FStockNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出库仓库编码',
    `FStockName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出库仓库名称',
    `FBatch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出库批号',
    `FPayWay` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结算方式编码',
    `FPayWayName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结算方式名称',
    `FPayMoney` decimal(18, 6) NULL DEFAULT NULL COMMENT '回款金额',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_at_sale_line_fentryid`(`FEntryID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '销售明细表' ROW_FORMAT = Dynamic;

-- changeset zq:1.19

CREATE TABLE IF NOT EXISTS`at_sale_rec` (
    `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `FID` varchar(50) NOT NULL COMMENT '主表ID',
    `FBillNo` varchar(30) DEFAULT NULL COMMENT '单据编码',
    `FSalesNo` varchar(30) NOT NULL COMMENT '销售单号',
    `FOrgID` varchar(20) DEFAULT NULL COMMENT '回款门店ID',
    `FOrgNumber` varchar(20) DEFAULT NULL COMMENT '回款门店编码',
    `FOrgName` varchar(50) DEFAULT NULL COMMENT '回款门店名称',
    `FDate` date DEFAULT NULL COMMENT '业务日期',
    `FSaleType` varchar(20) DEFAULT NULL COMMENT '销售类型编码',
    `FSaleTypeName` varchar(50) DEFAULT NULL COMMENT '销售类型名称',
    `FSrcEntryID` varchar(30) DEFAULT NULL COMMENT '销售明细内码',
    `FSetType` varchar(20) DEFAULT NULL COMMENT '结算方式编码',
    `FSetTypeName` varchar(50) DEFAULT NULL COMMENT '结算方式名称',
    `FPayMent` varchar(50) DEFAULT NULL COMMENT '支付方式编码',
    `FPayMentName` varchar(50) DEFAULT NULL COMMENT '支付方式名称',
    `FMemberID` varchar(20) DEFAULT NULL COMMENT '会员编码',
    `FPayMoney` decimal(18,2) DEFAULT NULL COMMENT '回款金额',
    `FRemark` varchar(200) DEFAULT NULL COMMENT '备注',
    `FCreateBy` varchar(20) DEFAULT NULL COMMENT '创建人',
    `FCreateDate` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `FUpdateBy` varchar(20) DEFAULT NULL COMMENT '修改人',
    `FUpdateDate` datetime DEFAULT NULL COMMENT '修改时间',
    `sync_flag` varchar(10) DEFAULT '0' COMMENT '同步标志',
    `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `uk_fsalesno` (`FSalesNo`),
    KEY `idx_query_org_date` (`FOrgID`,`FDate`),
    KEY `idx_query_date_type` (`FDate`,`FSaleType`),
    KEY `idx_sync` (`sync_flag`,`sync_time`),
    KEY `idx_bill` (`FBillNo`),
    KEY `idx_fid` (`FID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售回款记录表';

-- changeset zq:1.20
CREATE TABLE IF NOT EXISTS  `at_service_card`  (
    `ID` bigint NOT NULL AUTO_INCREMENT,
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FBillNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FBillType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FDate` datetime NULL DEFAULT NULL,
    `FOrgID` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOrgNumber` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOrgName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FRemark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    `FPayID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FPayWay` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FPayDate` datetime NULL DEFAULT NULL,
    `FPayMoney` decimal(18, 2) NULL DEFAULT NULL,
    `FCycleID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `sync_flag` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `sync_time` datetime NULL DEFAULT NULL,
    `FCreateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FCreateDate` datetime NULL DEFAULT NULL,
    `FUpdateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FUpdateDate` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `uk_service_card_fid`(`FID` ASC) USING BTREE,
    UNIQUE INDEX `uk_service_card_fpayid`(`FPayID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- changeset zq:1.21
CREATE TABLE IF NOT EXISTS  `at_service_card_line`  (
    `ID` bigint NOT NULL AUTO_INCREMENT,
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FEntryID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FCardTypeID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FCardTypeName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FPrice` decimal(18, 2) NULL DEFAULT NULL,
    `FQty` decimal(10, 2) NULL DEFAULT NULL,
    `FTotalPrice` decimal(18, 2) NULL DEFAULT NULL,
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `uk_service_card_line_fentryid`(`FEntryID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- changeset zq:1.22
CREATE TABLE IF NOT EXISTS  `at_service_cost`  (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `FID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务消费ID',
    `FType` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务消费类型',
    `FDate` datetime NULL DEFAULT NULL COMMENT '消费日期',
    `FCardID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务卡卡号',
    `FCardName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务卡名称',
    `FServiceTypeID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务类别编码',
    `FServiceTypeName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务类别名称',
    `FBuyStoreID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购卡门店ID',
    `FBuyStoreNumber` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购卡门店编码',
    `FBuyStoreName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购卡门店名称',
    `FConsumeStoreID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消费门店ID',
    `FConsumeStoreNumber` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消费门店编码',
    `FConsumeStoreName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消费门店名称',
    `FMoney` decimal(18, 2) NULL DEFAULT NULL COMMENT '消费金额',
    `FCrossStore` tinyint(1) NULL DEFAULT NULL COMMENT '是否跨店消费',
    `FGoodsStock` decimal(18, 2) NULL DEFAULT NULL COMMENT '服务成本',
    `FCreateBy` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
    `FCreateDate` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `FUpdateBy` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
    `FUpdateDate` datetime NULL DEFAULT NULL COMMENT '修改时间',
    `mark` tinyint(1) NOT NULL DEFAULT 0 COMMENT '标记位',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_fid`(`FID` ASC) USING BTREE COMMENT '服务消费ID唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务消费表' ROW_FORMAT = Dynamic;

-- changeset zq:1.23
CREATE TABLE IF NOT EXISTS  `at_stock_take`  (
    `ID` bigint NOT NULL AUTO_INCREMENT,
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FBillType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FDate` datetime NULL DEFAULT NULL,
    `FOrgID` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOrgNumber` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOrgName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FRemark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    `FCreateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FCreateDate` datetime NULL DEFAULT NULL,
    `FUpdateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FUpdateDate` datetime NULL DEFAULT NULL,
    `sync_flag` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `sync_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `uk_transfer_fid`(`FID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- changeset zq:1.24
CREATE TABLE IF NOT EXISTS  `at_stock_take_line`  (
    `ID` bigint NOT NULL AUTO_INCREMENT,
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FEntryID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FMaterialID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialTypeNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialTypeName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FUnit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FYkQty` decimal(18, 2) NULL DEFAULT NULL,
    `FYkPrice` decimal(18, 2) NULL DEFAULT NULL,
    `FYkAmount` decimal(18, 2) NULL DEFAULT NULL,
    `FTzQty` decimal(18, 2) NULL DEFAULT NULL,
    `FTzPrice` decimal(18, 2) NULL DEFAULT NULL,
    `FTzAmount` decimal(18, 2) NULL DEFAULT NULL,
    `FCGPrice` decimal(18, 2) NULL DEFAULT NULL,
    `FStockID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FStockNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FStockName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FBatch` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FNote` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    `FZFQty` decimal(18, 2) NULL DEFAULT NULL,
    `FQty` decimal(18, 2) NULL DEFAULT NULL,
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `uk_transfer_line_entryid`(`FEntryID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- changeset zq:1.25
CREATE TABLE IF NOT EXISTS `at_store_tran`  (
    `ID` bigint NOT NULL AUTO_INCREMENT,
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FDate` datetime NULL DEFAULT NULL,
    `FOutOrgID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOutOrgNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOutOrgName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FInOrgID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FInOrgNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FInOrgName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FRemark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    `FCreateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FCreateDate` datetime NULL DEFAULT NULL,
    `FUpdateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FUpdateDate` datetime NULL DEFAULT NULL,
    `sync_flag` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `sync_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `uk_transfer_bill_fid`(`FID` ASC) USING BTREE,
    INDEX `idx_date`(`FDate` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- changeset zq:1.26
CREATE TABLE IF NOT EXISTS  `at_store_tran_line`  (
    `ID` bigint NOT NULL AUTO_INCREMENT,
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FEntryID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `FDBType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialTypeNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FMaterialTypeName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FUnit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FQty` decimal(18, 2) NULL DEFAULT NULL,
    `FOutStockID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOutStockNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FOutStockName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FInStockID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FInStockNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FInStockName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FBatch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `FRate` decimal(5, 4) NULL DEFAULT NULL,
    `FInPrice` decimal(18, 2) NULL DEFAULT NULL,
    `FInAmount` decimal(18, 2) NULL DEFAULT NULL,
    `FNote` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `uk_transfer_entry_fentryid`(`FEntryID` ASC) USING BTREE,
    INDEX `idx_fid`(`FID` ASC) USING BTREE,
    CONSTRAINT `at_store_tran_line_ibfk_1` FOREIGN KEY (`FID`) REFERENCES `at_store_tran` (`FID`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;


-- changeset zq:1.27
CREATE TABLE IF NOT EXISTS  `at_service_box`  (
    `ID` bigint NOT NULL AUTO_INCREMENT,
    `FID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `FBillNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FOrgID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FOrgNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FOrgName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FDate` datetime NULL DEFAULT NULL,
    `FBillNoOut` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FTisQty` decimal(18, 4) NULL DEFAULT NULL,
    `FQty` decimal(18, 4) NULL DEFAULT NULL,
    `FPrice` decimal(18, 4) NULL DEFAULT NULL,
    `FTotalPrice` decimal(18, 4) NULL DEFAULT NULL,
    `FCard` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FMaterialID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FMaterialNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FMaterialName` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FMaterialTypeNumber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FMaterialTypeName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FUnit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FSaleBillNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FCreateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FCreateDate` datetime NULL DEFAULT NULL,
    `FUpdateBy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `FUpdateDate` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE INDEX `uk_sale_record_fid`(`FID` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

