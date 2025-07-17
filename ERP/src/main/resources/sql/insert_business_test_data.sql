-- 业务数据插入脚本
-- 注意：请先执行 insert_complete_test_data.sql

-- 1. 插入测试客户数据
INSERT IGNORE INTO erp_customer (
    title, name, language, street, city, region, postal_code, country,
    company_code, reconciliation_account, sort_key, sales_org, channel,
    division, currency, sales_district, price_group, customer_group,
    delivery_priority, shipping_condition, delivering_plant, max_part_deliv,
    incoterms, incoterms_location, payment_terms, acct_assignment, output_tax
) VALUES 
-- 个人客户
('PI', 'John Smith Individual', 'EN', '123 Main Street', 'New York', 'NY', '10001', 'US',
 '1000', '140000', '001', '1000', 10, '01', 'USD', '000001', '01', '01',
 '02', '01', 1000, 5, 'EXW', 'Factory', '0001', '01', 1),

('PI', 'Sarah Johnson Personal', 'EN', '456 Oak Avenue', 'Los Angeles', 'CA', '90210', 'US',
 '1000', '140000', '001', '1000', 10, '01', 'USD', '000001', '01', '01',
 '02', '02', 1000, 5, 'EXW', 'Factory', '0001', '01', 1),

-- 公司客户
('COMP', 'ABC Corporation', 'EN', '789 Business Blvd', 'Chicago', 'IL', '60601', 'US',
 '1000', '140000', '004', '1000', 20, '01', 'USD', '000001', '05', '03',
 '01', '02', 1000, 10, 'FOB', 'Port', '0002', '04', 1),

('COMP', 'Tech Solutions Ltd', 'EN', '321 Innovation Drive', 'San Francisco', 'CA', '94102', 'US',
 '1000', '140000', '004', '1000', 30, '04', 'USD', '000001', '02', '03',
 '01', '02', 1000, 8, 'CIF', 'Destination', '0001', '01', 1),

-- 组织客户
('GR', 'Global Partners Group', 'EN', '555 Corporate Center', 'Houston', 'TX', '77001', 'US',
 '1000', '140000', '002', '1000', 40, '01', 'USD', '000001', '04', '02',
 '01', '03', 2000, 15, 'DDP', 'Customer Site', '0003', '02', 1),

-- 国际客户
('COMP', 'European Tech GmbH', 'DE', 'Hauptstraße 100', 'Berlin', 'BE', '10115', 'DE',
 '2000', '140000', '004', '2000', 50, '01', 'EUR', '000002', '02', '03',
 '02', '05', 3000, 5, 'EXW', 'Factory', '0002', '01', 1),

('COMP', 'Asia Pacific Solutions', 'ZH', '88 Business Road', 'Shanghai', 'SH', '200000', 'CN',
 '3000', '140000', '004', '3000', 50, '01', 'CNY', '000003', '03', '03',
 '02', '06', 4000, 10, 'FOB', 'Shanghai Port', '0003', '02', 1);

-- 2. 插入联系人数据
INSERT IGNORE INTO erp_contact (
    title, first_name, last_name, cor_language, country
) VALUES 
('MR', 'John', 'Smith', 'EN', 'US'),
('MS', 'Sarah', 'Johnson', 'EN', 'US'),
('DR', 'Michael', 'Brown', 'EN', 'US'),
('MR', 'David', 'Wilson', 'EN', 'US'),
('MS', 'Emily', 'Davis', 'EN', 'US'),
('MR', 'Hans', 'Mueller', 'DE', 'DE'),
('MS', 'Li', 'Wei', 'ZH', 'CN'),
('MR', 'Pierre', 'Dubois', 'FR', 'FR');

-- 3. 插入客户-联系人关系数据
INSERT IGNORE INTO erp_relation (
    rel_category, bp1, bp2, management, department, function, valid_from, valid_to
) VALUES 
('PRIMARY_CONTACT', 1, 1, 3, 'SALES', 'MGR', '2024-01-01', '2025-12-31'),
('PRIMARY_CONTACT', 2, 2, 3, 'SALES', 'SALES', '2024-01-01', '2025-12-31'),
('TECHNICAL_CONTACT', 3, 3, 2, 'TECH', 'TECH', '2024-01-01', '2025-12-31'),
('BILLING_CONTACT', 4, 4, 3, 'FIN', 'ADMIN', '2024-01-01', '2025-12-31'),
('SALES_CONTACT', 5, 5, 4, 'SALES', 'SALES', '2024-01-01', '2025-12-31'),
('PRIMARY_CONTACT', 6, 6, 2, 'SALES', 'MGR', '2024-01-01', '2025-12-31'),
('TECHNICAL_CONTACT', 7, 7, 3, 'TECH', 'TECH', '2024-01-01', '2025-12-31');

-- 4. 插入物料数据
INSERT IGNORE INTO erp_material (
    mat_desc, division, base_uom, srd_price
) VALUES 
('Laptop Computer Model X1', '01', 'EA', 1200.00),
('Desktop Computer Pro', '01', 'EA', 800.00),
('Server Rack Unit', '01', 'EA', 5000.00),
('Network Switch 24-Port', '01', 'EA', 300.00),
('Wireless Router Enterprise', '01', 'EA', 150.00),
('Industrial Machine Type A', '02', 'EA', 25000.00),
('Assembly Line Component', '02', 'EA', 1500.00),
('Quality Control System', '02', 'EA', 8000.00),
('Consulting Service Hour', '03', 'HR', 150.00),
('Technical Support Package', '03', 'EA', 500.00),
('Software License Enterprise', '04', 'EA', 2000.00),
('Database Management System', '04', 'EA', 10000.00);

-- 5. 插入库存数据
INSERT IGNORE INTO erp_stock (
    plant_id, mat_id, bp_id, storage_loc, qty_on_hand, qty_committed
) VALUES 
(1000, 1, 1, '0001', 50.0, 10.0),
(1000, 2, 1, '0001', 30.0, 5.0),
(1000, 3, 2, '0001', 20.0, 2.0),
(1000, 4, 3, '0002', 100.0, 15.0),
(1000, 5, 3, '0002', 75.0, 10.0),
(2000, 6, 4, '0001', 5.0, 1.0),
(2000, 7, 4, '0001', 25.0, 3.0),
(3000, 8, 5, '0003', 10.0, 2.0),
(1000, 9, 1, '0004', 1000.0, 100.0),
(1000, 10, 2, '0004', 500.0, 50.0),
(1000, 11, 3, '0001', 200.0, 25.0),
(1000, 12, 4, '0001', 50.0, 5.0);

-- 6. 插入账号数据（用于登录测试）
INSERT IGNORE INTO erp_account (id, password) VALUES 
(1001, 'TestPass123'),
(1002, 'AdminPass456'),
(1003, 'UserPass789'),
(1004, 'ManagerPass321'),
(1005, 'GuestPass654');

-- 验证数据插入结果
SELECT 'Data insertion completed. Checking counts:' as status;
SELECT 'erp_customer' as table_name, COUNT(*) as record_count FROM erp_customer
UNION ALL
SELECT 'erp_contact', COUNT(*) FROM erp_contact
UNION ALL
SELECT 'erp_relation', COUNT(*) FROM erp_relation
UNION ALL
SELECT 'erp_material', COUNT(*) FROM erp_material
UNION ALL
SELECT 'erp_stock', COUNT(*) FROM erp_stock
UNION ALL
SELECT 'erp_account', COUNT(*) FROM erp_account;
