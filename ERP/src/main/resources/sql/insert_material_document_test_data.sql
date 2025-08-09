-- 物料凭证测试数据插入脚本
-- 用于测试物料凭证搜索和详情查询接口

-- 注意：执行此脚本前请确保已执行以下脚本：
-- 1. create_material_document_tables.sql (创建物料凭证相关表)
-- 2. insert_reference_data.sql 或相关基础数据脚本 (工厂、物料、库存地点等基础数据)

-- 1. 插入测试用物料凭证头数据
INSERT IGNORE INTO erp_material_document (
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

-- 2. 插入额外的工厂数据（如果不存在）
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, city) VALUES
(1001, 'Secondary Plant', 'Los Angeles'),
(1002, 'Distribution Center', 'Chicago');

-- 3. 插入额外的物料数据（如果不存在）
INSERT IGNORE INTO erp_material (mat_desc, division, base_uom, srd_price) VALUES
('Test Material A', '01', 'PC', 100.00),
('Test Material B', '01', 'KG', 25.50),
('Test Material C', '02', 'EA', 75.25),
('Test Material D', '01', 'L', 15.00),
('Test Material E', '03', 'PC', 200.00);

-- 4. 插入额外的库存地点数据（如果不存在）
INSERT IGNORE INTO erp_storage_location (loc_id, name) VALUES
('0002', 'Secondary Warehouse'),
('0003', 'Quality Control Area'),
('0004', 'Shipping Area'),
('0005', 'Returns Area');

-- 5. 插入物料凭证项目数据
-- 获取物料凭证ID和物料ID用于插入项目数据
INSERT INTO erp_material_document_item (
    material_document_id, item_no, mat_id, quantity, unit, movement_type, storage_loc
)
SELECT md.material_document_id, 1, m.mat_id, 100.000, 'PC', '101', '0001'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD001' AND m.mat_desc = 'Test Material A'
UNION ALL
SELECT md.material_document_id, 2, m.mat_id, 50.000, 'KG', '101', '0001'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD001' AND m.mat_desc = 'Test Material B'
UNION ALL
-- MD002项目
SELECT md.material_document_id, 1, m.mat_id, 25.000, 'EA', '261', '0001'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD002' AND m.mat_desc = 'Test Material C'
UNION ALL
-- MD003项目
SELECT md.material_document_id, 1, m.mat_id, 200.000, 'L', '311', '0002'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD003' AND m.mat_desc = 'Test Material D'
UNION ALL
SELECT md.material_document_id, 2, m.mat_id, 10.000, 'PC', '311', '0002'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD003' AND m.mat_desc = 'Test Material E'
UNION ALL
-- MD004项目
SELECT md.material_document_id, 1, m.mat_id, 75.000, 'PC', '501', '0003'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD004' AND m.mat_desc = 'Test Material A'
UNION ALL
-- MD005项目
SELECT md.material_document_id, 1, m.mat_id, 30.000, 'KG', '601', '0005'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD005' AND m.mat_desc = 'Test Material B'
UNION ALL
-- MD006项目
SELECT md.material_document_id, 1, m.mat_id, 150.000, 'PC', '261', '0001'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD006' AND m.mat_desc = 'Test Material A'
UNION ALL
-- MD007项目
SELECT md.material_document_id, 1, m.mat_id, 80.000, 'EA', '101', '0004'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD007' AND m.mat_desc = 'Test Material C'
UNION ALL
SELECT md.material_document_id, 2, m.mat_id, 120.000, 'L', '101', '0004'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD007' AND m.mat_desc = 'Test Material D'
UNION ALL
-- MD008项目
SELECT md.material_document_id, 1, m.mat_id, 5.000, 'PC', '311', '0002'
FROM erp_material_document md, erp_material m
WHERE md.material_document = 'MD008' AND m.mat_desc = 'Test Material E';

-- 6. 插入业务流程关联数据示例
-- 注意：这些关联数据是模拟的，实际环境中需要确保相关的交货单、会计凭证、销售订单存在
INSERT IGNORE INTO erp_material_document_process (
    material_document_id, dlv_id, bill_id, so_id
)
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD001'
UNION ALL
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD002'
UNION ALL
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD003'
UNION ALL
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD004'
UNION ALL
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD005'
UNION ALL
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD006'
UNION ALL
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD007'
UNION ALL
SELECT md.material_document_id, NULL, NULL, NULL
FROM erp_material_document md 
WHERE md.material_document = 'MD008';

-- 7. 验证插入的数据
SELECT 'Material Documents Inserted' as info, COUNT(*) as count FROM erp_material_document
UNION ALL
SELECT 'Material Document Items Inserted', COUNT(*) FROM erp_material_document_item
UNION ALL
SELECT 'Material Document Process Records Inserted', COUNT(*) FROM erp_material_document_process;

-- 8. 显示插入的测试数据概览
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

-- 测试提示：
-- 1. 搜索接口测试：
--    POST /api/material/search
--    请求体示例：
--    {
--      "materialDocument": "MD001",
--      "plant": "Main",
--      "materialDocumentYear": "2024",
--      "postingDate": "2024-01-15"
--    }
--
-- 2. 详情接口测试：
--    GET /api/material/get/1
--    （使用上面查询结果中的 material_document_id）