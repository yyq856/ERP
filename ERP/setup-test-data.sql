-- 设置测试数据脚本
-- 为outbound delivery测试准备必要的基础数据

-- 1. 清理可能存在的测试数据
DELETE FROM erp_outbound_item WHERE dlv_id IN (SELECT dlv_id FROM erp_outbound_delivery WHERE so_id IN (6001, 6002));
DELETE FROM erp_outbound_delivery WHERE so_id IN (6001, 6002);
DELETE FROM erp_item WHERE document_id IN (6001, 6002) AND document_type = 'sales';
DELETE FROM erp_sales_order_hdr WHERE so_id IN (6001, 6002);

-- 2. 插入客户数据
INSERT IGNORE INTO erp_customer (customer_id, name, customer_type) VALUES 
(1001, 'Domestic Elec (USNY)', 'CUSTOMER'),
(1002, 'International Corp', 'CUSTOMER');

-- 3. 插入工厂数据
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, plant_desc) VALUES 
(1000, 'Main Plant', 'Main Manufacturing Plant'),
(2000, 'Secondary Plant', 'Secondary Manufacturing Plant');

-- 4. 插入库存地点数据
INSERT IGNORE INTO erp_storage_location (loc_id, name, plant_id) VALUES 
('0001', 'Main Warehouse', 1000),
('0002', 'Secondary Warehouse', 1000),
('1001', 'Raw Materials', 2000);

-- 5. 插入物料数据
INSERT IGNORE INTO erp_material (mat_id, mat_code, mat_desc, mat_type, base_unit) VALUES 
(10001, 'MAT-001', 'Test Material 1', 'FERT', 'EA'),
(10002, 'MAT-002', 'Test Material 2', 'FERT', 'EA'),
(10003, 'MAT-003', 'Test Material 3', 'FERT', 'KG');

-- 6. 更新物料基础重量体积数据
UPDATE erp_material SET 
    base_gross_weight = 0.5,
    base_gross_weight_unit = 'KG',
    base_net_weight = 0.48,
    base_net_weight_unit = 'KG',
    base_volume = 0.012,
    base_volume_unit = 'M3'
WHERE mat_id = 10001;

UPDATE erp_material SET 
    base_gross_weight = 0.8,
    base_gross_weight_unit = 'KG',
    base_net_weight = 0.75,
    base_net_weight_unit = 'KG',
    base_volume = 0.015,
    base_volume_unit = 'M3'
WHERE mat_id = 10002;

UPDATE erp_material SET 
    base_gross_weight = 1.0,
    base_gross_weight_unit = 'KG',
    base_net_weight = 1.0,
    base_net_weight_unit = 'KG',
    base_volume = 0.001,
    base_volume_unit = 'M3'
WHERE mat_id = 10003;

-- 7. 插入销售订单头数据
INSERT INTO erp_sales_order_hdr (so_id, customer_no, req_delivery_date, ship_tp, created_time) VALUES 
(6001, 1001, '2024-01-15', 1001, NOW()),
(6002, 1002, '2024-01-20', 1002, NOW());

-- 8. 插入统一item数据（销售订单明细）
INSERT INTO erp_item (
    document_id, document_type, item_no, mat_id, material_code, quantity, net_price, item_value, 
    plant_id, su, order_quantity_str, order_quantity_unit, req_deliv_date, created_time
) VALUES 
-- 销售订单6001的明细
(6001, 'sales', 10, 10001, 'MAT-001', 100, 50.0, 5000.0, 1000, 'EA', '100', 'EA', '2024-01-15', NOW()),
(6001, 'sales', 20, 10002, 'MAT-002', 50, 80.0, 4000.0, 1000, 'EA', '50', 'EA', '2024-01-15', NOW()),
(6001, 'sales', 30, 10003, 'MAT-003', 25, 120.0, 3000.0, 1000, 'KG', '25', 'KG', '2024-01-15', NOW()),

-- 销售订单6002的明细
(6002, 'sales', 10, 10001, 'MAT-001', 200, 50.0, 10000.0, 2000, 'EA', '200', 'EA', '2024-01-20', NOW()),
(6002, 'sales', 20, 10002, 'MAT-002', 75, 80.0, 6000.0, 2000, 'EA', '75', 'EA', '2024-01-20', NOW());

-- 9. 验证测试数据
SELECT 'Sales Orders' as data_type, COUNT(*) as count FROM erp_sales_order_hdr WHERE so_id IN (6001, 6002)
UNION ALL
SELECT 'Sales Items' as data_type, COUNT(*) as count FROM erp_item WHERE document_id IN (6001, 6002) AND document_type = 'sales'
UNION ALL
SELECT 'Materials' as data_type, COUNT(*) as count FROM erp_material WHERE mat_id IN (10001, 10002, 10003)
UNION ALL
SELECT 'Customers' as data_type, COUNT(*) as count FROM erp_customer WHERE customer_id IN (1001, 1002)
UNION ALL
SELECT 'Plants' as data_type, COUNT(*) as count FROM erp_plant_name WHERE plant_id IN (1000, 2000)
UNION ALL
SELECT 'Storage Locations' as data_type, COUNT(*) as count FROM erp_storage_location WHERE loc_id IN ('0001', '0002', '1001');

-- 10. 显示测试数据详情
SELECT 
    'Sales Order Items' as info,
    ei.document_id as so_id,
    ei.item_no,
    ei.material_code,
    ei.order_quantity_str,
    ei.order_quantity_unit,
    m.base_gross_weight,
    m.base_net_weight,
    m.base_volume
FROM erp_item ei
LEFT JOIN erp_material m ON ei.mat_id = m.mat_id
WHERE ei.document_id IN (6001, 6002) AND ei.document_type = 'sales'
ORDER BY ei.document_id, ei.item_no;

COMMIT;
