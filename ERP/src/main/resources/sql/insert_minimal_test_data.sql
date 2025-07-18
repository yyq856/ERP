-- 最简化的测试数据插入脚本
-- 只插入业务伙伴关系测试所需的最基本数据

-- 1. 管理级别
INSERT IGNORE INTO erp_management (level_id, description) VALUES (1, 'Standard');

-- 2. 部门
INSERT IGNORE INTO erp_department (dept_id, name) VALUES ('01', 'Default Department');

-- 3. 功能
INSERT IGNORE INTO erp_function (function_id, name) VALUES ('01', 'Default Function');

-- 4. 基础依赖数据（为了能创建客户和联系人）
INSERT IGNORE INTO erp_company_code (code, name) VALUES ('1000', 'Test Company');
INSERT IGNORE INTO erp_language (lang_id, name) VALUES ('EN', 'English');
INSERT IGNORE INTO erp_currency (currency_code, name) VALUES ('USD', 'US Dollar');
INSERT IGNORE INTO erp_customer_title (title_id, title_name) VALUES ('MR', 'Mr.');
INSERT IGNORE INTO erp_title (title_id, title_name) VALUES ('MR', 'Mr.');
INSERT IGNORE INTO erp_sort_key (key_id, name) VALUES ('001', 'Standard');
INSERT IGNORE INTO erp_payment_terms (term_id, name) VALUES ('0001', 'Net 30');
INSERT IGNORE INTO erp_sales_org (org_id, name) VALUES ('1000', 'Main Sales');
INSERT IGNORE INTO erp_distribution_channel (channel_id, name) VALUES (10, 'Direct');
INSERT IGNORE INTO erp_division (division_id, name) VALUES ('01', 'General');
INSERT IGNORE INTO erp_sales_district (district_id, name) VALUES ('000001', 'Main District');
INSERT IGNORE INTO erp_price_group (group_id, name) VALUES ('01', 'Standard');
INSERT IGNORE INTO erp_customer_group (group_id, name) VALUES ('01', 'Standard');
INSERT IGNORE INTO erp_deliver_priority (priority_id, name) VALUES ('01', 'Standard');
INSERT IGNORE INTO erp_shipping_condition (condition_id, name) VALUES ('01', 'Standard');
INSERT IGNORE INTO erp_acct (acct_id, name) VALUES ('01', 'Standard');
INSERT IGNORE INTO erp_reconciliation_account (account_id, name) VALUES ('140000', 'Receivables');
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, city) VALUES (1000, 'Main Plant', 'Test City');

-- 5. 创建测试客户
INSERT IGNORE INTO erp_customer (
    customer_id, title, name, language, street, city, region, postal_code, country,
    company_code, reconciliation_account, sort_key, sales_org, channel, division,
    currency, sales_district, price_group, customer_group, delivery_priority,
    shipping_condition, delivering_plant, max_part_deliv, incoterms, incoterms_location,
    payment_terms, acct_assignment, output_tax
) VALUES 
(1, 'MR', 'Test Customer 1', 'EN', 'Test St', 'Test City', '01', '12345', 'US',
 '1000', '140000', '001', '1000', 10, '01', 'USD', '000001', '01', '01', '01',
 '01', 1000, 5, 'EXW', 'Factory', '0001', '01', 1),
(2, 'MR', 'Test Customer 2', 'EN', 'Test Ave', 'Test City', '01', '12345', 'US',
 '1000', '140000', '001', '1000', 10, '01', 'USD', '000001', '01', '01', '01',
 '01', 1000, 5, 'EXW', 'Factory', '0001', '01', 1);

-- 6. 创建测试联系人
INSERT IGNORE INTO erp_contact (contact_id, title, first_name, last_name, cor_language, country) VALUES 
(1, 'MR', 'John', 'Doe', 'EN', 'US'),
(2, 'MR', 'Jane', 'Smith', 'EN', 'US');

-- 验证插入结果
SELECT 'Test data inserted successfully!' as status;
SELECT customer_id, name FROM erp_customer WHERE customer_id IN (1, 2);
SELECT contact_id, first_name, last_name FROM erp_contact WHERE contact_id IN (1, 2);
