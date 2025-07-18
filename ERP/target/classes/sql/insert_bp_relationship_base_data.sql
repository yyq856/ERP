-- 业务伙伴关系基础数据插入脚本
-- 执行顺序很重要，因为有外键依赖关系

-- 1. 插入管理级别数据
INSERT INTO erp_management (level_id, description) VALUES 
(1, 'Standard'),
(2, 'VIP'),
(3, 'Premium'),
(4, 'Gold'),
(5, 'Platinum')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 2. 插入部门数据
INSERT INTO erp_department (dept_id, name) VALUES 
('01', 'Sales Department'),
('02', 'Marketing Department'),
('03', 'Customer Service'),
('04', 'Technical Support'),
('05', 'Finance Department')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. 插入功能数据
INSERT INTO erp_function (function_id, name) VALUES 
('01', 'Manager'),
('02', 'Sales Representative'),
('03', 'Customer Service Rep'),
('04', 'Technical Specialist'),
('05', 'Finance Officer')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 4. 插入一些测试用的客户数据（如果不存在的话）
-- 注意：这需要先有其他基础数据，所以我们先插入最基本的依赖数据

-- 插入公司代码
INSERT INTO erp_company_code (code, name) VALUES 
('1000', 'Main Company'),
('2000', 'Branch Company')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入语言
INSERT INTO erp_language (lang_id, name) VALUES 
('EN', 'English'),
('CN', 'Chinese'),
('DE', 'German')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入货币
INSERT INTO erp_currency (currency_code, name) VALUES 
('USD', 'US Dollar'),
('EUR', 'Euro'),
('CNY', 'Chinese Yuan')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入客户标题
INSERT INTO erp_customer_title (title_id, title_name) VALUES 
('MR', 'Mr.'),
('MS', 'Ms.'),
('DR', 'Dr.'),
('PROF', 'Prof.')
ON DUPLICATE KEY UPDATE title_name = VALUES(title_name);

-- 插入标题
INSERT INTO erp_title (title_id, title_name) VALUES 
('MR', 'Mr.'),
('MS', 'Ms.'),
('DR', 'Dr.'),
('PROF', 'Prof.')
ON DUPLICATE KEY UPDATE title_name = VALUES(title_name);

-- 插入排序键
INSERT INTO erp_sort_key (key_id, name) VALUES 
('001', 'Standard Sort'),
('002', 'Priority Sort')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入付款条件
INSERT INTO erp_payment_terms (term_id, name) VALUES 
('0001', 'Net 30 Days'),
('0002', 'Net 60 Days'),
('0003', 'Cash on Delivery')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入销售组织
INSERT INTO erp_sales_org (org_id, name) VALUES 
('1000', 'Main Sales Org'),
('2000', 'Regional Sales Org')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入分销渠道
INSERT INTO erp_distribution_channel (channel_id, name) VALUES 
(10, 'Direct Sales'),
(20, 'Retail'),
(30, 'Online')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入产品线
INSERT INTO erp_division (division_id, name) VALUES 
('01', 'Electronics'),
('02', 'Automotive'),
('03', 'Industrial')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入销售区域
INSERT INTO erp_sales_district (district_id, name) VALUES 
('000001', 'North District'),
('000002', 'South District'),
('000003', 'East District'),
('000004', 'West District')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入价格组
INSERT INTO erp_price_group (group_id, name) VALUES 
('01', 'Standard Price'),
('02', 'Discount Price'),
('03', 'Premium Price')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入客户组
INSERT INTO erp_customer_group (group_id, name) VALUES 
('01', 'Standard Customer'),
('02', 'VIP Customer'),
('03', 'Corporate Customer')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入交货优先级
INSERT INTO erp_deliver_priority (priority_id, name) VALUES 
('01', 'Standard'),
('02', 'High Priority'),
('03', 'Urgent')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入运输条件
INSERT INTO erp_shipping_condition (condition_id, name) VALUES 
('01', 'Standard Shipping'),
('02', 'Express Shipping'),
('03', 'Overnight')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入账户分配
INSERT INTO erp_acct (acct_id, name) VALUES 
('01', 'Standard Account'),
('02', 'Special Account')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入调节账户
INSERT INTO erp_reconciliation_account (account_id, name) VALUES 
('140000', 'Customer Receivables'),
('240000', 'Supplier Payables')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入工厂
INSERT INTO erp_plant_name (plant_id, plant_name, city) VALUES 
(1000, 'Main Plant', 'New York'),
(2000, 'Branch Plant', 'Los Angeles')
ON DUPLICATE KEY UPDATE plant_name = VALUES(plant_name), city = VALUES(city);

-- 现在插入测试客户数据
INSERT INTO erp_customer (
    customer_id, title, name, language, street, city, region, postal_code, country,
    company_code, reconciliation_account, sort_key, sales_org, channel, division,
    currency, sales_district, price_group, customer_group, delivery_priority,
    shipping_condition, delivering_plant, max_part_deliv, incoterms, incoterms_location,
    payment_terms, acct_assignment, output_tax
) VALUES 
(1, 'MR', 'Test Customer 1', 'EN', '123 Main St', 'New York', '01', '10001', 'US',
 '1000', '140000', '001', '1000', 10, '01', 'USD', '000001', '01', '01', '01',
 '01', 1000, 5, 'EXW', 'Factory', '0001', '01', 1),
(2, 'MS', 'Test Customer 2', 'EN', '456 Oak Ave', 'Los Angeles', '02', '90001', 'US',
 '1000', '140000', '001', '1000', 10, '01', 'USD', '000002', '01', '01', '01',
 '01', 1000, 5, 'EXW', 'Factory', '0001', '01', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 插入测试联系人数据
INSERT INTO erp_contact (contact_id, title, first_name, last_name, cor_language, country) VALUES 
(1, 'MR', 'John', 'Doe', 'EN', 'US'),
(2, 'MS', 'Jane', 'Smith', 'EN', 'US')
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name);

-- 验证数据插入
SELECT 'Management levels:' as info;
SELECT * FROM erp_management;

SELECT 'Departments:' as info;
SELECT * FROM erp_department;

SELECT 'Functions:' as info;
SELECT * FROM erp_function;

SELECT 'Customers:' as info;
SELECT customer_id, name FROM erp_customer LIMIT 5;

SELECT 'Contacts:' as info;
SELECT contact_id, first_name, last_name FROM erp_contact LIMIT 5;
