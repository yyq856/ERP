-- 物品验证服务测试数据
-- 插入物料测试数据

INSERT INTO erp_material (mat_id, mat_desc, division, base_uom, srd_price) VALUES
(1001, '标准电脑主板', '01', 'PC', 1200.00),
(1002, '高性能显卡', '01', 'PC', 2800.00),
(1003, '内存条8GB', '01', 'PC', 450.00),
(1004, '固态硬盘512GB', '01', 'PC', 680.00),
(1005, '机械键盘', '02', 'PC', 320.00),
(1006, '无线鼠标', '02', 'PC', 120.00),
(1007, '27寸显示器', '02', 'PC', 1500.00),
(1008, 'CPU处理器', '01', 'PC', 2200.00),
(1009, '电源模块', '01', 'PC', 380.00),
(1010, '散热器', '01', 'PC', 250.00);

-- 插入工厂信息（如果不存在）
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, city) VALUES
(1001, '北京生产基地', '北京'),
(1002, '上海制造中心', '上海'),
(1003, '深圳组装厂', '深圳');

-- 插入货币信息（如果不存在）
INSERT IGNORE INTO erp_currency (currency_code, name) VALUES
('CNY', '人民币'),
('USD', '美元'),
('EUR', '欧元');

-- 插入公司代码（如果不存在）
INSERT IGNORE INTO erp_company_code (code, name) VALUES
('1000', '总公司'),
('2000', '分公司A'),
('3000', '分公司B');

-- 插入语言信息（如果不存在）
INSERT IGNORE INTO erp_language (lang_id, name) VALUES
('ZH', '中文'),
('EN', '英文'),
('JP', '日文');

-- 插入客户类型（如果不存在）
INSERT IGNORE INTO erp_customer_title (title_id, title_name) VALUES
('GR', '集团公司'),
('PI', '个人'),
('CO', '合作伙伴');

-- 插入调账账户（如果不存在）
INSERT IGNORE INTO erp_reconciliation_account (account_id, name) VALUES
('140000', '应收账款'),
('210000', '应付账款'),
('300000', '预付款项');

-- 插入排序键（如果不存在）
INSERT IGNORE INTO erp_sort_key (key_id, name) VALUES
('001', '按名称'),
('002', '按代码'),
('003', '按地区');

-- 插入销售组织（如果不存在）
INSERT IGNORE INTO erp_sales_org (org_id, name) VALUES
('1000', '国内销售'),
('2000', '国际销售'),
('3000', '在线销售');

-- 插入分销渠道（如果不存在）
INSERT IGNORE INTO erp_distribution_channel (channel_id, name) VALUES
(10, '直销'),
(20, '代理商'),
(30, '电商平台');

-- 插入产品线（如果不存在）
INSERT IGNORE INTO erp_division (division_id, name) VALUES
('01', '硬件产品'),
('02', '外设产品'),
('03', '软件产品');

-- 插入销售区域（如果不存在）
INSERT IGNORE INTO erp_sales_district (district_id, name) VALUES
('BJ', '北京区'),
('SH', '上海区'),
('GD', '广东区');

-- 插入价格组（如果不存在）
INSERT IGNORE INTO erp_price_group (group_id, name) VALUES
('01', '标准价格'),
('02', '折扣价格'),
('03', 'VIP价格');

-- 插入客户组（如果不存在）
INSERT IGNORE INTO erp_customer_group (group_id, name) VALUES
('01', '零售客户'),
('02', '企业客户'),
('03', '政府客户');

-- 插入交货优先级（如果不存在）
INSERT IGNORE INTO erp_deliver_priority (priority_id, name) VALUES
('01', '普通'),
('02', '紧急'),
('03', '特急');

-- 插入运输条件（如果不存在）
INSERT IGNORE INTO erp_shipping_condition (condition_id, name) VALUES
('01', '标准运输'),
('02', '快递'),
('03', '专车配送');

-- 插入账户分配（如果不存在）
INSERT IGNORE INTO erp_acct (acct_id, name) VALUES
('01', '销售收入'),
('02', '服务收入'),
('03', '其他收入');

-- 插入付款条件（如果不存在）
INSERT IGNORE INTO erp_payment_terms (term_id, name) VALUES
('01', '现金'),
('02', '30天账期'),
('03', '60天账期');