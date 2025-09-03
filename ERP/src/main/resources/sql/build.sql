SET foreign_key_checks = 0;
DROP TABLE IF EXISTS `erp_account`;
CREATE TABLE `erp_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账号',
  `password` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2260000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_acct`;
CREATE TABLE `erp_acct` (
  `acct_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`acct_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_billing_hdr`;
CREATE TABLE `erp_billing_hdr` (
  `bill_id` bigint NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `dlv_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `billing_date` date DEFAULT NULL,
  `net` float DEFAULT NULL,
  `tax` float DEFAULT NULL,
  `gross` float DEFAULT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`bill_id`),
  KEY `dlv_id` (`dlv_id`),
  KEY `customer_id` (`customer_id`),
  KEY `status` (`status`),
  CONSTRAINT `erp_billing_hdr_ibfk_1` FOREIGN KEY (`dlv_id`) REFERENCES `erp_outbound_delivery` (`dlv_id`),
  CONSTRAINT `erp_billing_hdr_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_billing_hdr_ibfk_3` FOREIGN KEY (`status`) REFERENCES `erp_order_status` (`status_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1010 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_billing_item`;
CREATE TABLE `erp_billing_item` (
  `bill_id` bigint NOT NULL,
  `item_no` smallint NOT NULL,
  `mat_id` bigint NOT NULL,
  `quantity` smallint NOT NULL,
  `net_price` float NOT NULL,
  `tax_rate` smallint NOT NULL,
  PRIMARY KEY (`bill_id`,`item_no`),
  KEY `mat_id` (`mat_id`),
  CONSTRAINT `erp_billing_item_ibfk_1` FOREIGN KEY (`bill_id`) REFERENCES `erp_billing_hdr` (`bill_id`),
  CONSTRAINT `erp_billing_item_ibfk_2` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_company_code`;
CREATE TABLE `erp_company_code` (
  `code` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_condition_currency_mapping`;
CREATE TABLE `erp_condition_currency_mapping` (
  `mapping_id` bigint NOT NULL AUTO_INCREMENT COMMENT '映射ID',
  `cnty` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件类型代码',
  `default_city_unit` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '默认城市/货币单位',
  `description` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否为默认值',
  PRIMARY KEY (`mapping_id`),
  KEY `fk_condition_currency_cnty` (`cnty`),
  CONSTRAINT `fk_condition_currency_cnty` FOREIGN KEY (`cnty`) REFERENCES `erp_pricing_condition_type` (`cnty`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='条件类型与货币单位映射表';

DROP TABLE IF EXISTS `erp_contact`;
CREATE TABLE `erp_contact` (
  `contact_id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `title` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '称谓（Mr/Ms）',
  `first_name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '名',
  `last_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '姓',
  `cor_language` varchar(5) COLLATE utf8mb4_general_ci NOT NULL,
  `country` char(2) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`contact_id`),
  KEY `title` (`title`),
  KEY `cor_language` (`cor_language`),
  CONSTRAINT `erp_contact_ibfk_1` FOREIGN KEY (`title`) REFERENCES `erp_title` (`title_id`),
  CONSTRAINT `erp_contact_ibfk_2` FOREIGN KEY (`cor_language`) REFERENCES `erp_language` (`lang_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_currency`;
CREATE TABLE `erp_currency` (
  `currency_code` varchar(5) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`currency_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_customer`;
CREATE TABLE `erp_customer` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户编号',
  `title` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司类型（GR/PI）',
  `name` varchar(60) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司名称',
  `language` varchar(5) COLLATE utf8mb4_general_ci NOT NULL,
  `street` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `city` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `region` char(2) COLLATE utf8mb4_general_ci NOT NULL,
  `postal_code` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `country` char(2) COLLATE utf8mb4_general_ci NOT NULL,
  `company_code` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `reconciliation_account` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `sort_key` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `sales_org` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `channel` int NOT NULL,
  `division` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `currency` varchar(5) COLLATE utf8mb4_general_ci NOT NULL,
  `sales_district` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `price_group` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `customer_group` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `delivery_priority` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `shipping_condition` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `delivering_plant` bigint NOT NULL,
  `max_part_deliv` int NOT NULL,
  `incoterms` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `incoterms_location` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `payment_terms` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `acct_assignment` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `output_tax` int NOT NULL,
  `first_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `last_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `bp_type` varchar(10) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'org' COMMENT '业务伙伴类型：person/org/group',
  `search_term` varchar(60) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '搜索词',
  PRIMARY KEY (`customer_id`),
  KEY `title` (`title`),
  KEY `language` (`language`),
  KEY `company_code` (`company_code`),
  KEY `reconciliation_account` (`reconciliation_account`),
  KEY `sort_key` (`sort_key`),
  KEY `sales_org` (`sales_org`),
  KEY `channel` (`channel`),
  KEY `division` (`division`),
  KEY `currency` (`currency`),
  KEY `sales_district` (`sales_district`),
  KEY `price_group` (`price_group`),
  KEY `customer_group` (`customer_group`),
  KEY `delivery_priority` (`delivery_priority`),
  KEY `shipping_condition` (`shipping_condition`),
  KEY `delivering_plant` (`delivering_plant`),
  KEY `payment_terms` (`payment_terms`),
  KEY `acct_assignment` (`acct_assignment`),
  CONSTRAINT `erp_customer_ibfk_1` FOREIGN KEY (`title`) REFERENCES `erp_customer_title` (`title_id`),
  CONSTRAINT `erp_customer_ibfk_10` FOREIGN KEY (`sales_district`) REFERENCES `erp_sales_district` (`district_id`),
  CONSTRAINT `erp_customer_ibfk_11` FOREIGN KEY (`price_group`) REFERENCES `erp_price_group` (`group_id`),
  CONSTRAINT `erp_customer_ibfk_12` FOREIGN KEY (`customer_group`) REFERENCES `erp_customer_group` (`group_id`),
  CONSTRAINT `erp_customer_ibfk_13` FOREIGN KEY (`delivery_priority`) REFERENCES `erp_deliver_priority` (`priority_id`),
  CONSTRAINT `erp_customer_ibfk_14` FOREIGN KEY (`shipping_condition`) REFERENCES `erp_shipping_condition` (`condition_id`),
  CONSTRAINT `erp_customer_ibfk_15` FOREIGN KEY (`delivering_plant`) REFERENCES `erp_plant_name` (`plant_id`),
  CONSTRAINT `erp_customer_ibfk_16` FOREIGN KEY (`payment_terms`) REFERENCES `erp_payment_terms` (`term_id`),
  CONSTRAINT `erp_customer_ibfk_17` FOREIGN KEY (`acct_assignment`) REFERENCES `erp_acct` (`acct_id`),
  CONSTRAINT `erp_customer_ibfk_2` FOREIGN KEY (`language`) REFERENCES `erp_language` (`lang_id`),
  CONSTRAINT `erp_customer_ibfk_3` FOREIGN KEY (`company_code`) REFERENCES `erp_company_code` (`code`),
  CONSTRAINT `erp_customer_ibfk_4` FOREIGN KEY (`reconciliation_account`) REFERENCES `erp_reconciliation_account` (`account_id`),
  CONSTRAINT `erp_customer_ibfk_5` FOREIGN KEY (`sort_key`) REFERENCES `erp_sort_key` (`key_id`),
  CONSTRAINT `erp_customer_ibfk_6` FOREIGN KEY (`sales_org`) REFERENCES `erp_sales_org` (`org_id`),
  CONSTRAINT `erp_customer_ibfk_7` FOREIGN KEY (`channel`) REFERENCES `erp_distribution_channel` (`channel_id`),
  CONSTRAINT `erp_customer_ibfk_8` FOREIGN KEY (`division`) REFERENCES `erp_division` (`division_id`),
  CONSTRAINT `erp_customer_ibfk_9` FOREIGN KEY (`currency`) REFERENCES `erp_currency` (`currency_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1064 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_customer_group`;
CREATE TABLE `erp_customer_group` (
  `group_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_customer_title`;
CREATE TABLE `erp_customer_title` (
  `title_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `title_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`title_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_deliver_priority`;
CREATE TABLE `erp_deliver_priority` (
  `priority_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`priority_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_department`;
CREATE TABLE `erp_department` (
  `dept_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_distribution_channel`;
CREATE TABLE `erp_distribution_channel` (
  `channel_id` int NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_division`;
CREATE TABLE `erp_division` (
  `division_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`division_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_document_flow`;
CREATE TABLE `erp_document_flow` (
  `doc_id` bigint NOT NULL COMMENT '文档ID',
  `doc_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档类型',
  PRIMARY KEY (`doc_id`,`doc_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_function`;
CREATE TABLE `erp_function` (
  `function_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`function_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_good_issue`;
CREATE TABLE `erp_good_issue` (
  `gi_id` bigint NOT NULL AUTO_INCREMENT COMMENT '发货ID',
  `dlv_id` bigint NOT NULL,
  `posting_date` date DEFAULT NULL,
  `mat_id` bigint DEFAULT NULL,
  `quantity` smallint DEFAULT NULL,
  `plant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`gi_id`),
  KEY `dlv_id` (`dlv_id`),
  KEY `mat_id` (`mat_id`),
  KEY `plant_id` (`plant_id`),
  CONSTRAINT `erp_good_issue_ibfk_1` FOREIGN KEY (`dlv_id`) REFERENCES `erp_outbound_delivery` (`dlv_id`),
  CONSTRAINT `erp_good_issue_ibfk_2` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`),
  CONSTRAINT `erp_good_issue_ibfk_3` FOREIGN KEY (`plant_id`) REFERENCES `erp_plant_name` (`plant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_inquiry`;
CREATE TABLE `erp_inquiry` (
  `inquiry_id` bigint NOT NULL AUTO_INCREMENT COMMENT '询价单ID',
  `cust_id` bigint NOT NULL,
  `inquiry_type` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sls_org` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sales_district` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `division` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sold_tp` bigint NOT NULL,
  `ship_tp` bigint NOT NULL,
  `cust_ref` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `customer_reference_date` date DEFAULT NULL,
  `valid_from_date` date DEFAULT NULL,
  `valid_to_date` date DEFAULT NULL,
  `probability` float DEFAULT NULL,
  `net_value` float DEFAULT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `req_deliv_date` date DEFAULT NULL COMMENT '送货日期',
  PRIMARY KEY (`inquiry_id`),
  KEY `cust_id` (`cust_id`),
  KEY `sls_org` (`sls_org`),
  KEY `sales_district` (`sales_district`),
  KEY `division` (`division`),
  KEY `sold_tp` (`sold_tp`),
  KEY `ship_tp` (`ship_tp`),
  CONSTRAINT `erp_inquiry_ibfk_1` FOREIGN KEY (`cust_id`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_inquiry_ibfk_2` FOREIGN KEY (`sls_org`) REFERENCES `erp_sales_org` (`org_id`),
  CONSTRAINT `erp_inquiry_ibfk_3` FOREIGN KEY (`sales_district`) REFERENCES `erp_sales_district` (`district_id`),
  CONSTRAINT `erp_inquiry_ibfk_4` FOREIGN KEY (`division`) REFERENCES `erp_division` (`division_id`),
  CONSTRAINT `erp_inquiry_ibfk_5` FOREIGN KEY (`sold_tp`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_inquiry_ibfk_6` FOREIGN KEY (`ship_tp`) REFERENCES `erp_customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_inquiry_item`;
CREATE TABLE `erp_inquiry_item` (
  `inquiry_id` bigint NOT NULL,
  `item_no` smallint NOT NULL,
  `mat_id` bigint NOT NULL,
  `quantity` smallint NOT NULL,
  `net_price` float NOT NULL,
  `item_value` float NOT NULL,
  `plant_id` bigint NOT NULL,
  `su` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `item_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目代码',
  `material_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料代码',
  `order_quantity_str` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单数量字符串',
  `order_quantity_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单数量单位',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料描述',
  `req_deliv_date` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '要求交货日期',
  `net_value_str` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '净值字符串',
  `net_value_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '净值单位',
  `tax_value_str` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '税值字符串',
  `tax_value_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '税值单位',
  `pricing_date` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '定价日期',
  `order_probability` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单概率',
  `pricing_elements_json` text COLLATE utf8mb4_general_ci COMMENT '定价元素JSON数据',
  PRIMARY KEY (`inquiry_id`,`item_no`),
  KEY `mat_id` (`mat_id`),
  KEY `plant_id` (`plant_id`),
  KEY `idx_inquiry_item_material_code` (`material_code`),
  KEY `idx_inquiry_item_item_code` (`item_code`),
  CONSTRAINT `erp_inquiry_item_ibfk_1` FOREIGN KEY (`inquiry_id`) REFERENCES `erp_inquiry` (`inquiry_id`),
  CONSTRAINT `erp_inquiry_item_ibfk_2` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`),
  CONSTRAINT `erp_inquiry_item_ibfk_3` FOREIGN KEY (`plant_id`) REFERENCES `erp_plant_name` (`plant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='询价单项目表 - 扩展支持完整ItemValidation字段';

DROP TABLE IF EXISTS `erp_item`;
CREATE TABLE `erp_item` (
  `document_id` bigint NOT NULL COMMENT '文档ID (inquiry_id/quotation_id/so_id/dlv_id/bill_id)',
  `document_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档类型 (inquiry/quotation/sales/outbound/billdoc)',
  `item_no` smallint NOT NULL COMMENT '项目号',
  `mat_id` bigint NOT NULL COMMENT '物料ID',
  `quantity` smallint NOT NULL COMMENT '数量',
  `net_price` float NOT NULL COMMENT '净价',
  `item_value` float NOT NULL COMMENT '项目总值',
  `plant_id` bigint NOT NULL COMMENT '工厂ID',
  `su` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '单位',
  `item_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目代码',
  `material_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料代码',
  `order_quantity_str` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单数量字符串',
  `order_quantity_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单数量单位',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料描述',
  `req_deliv_date` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '要求交货日期',
  `net_value_str` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '净值字符串',
  `net_value_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '净值单位',
  `tax_value_str` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '税值字符串',
  `tax_value_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '税值单位',
  `pricing_date` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '定价日期',
  `order_probability` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单概率',
  `pricing_elements_json` text COLLATE utf8mb4_general_ci COMMENT '定价元素JSON数据',
  `created_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`document_id`,`document_type`,`item_no`),
  KEY `mat_id` (`mat_id`),
  KEY `plant_id` (`plant_id`),
  KEY `idx_item_material_code` (`material_code`),
  KEY `idx_item_item_code` (`item_code`),
  KEY `idx_item_document_type` (`document_type`),
  KEY `idx_item_document_id_type` (`document_id`, `document_type`),
  CONSTRAINT `erp_item_ibfk_1` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`),
  CONSTRAINT `erp_item_ibfk_2` FOREIGN KEY (`plant_id`) REFERENCES `erp_plant_name` (`plant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统一项目表 - 支持所有业务类型的完整ItemValidation字段';

DROP TABLE IF EXISTS `erp_item_validation_config`;
CREATE TABLE `erp_item_validation_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `application_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '应用类型（inquiry/quotation/so/billing）',
  `endpoint_path` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '端点路径',
  `tax_rate` decimal(5,4) DEFAULT '0.1300' COMMENT '默认税率',
  `default_currency` varchar(5) COLLATE utf8mb4_general_ci DEFAULT 'CNY' COMMENT '默认货币',
  `default_probability` varchar(10) COLLATE utf8mb4_general_ci DEFAULT '100' COMMENT '默认订单概率',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否有效',
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`),
  CONSTRAINT `chk_tax_rate` CHECK (((`tax_rate` >= 0) and (`tax_rate` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物品验证配置表';

DROP TABLE IF EXISTS `erp_item_validation_log`;
CREATE TABLE `erp_item_validation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `session_id` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会话ID',
  `application_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '应用类型',
  `request_data` json DEFAULT NULL COMMENT '请求数据',
  `response_data` json DEFAULT NULL COMMENT '响应数据',
  `validation_result` tinyint(1) DEFAULT NULL COMMENT '验证结果',
  `error_message` text COLLATE utf8mb4_general_ci COMMENT '错误信息',
  `processing_time_ms` bigint DEFAULT NULL COMMENT '处理时间(毫秒)',
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物品验证日志表';

DROP TABLE IF EXISTS `erp_language`;
CREATE TABLE `erp_language` (
  `lang_id` varchar(5) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`lang_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_management`;
CREATE TABLE `erp_management` (
  `level_id` int NOT NULL,
  `description` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_material`;
CREATE TABLE `erp_material` (
  `mat_id` bigint NOT NULL AUTO_INCREMENT COMMENT '物料ID',
  `mat_desc` varchar(60) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料描述',
  `division` char(2) COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品线',
  `base_uom` varchar(60) COLLATE utf8mb4_general_ci NOT NULL COMMENT '基本单位',
  `srd_price` float NOT NULL COMMENT '标准价格',
  `tax_classification` varchar(10) COLLATE utf8mb4_general_ci DEFAULT 'TAXABLE' COMMENT '税务分类',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否有效',
  `price_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT 'CNY' COMMENT '价格单位',
  PRIMARY KEY (`mat_id`),
  KEY `idx_material_active` (`is_active`),
  CONSTRAINT `chk_std_price_positive` CHECK ((`srd_price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=10006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 扩展物料基础重量与体积字段（按需新增）
ALTER TABLE `erp_material`
  ADD COLUMN `base_gross_weight` decimal(13,3) DEFAULT NULL COMMENT '基础毛重',
  ADD COLUMN `base_gross_weight_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '基础毛重单位',
  ADD COLUMN `base_net_weight` decimal(13,3) DEFAULT NULL COMMENT '基础净重',
  ADD COLUMN `base_net_weight_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '基础净重单位',
  ADD COLUMN `base_volume` decimal(13,3) DEFAULT NULL COMMENT '基础体积',
  ADD COLUMN `base_volume_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '基础体积单位';


DROP TABLE IF EXISTS `erp_material_document`;
CREATE TABLE `erp_material_document` (
  `material_document_id` bigint NOT NULL AUTO_INCREMENT COMMENT '物料凭证ID',
  `material_document` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料凭证号码',
  `material_document_year` varchar(4) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物料凭证年份',
  `plant_id` bigint NOT NULL COMMENT '工厂ID',
  `posting_date` date NOT NULL COMMENT '过账日期',
  `document_date` date NOT NULL COMMENT '凭证日期',
  `created_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`material_document_id`),
  UNIQUE KEY `uk_material_doc_year` (`material_document`,`material_document_year`),
  KEY `plant_id` (`plant_id`),
  CONSTRAINT `erp_material_document_ibfk_1` FOREIGN KEY (`plant_id`) REFERENCES `erp_plant_name` (`plant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_material_document_item`;
CREATE TABLE `erp_material_document_item` (
  `material_document_id` bigint NOT NULL COMMENT '物料凭证ID',
  `item_no` smallint NOT NULL COMMENT '项目编号',
  `mat_id` bigint NOT NULL COMMENT '物料ID',
  `quantity` decimal(13,3) NOT NULL COMMENT '数量',
  `unit` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '单位',
  `movement_type` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '移动类型',
  `storage_loc` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '库存地点',
  PRIMARY KEY (`material_document_id`,`item_no`),
  KEY `mat_id` (`mat_id`),
  KEY `storage_loc` (`storage_loc`),
  CONSTRAINT `erp_material_document_item_ibfk_1` FOREIGN KEY (`material_document_id`) REFERENCES `erp_material_document` (`material_document_id`),
  CONSTRAINT `erp_material_document_item_ibfk_2` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`),
  CONSTRAINT `erp_material_document_item_ibfk_3` FOREIGN KEY (`storage_loc`) REFERENCES `erp_storage_location` (`loc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_material_document_process`;
CREATE TABLE `erp_material_document_process` (
  `material_document_id` bigint NOT NULL COMMENT '物料凭证ID',
  `dlv_id` bigint DEFAULT NULL COMMENT '交货单ID',
  `bill_id` bigint DEFAULT NULL COMMENT '会计凭证ID',
  `so_id` bigint DEFAULT NULL COMMENT '销售订单ID',
  PRIMARY KEY (`material_document_id`),
  KEY `dlv_id` (`dlv_id`),
  KEY `bill_id` (`bill_id`),
  KEY `so_id` (`so_id`),
  CONSTRAINT `erp_material_document_process_ibfk_1` FOREIGN KEY (`material_document_id`) REFERENCES `erp_material_document` (`material_document_id`),
  CONSTRAINT `erp_material_document_process_ibfk_2` FOREIGN KEY (`dlv_id`) REFERENCES `erp_outbound_delivery` (`dlv_id`),
  CONSTRAINT `erp_material_document_process_ibfk_3` FOREIGN KEY (`bill_id`) REFERENCES `erp_billing_hdr` (`bill_id`),
  CONSTRAINT `erp_material_document_process_ibfk_4` FOREIGN KEY (`so_id`) REFERENCES `erp_sales_order_hdr` (`so_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_movement_type`;
CREATE TABLE `erp_movement_type` (
  `movement_type` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '移动类型代码',
  `description` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '移动类型描述',
  `movement_indicator` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '移动指示符（收货/发货/转储等）',
  PRIMARY KEY (`movement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_order_status`;
CREATE TABLE `erp_order_status` (
  `status_code` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`status_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_outbound_delivery`;
CREATE TABLE `erp_outbound_delivery` (
  `dlv_id` bigint NOT NULL AUTO_INCREMENT COMMENT '交货单ID',
  `so_id` bigint NOT NULL COMMENT '关联销售订单ID',
  `ship_tp` bigint NOT NULL COMMENT '送达方客户ID',
  `shipping_point` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '装运点/发运点',
  `posted` tinyint(1) DEFAULT '0' COMMENT '是否已过账',
  `ready_to_post` tinyint(1) DEFAULT '0' COMMENT '是否准备好过账',
  `actual_gi_date` date DEFAULT NULL COMMENT '实际发货日期',
  `planned_gi_date` date DEFAULT NULL COMMENT '计划发货日期(来自SO)',
  `actual_date` date DEFAULT NULL COMMENT '实际日期（过账时）',
  `loading_date` date DEFAULT NULL COMMENT '装载日期(来自SO)',
  `delivery_date` date DEFAULT NULL COMMENT '交货日期(来自SO)',
  `picking_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'IN_PROGRESS' COMMENT '拣配状态',
  `overall_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'IN_PROGRESS' COMMENT '整体状态',
  `gi_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'IN_PROGRESS' COMMENT '发货状态',
  `address` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '送达地址',
  `gross_weight` decimal(13,3) DEFAULT NULL COMMENT '总毛重',
  `gross_weight_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '毛重单位',
  `net_weight` decimal(13,3) DEFAULT NULL COMMENT '总净重',
  `net_weight_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '净重单位',
  `volume` decimal(13,3) DEFAULT NULL COMMENT '总体积',
  `volume_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '体积单位',
  `priority` varchar(50) COLLATE utf8mb4_general_ci DEFAULT 'Normal Items' COMMENT '优先级',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dlv_id`),
  KEY `so_id` (`so_id`),
  KEY `ship_tp` (`ship_tp`),
  CONSTRAINT `erp_outbound_delivery_ibfk_1` FOREIGN KEY (`so_id`) REFERENCES `erp_sales_order_hdr` (`so_id`),
  CONSTRAINT `erp_outbound_delivery_ibfk_2` FOREIGN KEY (`ship_tp`) REFERENCES `erp_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_outbound_item`;
CREATE TABLE `erp_outbound_item` (
  `dlv_id` bigint NOT NULL COMMENT '出库交货单ID',
  `item_no` smallint NOT NULL COMMENT '行项目号',
  `ref_document_id` bigint NOT NULL COMMENT '引用的erp_item.document_id (销售订单ID)',
  `ref_document_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '引用的erp_item.document_type (固定为sales)',
  `ref_item_no` smallint NOT NULL COMMENT '引用的erp_item.item_no (销售订单行项目号)',
  `picking_quantity` decimal(13,3) NOT NULL DEFAULT 0 COMMENT '拣配数量',
  `picking_status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Completed' COMMENT '拣配状态',
  `confirmation_status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Not Confirmed' COMMENT '确认状态',
  `item_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Standard' COMMENT '项目类型',
  `conversion_rate` decimal(13,3) NOT NULL DEFAULT 1.000 COMMENT '转换率',
  `storage_loc` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '库存地点(用户输入)',
  `storage_bin` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '储位(用户输入)',
  `gross_weight` decimal(13,3) DEFAULT NULL COMMENT '毛重(数值)',
  `gross_weight_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '毛重单位',
  `net_weight` decimal(13,3) DEFAULT NULL COMMENT '净重(数值)',
  `net_weight_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '净重单位',
  `volume` decimal(13,3) DEFAULT NULL COMMENT '体积(数值)',
  `volume_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '体积单位',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dlv_id`,`item_no`),
  KEY `idx_outbound_item_ref` (`ref_document_id`,`ref_document_type`,`ref_item_no`),
  KEY `idx_outbound_item_storage_loc` (`storage_loc`),
  CONSTRAINT `fk_outbound_item_dlv` FOREIGN KEY (`dlv_id`) REFERENCES `erp_outbound_delivery` (`dlv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出库交货单物品表，引用销售订单的统一erp_item记录';

DROP TABLE IF EXISTS `erp_payment`;
CREATE TABLE `erp_payment` (
  `pay_id` bigint NOT NULL AUTO_INCREMENT COMMENT '付款ID',
  `bill_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `posting_date` date DEFAULT NULL,
  `amount` float DEFAULT NULL,
  `currency` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `clearing_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`pay_id`),
  KEY `bill_id` (`bill_id`),
  KEY `customer_id` (`customer_id`),
  KEY `currency` (`currency`),
  CONSTRAINT `erp_payment_ibfk_1` FOREIGN KEY (`bill_id`) REFERENCES `erp_billing_hdr` (`bill_id`),
  CONSTRAINT `erp_payment_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_payment_ibfk_3` FOREIGN KEY (`currency`) REFERENCES `erp_currency` (`currency_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_payment_terms`;
CREATE TABLE `erp_payment_terms` (
  `term_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`term_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_plant_name`;
CREATE TABLE `erp_plant_name` (
  `plant_id` bigint NOT NULL,
  `plant_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `city` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`plant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_price_group`;
CREATE TABLE `erp_price_group` (
  `group_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_pricing_condition_type`;
CREATE TABLE `erp_pricing_condition_type` (
  `cnty` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件类型代码',
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件类型名称',
  `description` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '条件描述',
  `is_percentage` tinyint(1) DEFAULT '0' COMMENT '是否为百分比类型',
  `default_currency` varchar(5) COLLATE utf8mb4_general_ci DEFAULT 'CNY' COMMENT '默认货币',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否有效',
  PRIMARY KEY (`cnty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定价条件类型映射表';

DROP TABLE IF EXISTS `erp_pricing_element`;
CREATE TABLE `erp_pricing_element` (
  `element_id` bigint NOT NULL AUTO_INCREMENT COMMENT '定价元素ID',
  `document_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档类型 (inquiry/quotation/outbound/sales_order/billdoc)',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `item_no` smallint NOT NULL COMMENT '项目号',
  `cnty` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国家',
  `condition_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件名称',
  `amount` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '金额（可能是数值或百分比）',
  `city` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '城市/货币',
  `per_value` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '每(例如: "1")',
  `uom` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位(例如: "EA")',
  `condition_value` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '条件值',
  `currency` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '货币',
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  `numC` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数量条件',
  `ato_mts_component` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ATO/MTS组件',
  `oun` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'OUn',
  `ccon_de` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'CConDe',
  `un` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Un',
  `condition_value2` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '条件值2',
  `cd_cur` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'CdCur',
  `stat` tinyint(1) DEFAULT '1' COMMENT '状态布尔值',
  PRIMARY KEY (`element_id`),
  KEY `idx_pricing_element_document` (`document_type`,`document_id`),
  KEY `idx_pricing_element_cnty` (`cnty`),
  CONSTRAINT `fk_pricing_condition_type` FOREIGN KEY (`cnty`) REFERENCES `erp_pricing_condition_type` (`cnty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_pricing_element_backup`;
CREATE TABLE `erp_pricing_element_backup` (
  `element_id` bigint NOT NULL DEFAULT '0' COMMENT '定价元素ID',
  `document_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档类型 (inquiry/quotation/sales_order)',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `item_no` smallint NOT NULL COMMENT '项目号',
  `cnty` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '国家',
  `condition_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件名称',
  `amount` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '金额（可能是数值或百分比）',
  `city` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '城市/货币',
  `per_value` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '每(例如: "1")',
  `uom` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位(例如: "EA")',
  `condition_value` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '条件值',
  `currency` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '货币',
  `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  `numC` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数量条件',
  `ato_mts_component` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ATO/MTS组件',
  `oun` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'OUn',
  `ccon_de` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'CConDe',
  `un` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Un',
  `condition_value2` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '条件值2',
  `cd_cur` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'CdCur',
  `stat` tinyint(1) DEFAULT '1' COMMENT '状态布尔值'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_quotation`;
CREATE TABLE `erp_quotation` (
  `quotation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '报价单ID',
  `reference_inquiry_id` bigint DEFAULT NULL,
  `cust_id` bigint NOT NULL,
  `inquiry_type` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sls_org` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sales_district` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `division` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sold_tp` bigint DEFAULT NULL,
  `ship_tp` bigint DEFAULT NULL,
  `cust_ref` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `customer_reference_date` date DEFAULT NULL,
  `valid_from_date` date DEFAULT NULL,
  `valid_to_date` date DEFAULT NULL,
  `probability` float DEFAULT NULL,
  `net_value` float DEFAULT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `currency` varchar(5) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'USD' COMMENT '货币代码',
  PRIMARY KEY (`quotation_id`),
  KEY `reference_inquiry_id` (`reference_inquiry_id`),
  KEY `cust_id` (`cust_id`),
  KEY `sls_org` (`sls_org`),
  KEY `sales_district` (`sales_district`),
  KEY `division` (`division`),
  KEY `sold_tp` (`sold_tp`),
  KEY `ship_tp` (`ship_tp`),
  CONSTRAINT `erp_quotation_ibfk_1` FOREIGN KEY (`reference_inquiry_id`) REFERENCES `erp_inquiry` (`inquiry_id`),
  CONSTRAINT `erp_quotation_ibfk_2` FOREIGN KEY (`cust_id`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_quotation_ibfk_3` FOREIGN KEY (`sls_org`) REFERENCES `erp_sales_org` (`org_id`),
  CONSTRAINT `erp_quotation_ibfk_4` FOREIGN KEY (`sales_district`) REFERENCES `erp_sales_district` (`district_id`),
  CONSTRAINT `erp_quotation_ibfk_5` FOREIGN KEY (`division`) REFERENCES `erp_division` (`division_id`),
  CONSTRAINT `erp_quotation_ibfk_6` FOREIGN KEY (`sold_tp`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_quotation_ibfk_7` FOREIGN KEY (`ship_tp`) REFERENCES `erp_customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_quotation_item`;
CREATE TABLE `erp_quotation_item` (
  `quotation_id` bigint NOT NULL,
  `item_no` smallint NOT NULL,
  `mat_id` bigint NOT NULL,
  `quantity` smallint NOT NULL,
  `net_price` float NOT NULL,
  `item_discount_pct` int DEFAULT '0',
  `item_value` float NOT NULL,
  `plant_id` bigint NOT NULL,
  `su` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `cnty` smallint NOT NULL,
  `tax_value` float NOT NULL DEFAULT '0' COMMENT '税值',
  PRIMARY KEY (`quotation_id`,`item_no`),
  KEY `mat_id` (`mat_id`),
  KEY `plant_id` (`plant_id`),
  CONSTRAINT `erp_quotation_item_ibfk_1` FOREIGN KEY (`quotation_id`) REFERENCES `erp_quotation` (`quotation_id`),
  CONSTRAINT `erp_quotation_item_ibfk_2` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`),
  CONSTRAINT `erp_quotation_item_ibfk_3` FOREIGN KEY (`plant_id`) REFERENCES `erp_plant_name` (`plant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_reconciliation_account`;
CREATE TABLE `erp_reconciliation_account` (
  `account_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_relation`;
CREATE TABLE `erp_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系索引',
  `rel_category` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '关系类别',
  `bp1` bigint NOT NULL COMMENT '客户ID',
  `bp2` bigint NOT NULL COMMENT '联系人ID',
  `management` int NOT NULL COMMENT 'VIP等级',
  `department` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `function` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `valid_from` date NOT NULL,
  `valid_to` date NOT NULL,
  `customer_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户代码（customer类型专用）',
  `customer_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户名称（customer类型专用）',
  `contact_person` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人（customer类型专用）',
  `test_field` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '测试字段（ContactPerson/test类型专用）',
  `description` text COLLATE utf8mb4_general_ci COMMENT '描述（ContactPerson/test类型专用）',
  `extended_data` json DEFAULT NULL COMMENT '扩展数据（JSON格式存储动态字段）',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`relation_id`),
  KEY `bp1` (`bp1`),
  KEY `bp2` (`bp2`),
  KEY `management` (`management`),
  KEY `department` (`department`),
  KEY `function` (`function`),
  CONSTRAINT `erp_relation_ibfk_1` FOREIGN KEY (`bp1`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_relation_ibfk_2` FOREIGN KEY (`bp2`) REFERENCES `erp_contact` (`contact_id`),
  CONSTRAINT `erp_relation_ibfk_3` FOREIGN KEY (`management`) REFERENCES `erp_management` (`level_id`),
  CONSTRAINT `erp_relation_ibfk_4` FOREIGN KEY (`department`) REFERENCES `erp_department` (`dept_id`),
  CONSTRAINT `erp_relation_ibfk_5` FOREIGN KEY (`function`) REFERENCES `erp_function` (`function_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3043 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_sales_district`;
CREATE TABLE `erp_sales_district` (
  `district_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`district_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_sales_item`;
CREATE TABLE `erp_sales_item` (
  `so_id` bigint NOT NULL,
  `item_no` smallint NOT NULL,
  `mat_id` bigint NOT NULL,
  `plt_id` bigint NOT NULL,
  `storage_loc` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `quantity` smallint NOT NULL,
  `su` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `net_price` float NOT NULL,
  `discount_pct` smallint NOT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`so_id`,`item_no`),
  KEY `mat_id` (`mat_id`),
  KEY `plt_id` (`plt_id`),
  KEY `storage_loc` (`storage_loc`),
  KEY `status` (`status`),
  CONSTRAINT `erp_sales_item_ibfk_1` FOREIGN KEY (`so_id`) REFERENCES `erp_sales_order_hdr` (`so_id`),
  CONSTRAINT `erp_sales_item_ibfk_2` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`),
  CONSTRAINT `erp_sales_item_ibfk_3` FOREIGN KEY (`plt_id`) REFERENCES `erp_plant_name` (`plant_id`),
  CONSTRAINT `erp_sales_item_ibfk_4` FOREIGN KEY (`storage_loc`) REFERENCES `erp_storage_location` (`loc_id`),
  CONSTRAINT `erp_sales_item_ibfk_5` FOREIGN KEY (`status`) REFERENCES `erp_order_status` (`status_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_sales_order_hdr`;
CREATE TABLE `erp_sales_order_hdr` (
  `so_id` bigint NOT NULL AUTO_INCREMENT COMMENT '销售订单ID',
  `quote_id` bigint DEFAULT NULL,
  `customer_no` bigint DEFAULT NULL,
  `contact_id` bigint DEFAULT NULL,
  `doc_date` date DEFAULT NULL,
  `req_delivery_date` date DEFAULT NULL,
  `currency` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `net_value` float DEFAULT NULL,
  `tax_value` float DEFAULT NULL,
  `gross_value` float DEFAULT NULL,
  `incoterms` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `payment_terms` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cust_ref` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户参考',
  `customer_reference_date` date DEFAULT NULL COMMENT '客户参考日期',
  `sold_tp` bigint DEFAULT NULL COMMENT '售达方客户ID',
  `ship_tp` bigint DEFAULT NULL COMMENT '送达方客户ID',
  PRIMARY KEY (`so_id`),
  KEY `quote_id` (`quote_id`),
  KEY `customer_no` (`customer_no`),
  KEY `contact_id` (`contact_id`),
  KEY `currency` (`currency`),
  KEY `payment_terms` (`payment_terms`),
  CONSTRAINT `erp_sales_order_hdr_ibfk_1` FOREIGN KEY (`quote_id`) REFERENCES `erp_quotation` (`quotation_id`),
  CONSTRAINT `erp_sales_order_hdr_ibfk_2` FOREIGN KEY (`customer_no`) REFERENCES `erp_customer` (`customer_id`),
  CONSTRAINT `erp_sales_order_hdr_ibfk_3` FOREIGN KEY (`contact_id`) REFERENCES `erp_contact` (`contact_id`),
  CONSTRAINT `erp_sales_order_hdr_ibfk_4` FOREIGN KEY (`currency`) REFERENCES `erp_currency` (`currency_code`),
  CONSTRAINT `erp_sales_order_hdr_ibfk_5` FOREIGN KEY (`payment_terms`) REFERENCES `erp_payment_terms` (`term_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6010 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_sales_org`;
CREATE TABLE `erp_sales_org` (
  `org_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_shipping_condition`;
CREATE TABLE `erp_shipping_condition` (
  `condition_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`condition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_sort_key`;
CREATE TABLE `erp_sort_key` (
  `key_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`key_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_stock`;
CREATE TABLE `erp_stock` (
  `plant_id` bigint NOT NULL COMMENT '工厂ID',
  `mat_id` bigint NOT NULL COMMENT '物料ID',
  `bp_id` bigint NOT NULL COMMENT '客户ID',
  `storage_loc` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '库存地点',
  `qty_on_hand` float NOT NULL COMMENT '在手量',
  `qty_committed` float NOT NULL COMMENT '承诺量',
  PRIMARY KEY (`plant_id`,`mat_id`,`bp_id`,`storage_loc`),
  KEY `erp_stock_ibfk_2` (`mat_id`),
  KEY `erp_stock_ibfk_3` (`bp_id`),
  KEY `erp_stock_ibfk_4` (`storage_loc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_pricing_element_key`;
CREATE TABLE `erp_pricing_element_key` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '定价元素名称代码',
  `description` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '描述',
  `default_unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '默认单位，NULL时跟随物品的netValueUnit',
  `rule` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '计算规则，前序遍历表达式',
  `sort_key` int NOT NULL COMMENT '排序键，数字小的先计算',
  `config` json DEFAULT ('{}') COMMENT '配置信息JSON',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pricing_element_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定价元素类型配置表';

-- 插入默认的定价元素类型
INSERT INTO `erp_pricing_element_key` (`id`, `name`, `description`, `default_unit`, `rule`, `sort_key`, `config`) VALUES
(1, 'BASE', '基础价格', NULL, '/{d}{p}', 1, '{}'),
(2, 'DCBV', '按数值减价', NULL, '-{x}/{d}{p}', 2, '{}'),
(3, 'DCBP', '百分比减价', '%', '*{x}-{1}/{d}{100}', 3, '{}');

DROP TABLE IF EXISTS `erp_storage_location`;
CREATE TABLE `erp_storage_location` (
  `loc_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`loc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `erp_title`;
CREATE TABLE `erp_title` (
  `title_id` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `title_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`title_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET foreign_key_checks = 1;
