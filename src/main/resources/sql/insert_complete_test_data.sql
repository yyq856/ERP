-- 完整的测试数据插入脚本
-- 按照建表顺序插入所有参考表数据，确保外键约束满足

-- 1. 公司代码表
INSERT IGNORE INTO erp_company_code (code, name) VALUES 
('1000', 'Main Company Ltd'),
('2000', 'Subsidiary Company Inc'),
('3000', 'Branch Office Corp'),
('4000', 'Regional Office LLC');

-- 2. 语言表
INSERT IGNORE INTO erp_language (lang_id, name) VALUES 
('EN', 'English'),
('ZH', 'Chinese'),
('DE', 'German'),
('FR', 'French'),
('ES', 'Spanish'),
('JA', 'Japanese'),
('KO', 'Korean');

-- 3. 货币表
INSERT IGNORE INTO erp_currency (currency_code, name) VALUES 
('USD', 'US Dollar'),
('EUR', 'Euro'),
('CNY', 'Chinese Yuan'),
('GBP', 'British Pound'),
('JPY', 'Japanese Yen'),
('KRW', 'Korean Won'),
('CAD', 'Canadian Dollar');

-- 4. 客户称谓表
INSERT IGNORE INTO erp_customer_title (title_id, title_name) VALUES 
('PI', 'Person Individual'),
('GR', 'Group'),
('ORG', 'Organization'),
('COMP', 'Company'),
('MR', 'Mr. Individual'),
('MS', 'Ms. Individual'),
('DR', 'Dr. Individual');

-- 5. 联系人称谓表
INSERT IGNORE INTO erp_title (title_id, title_name) VALUES 
('MR', 'Mr.'),
('MS', 'Ms.'),
('MRS', 'Mrs.'),
('DR', 'Dr.'),
('PROF', 'Prof.'),
('SIR', 'Sir'),
('MADAM', 'Madam');

-- 6. 排序关键字表
INSERT IGNORE INTO erp_sort_key (key_id, name) VALUES 
('001', 'Standard Sort'),
('002', 'Group Sort'),
('003', 'VIP Sort'),
('004', 'Corporate Sort'),
('005', 'Government Sort');

-- 7. 付款条件表
INSERT IGNORE INTO erp_payment_terms (term_id, name) VALUES 
('0001', 'Net 30 days'),
('0002', 'Net 60 days'),
('0003', 'Net 90 days'),
('0004', 'Cash on Delivery'),
('0005', 'Prepayment'),
('0006', 'Net 15 days'),
('0007', 'Net 45 days');

-- 8. 销售组织表
INSERT IGNORE INTO erp_sales_org (org_id, name) VALUES 
('1000', 'Main Sales Organization'),
('2000', 'Regional Sales Org'),
('3000', 'Online Sales Org'),
('4000', 'Export Sales Org'),
('5000', 'Retail Sales Org');

-- 9. 分销渠道表
INSERT IGNORE INTO erp_distribution_channel (channel_id, name) VALUES 
(10, 'Direct Sales'),
(20, 'Retail'),
(30, 'Online'),
(40, 'Wholesale'),
(50, 'Export'),
(60, 'Partner Channel');

-- 10. 产品组表
INSERT IGNORE INTO erp_division (division_id, name) VALUES 
('01', 'Electronics'),
('02', 'Machinery'),
('03', 'Services'),
('04', 'Software'),
('05', 'Hardware'),
('06', 'Consulting');

-- 11. 销售地区表
INSERT IGNORE INTO erp_sales_district (district_id, name) VALUES 
('000001', 'North America'),
('000002', 'Europe'),
('000003', 'Asia Pacific'),
('000004', 'Latin America'),
('000005', 'Middle East'),
('000006', 'Africa');

-- 12. 价格组表
INSERT IGNORE INTO erp_price_group (group_id, name) VALUES 
('01', 'Standard Price'),
('02', 'Premium Price'),
('03', 'Discount Price'),
('04', 'VIP Price'),
('05', 'Corporate Price'),
('06', 'Government Price');

-- 13. 客户组表
INSERT IGNORE INTO erp_customer_group (group_id, name) VALUES 
('01', 'Standard Customer'),
('02', 'VIP Customer'),
('03', 'Corporate Customer'),
('04', 'Government Customer'),
('05', 'Partner Customer'),
('06', 'Retail Customer');

-- 14. 交货优先级表
INSERT IGNORE INTO erp_deliver_priority (priority_id, name) VALUES 
('01', 'High Priority'),
('02', 'Normal Priority'),
('03', 'Low Priority'),
('04', 'Urgent Priority'),
('05', 'Standard Priority');

-- 15. 装运条件表
INSERT IGNORE INTO erp_shipping_condition (condition_id, name) VALUES 
('01', 'Standard Shipping'),
('02', 'Express Shipping'),
('03', 'Overnight Shipping'),
('04', 'Ground Shipping'),
('05', 'Air Shipping'),
('06', 'Sea Shipping');

-- 16. 科目分配表
INSERT IGNORE INTO erp_acct (acct_id, name) VALUES 
('01', 'Standard Account'),
('02', 'Special Account'),
('03', 'VIP Account'),
('04', 'Corporate Account'),
('05', 'Government Account');

-- 17. 统驭科目表
INSERT IGNORE INTO erp_reconciliation_account (account_id, name) VALUES 
('140000', 'Customer Receivables'),
('150000', 'Other Receivables'),
('160000', 'Trade Receivables'),
('170000', 'Long-term Receivables'),
('180000', 'Doubtful Receivables');

-- 18. 工厂表
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, city) VALUES 
(1000, 'Main Manufacturing Plant', 'New York'),
(2000, 'Secondary Plant', 'Los Angeles'),
(3000, 'Assembly Plant', 'Chicago'),
(4000, 'Distribution Center', 'Houston'),
(5000, 'Research Facility', 'San Francisco');

-- 19. 库存地点表
INSERT IGNORE INTO erp_storage_location (loc_id, name) VALUES 
('0001', 'Main Warehouse'),
('0002', 'Secondary Warehouse'),
('0003', 'Raw Materials Storage'),
('0004', 'Finished Goods Storage'),
('0005', 'Quality Control Storage'),
('0006', 'Shipping Dock');

-- 20. 订单状态表
INSERT IGNORE INTO erp_order_status (status_code, description) VALUES 
('OPEN', 'Open'),
('PROC', 'Processing'),
('COMP', 'Completed'),
('CANC', 'Cancelled'),
('HOLD', 'On Hold'),
('SHIP', 'Shipped'),
('DELV', 'Delivered');

-- 21. 管理级别表
INSERT IGNORE INTO erp_management (level_id, description) VALUES 
(1, 'Executive Level'),
(2, 'Senior Manager'),
(3, 'Manager'),
(4, 'Supervisor'),
(5, 'Team Lead'),
(6, 'Staff');

-- 22. 部门表
INSERT IGNORE INTO erp_department (dept_id, name) VALUES 
('SALES', 'Sales Department'),
('TECH', 'Technical Department'),
('FIN', 'Finance Department'),
('HR', 'Human Resources'),
('IT', 'Information Technology'),
('OPS', 'Operations'),
('MKT', 'Marketing');

-- 23. 职能表
INSERT IGNORE INTO erp_function (function_id, name) VALUES 
('MGR', 'Manager'),
('TECH', 'Technical Specialist'),
('SALES', 'Sales Representative'),
('ADMIN', 'Administrator'),
('ANALYST', 'Business Analyst'),
('COORD', 'Coordinator'),
('EXEC', 'Executive');
