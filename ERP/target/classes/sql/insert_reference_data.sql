-- 插入测试所需的完整参考数据
-- 执行前请先运行 check_reference_data.sql 检查当前状态

-- 1. 公司代码
INSERT IGNORE INTO erp_company_code (code, name) VALUES
('1000', 'Main Company'),
('2000', 'Subsidiary Company'),
('3000', 'Test Company');

-- 2. 语言
INSERT IGNORE INTO erp_language (lang_id, name) VALUES
('EN', 'English'),
('ZH', 'Chinese'),
('DE', 'German'),
('FR', 'French'),
('ES', 'Spanish');

-- 3. 货币
INSERT IGNORE INTO erp_currency (currency_code, name) VALUES
('USD', 'US Dollar'),
('EUR', 'Euro'),
('CNY', 'Chinese Yuan'),
('GBP', 'British Pound'),
('JPY', 'Japanese Yen');

-- 4. 客户称谓 (重要：这个对应我们测试用例中的title字段)
INSERT IGNORE INTO erp_customer_title (title_id, title_name) VALUES
('PI', 'Person Individual'),
('GR', 'Group'),
('ORG', 'Organization'),
('COMP', 'Company'),
('MR', 'Mr. Individual'),
('MS', 'Ms. Individual'),
('DR', 'Dr. Individual');

-- 5. 称谓（联系人用）
INSERT IGNORE INTO erp_title (title_id, title_name) VALUES
('MR', 'Mr.'),
('MS', 'Ms.'),
('DR', 'Dr.'),
('PROF', 'Prof.');

-- 6. 排序关键字
INSERT IGNORE INTO erp_sort_key (key_id, name) VALUES 
('001', 'Standard Sort'),
('002', 'Group Sort'),
('003', 'VIP Sort');

-- 7. 付款条件
INSERT IGNORE INTO erp_payment_terms (term_id, name) VALUES 
('0001', 'Net 30 days'),
('0002', 'Net 60 days'),
('0003', 'Cash on Delivery');

-- 8. 销售组织
INSERT IGNORE INTO erp_sales_org (org_id, name) VALUES 
('1000', 'Main Sales Org'),
('2000', 'Regional Sales Org');

-- 9. 分销渠道
INSERT IGNORE INTO erp_distribution_channel (channel_id, name) VALUES 
(10, 'Direct Sales'),
(20, 'Retail'),
(30, 'Online');

-- 10. 产品组
INSERT IGNORE INTO erp_division (division_id, name) VALUES 
('01', 'Electronics'),
('02', 'Machinery'),
('03', 'Services');

-- 11. 销售地区
INSERT IGNORE INTO erp_sales_district (district_id, name) VALUES 
('000001', 'North America'),
('000002', 'Europe'),
('000003', 'Asia Pacific');

-- 12. 价格组
INSERT IGNORE INTO erp_price_group (group_id, name) VALUES 
('01', 'Standard Price'),
('02', 'Premium Price'),
('03', 'Discount Price');

-- 13. 客户组
INSERT IGNORE INTO erp_customer_group (group_id, name) VALUES 
('01', 'Standard Customer'),
('02', 'VIP Customer'),
('03', 'Corporate Customer');

-- 14. 交货优先级
INSERT IGNORE INTO erp_deliver_priority (priority_id, name) VALUES 
('01', 'High Priority'),
('02', 'Normal Priority'),
('03', 'Low Priority');

-- 15. 装运条件
INSERT IGNORE INTO erp_shipping_condition (condition_id, name) VALUES 
('01', 'Standard Shipping'),
('02', 'Express Shipping'),
('03', 'Overnight Shipping');

-- 16. 科目分配
INSERT IGNORE INTO erp_acct (acct_id, name) VALUES 
('01', 'Standard Account'),
('02', 'Special Account');

-- 17. 统驭科目
INSERT IGNORE INTO erp_reconciliation_account (account_id, name) VALUES 
('140000', 'Customer Receivables'),
('150000', 'Other Receivables');

-- 18. 工厂
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, city) VALUES 
(1000, 'Main Plant', 'New York'),
(2000, 'Secondary Plant', 'Los Angeles');

-- 19. 库存地点
INSERT IGNORE INTO erp_storage_location (loc_id, name) VALUES 
('0001', 'Main Warehouse'),
('0002', 'Secondary Warehouse');

-- 20. 订单状态
INSERT IGNORE INTO erp_order_status (status_code, description) VALUES 
('OPEN', 'Open'),
('PROC', 'Processing'),
('COMP', 'Completed'),
('CANC', 'Cancelled');

-- 21. 管理级别
INSERT IGNORE INTO erp_management (level_id, description) VALUES 
(1, 'Executive'),
(2, 'Manager'),
(3, 'Supervisor'),
(4, 'Staff');

-- 22. 部门
INSERT IGNORE INTO erp_department (dept_id, name) VALUES 
('SALES', 'Sales Department'),
('TECH', 'Technical Department'),
('FIN', 'Finance Department');

-- 23. 职能
INSERT IGNORE INTO erp_function (function_id, name) VALUES 
('MGR', 'Manager'),
('TECH', 'Technical Specialist'),
('SALES', 'Sales Representative');
