-- 简化版物料凭证测试数据插入脚本
-- 去掉外键约束，确保数据能够成功插入

-- 1. 清理现有测试数据（可选）
DELETE FROM erp_material_document_process WHERE material_document_id > 0;
DELETE FROM erp_material_document_item WHERE material_document_id > 0;
DELETE FROM erp_material_document WHERE material_document_id > 0;

-- 2. 插入基础工厂数据
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, city) VALUES
(1000, 'Main Plant', 'New York'),
(1001, 'Secondary Plant', 'Los Angeles'),
(1002, 'Distribution Center', 'Chicago');

-- 3. 插入基础物料数据
INSERT IGNORE INTO erp_material (mat_desc, division, base_uom, srd_price) VALUES
('Test Material A', '01', 'PC', 100.00),
('Test Material B', '01', 'KG', 25.50),
('Test Material C', '02', 'EA', 75.25),
('Test Material D', '01', 'L', 15.00),
('Test Material E', '03', 'PC', 200.00);

-- 4. 插入库存地点数据
INSERT IGNORE INTO erp_storage_location (loc_id, name) VALUES
('0001', 'Main Warehouse'),
('0002', 'Secondary Warehouse'),
('0003', 'Quality Control Area'),
('0004', 'Shipping Area'),
('0005', 'Returns Area');

-- 5. 插入物料凭证头数据
INSERT INTO erp_material_document (
    material_document, material_document_year, plant_id, 
    posting_date, document_date, created_by
) VALUES 
('MD001', '2024', 1000, '2024-01-15', '2024-01-15', 'testuser'),
('MD002', '2024', 1000, '2024-02-20', '2024-02-20', 'testuser'),
('MD003', '2024', 1001, '2024-03-10', '2024-03-10', 'testuser'),
('MD004', '2024', 1000, '2024-04-05', '2024-04-05', 'testuser'),
('MD005', '2024', 1001, '2024-05-12', '2024-05-12', 'testuser'),
('MD006', '2023', 1000, '2023-12-25', '2023-12-25', 'testuser'),
('MD007', '2024', 1000, '2024-06-30', '2024-06-30', 'testuser'),
('MD008', '2024', 1001, '2024-07-18', '2024-07-18', 'testuser');

-- 6. 获取物料ID用于插入项目数据
SET @mat1 = (SELECT mat_id FROM erp_material WHERE mat_desc = 'Test Material A' LIMIT 1);
SET @mat2 = (SELECT mat_id FROM erp_material WHERE mat_desc = 'Test Material B' LIMIT 1);
SET @mat3 = (SELECT mat_id FROM erp_material WHERE mat_desc = 'Test Material C' LIMIT 1);
SET @mat4 = (SELECT mat_id FROM erp_material WHERE mat_desc = 'Test Material D' LIMIT 1);
SET @mat5 = (SELECT mat_id FROM erp_material WHERE mat_desc = 'Test Material E' LIMIT 1);

-- 7. 插入物料凭证项目数据（使用简化的方式）
INSERT INTO erp_material_document_item (
    material_document_id, item_no, mat_id, quantity, unit, movement_type, storage_loc
) VALUES 
-- MD001 项目
(1, 1, @mat1, 100.000, 'PC', '101', '0001'),
(1, 2, @mat2, 50.000, 'KG', '101', '0001'),
-- MD002 项目
(2, 1, @mat3, 25.000, 'EA', '261', '0001'),
-- MD003 项目
(3, 1, @mat4, 200.000, 'L', '311', '0002'),
(3, 2, @mat5, 10.000, 'PC', '311', '0002'),
-- MD004 项目
(4, 1, @mat1, 75.000, 'PC', '501', '0003'),
-- MD005 项目
(5, 1, @mat2, 30.000, 'KG', '601', '0005'),
-- MD006 项目
(6, 1, @mat1, 150.000, 'PC', '261', '0001'),
-- MD007 项目
(7, 1, @mat3, 80.000, 'EA', '101', '0004'),
(7, 2, @mat4, 120.000, 'L', '101', '0004'),
-- MD008 项目
(8, 1, @mat5, 5.000, 'PC', '311', '0002');

-- 8. 插入业务流程关联数据（简化版本，都为NULL）
INSERT INTO erp_material_document_process (material_document_id, dlv_id, bill_id, so_id) VALUES 
(1, NULL, NULL, NULL),
(2, NULL, NULL, NULL),
(3, NULL, NULL, NULL),
(4, NULL, NULL, NULL),
(5, NULL, NULL, NULL),
(6, NULL, NULL, NULL),
(7, NULL, NULL, NULL),
(8, NULL, NULL, NULL);

-- 9. 验证插入的数据
SELECT '=== 数据验证 ===' as info;

SELECT 'Material Documents' as table_name, COUNT(*) as count FROM erp_material_document
UNION ALL
SELECT 'Material Document Items', COUNT(*) FROM erp_material_document_item
UNION ALL
SELECT 'Material Document Process', COUNT(*) FROM erp_material_document_process
UNION ALL
SELECT 'Plant Names', COUNT(*) FROM erp_plant_name
UNION ALL
SELECT 'Materials', COUNT(*) FROM erp_material
UNION ALL
SELECT 'Storage Locations', COUNT(*) FROM erp_storage_location;

-- 10. 显示插入的物料凭证数据
SELECT '=== 物料凭证概览 ===' as info;

SELECT 
    md.material_document_id,
    md.material_document,
    md.material_document_year,
    pn.plant_name,
    md.posting_date,
    md.document_date,
    COUNT(mdi.item_no) as item_count
FROM erp_material_document md
LEFT JOIN erp_plant_name pn ON md.plant_id = pn.plant_id
LEFT JOIN erp_material_document_item mdi ON md.material_document_id = mdi.material_document_id
GROUP BY md.material_document_id, md.material_document, md.material_document_year, 
         pn.plant_name, md.posting_date, md.document_date
ORDER BY md.posting_date DESC;

-- 11. 显示一些样本项目数据
SELECT '=== 样本项目数据 ===' as info;

SELECT 
    md.material_document,
    mdi.item_no,
    m.mat_desc,
    mdi.quantity,
    mdi.unit,
    mdi.movement_type
FROM erp_material_document md
JOIN erp_material_document_item mdi ON md.material_document_id = mdi.material_document_id
LEFT JOIN erp_material m ON mdi.mat_id = m.mat_id
WHERE md.material_document IN ('MD001', 'MD002', 'MD003')
ORDER BY md.material_document, mdi.item_no;

-- 测试提示
SELECT '=== 测试提示 ===' as info;
SELECT 'Now you can test the search API with:' as step1;
SELECT 'POST /api/material/search with body: {}' as step2;
SELECT 'Expected: 8 material documents should be returned' as step3;