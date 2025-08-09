-- 物料凭证测试数据完整插入脚本
-- 包含所有必要的基础数据和外键依赖

-- 首先插入基础参考数据
INSERT IGNORE INTO erp_plant_name (plant_id, plant_name, city) VALUES
(1001, '北京工厂', '北京'),
(1002, '上海工厂', '上海'),
(1003, '广州工厂', '广州');

INSERT IGNORE INTO erp_storage_location (loc_id, name) VALUES
('WH01', '主仓库'),
('WH02', '备用仓库'),
('WH03', '原料仓库');

INSERT IGNORE INTO erp_material (mat_id, mat_desc, division, base_uom, srd_price) VALUES
(10001, '笔记本电脑-联想ThinkPad', '01', 'EA', 5999.00),
(10002, '台式机-戴尔OptiPlex', '01', 'EA', 3999.00),
(10003, '显示器-三星24寸', '01', 'EA', 1299.00),
(10004, '键盘-罗技机械', '01', 'EA', 299.00),
(10005, '鼠标-雷蛇游戏', '01', 'EA', 199.00);

-- 确保移动类型数据存在
INSERT IGNORE INTO erp_movement_type (movement_type, description, movement_indicator) VALUES
('101', '收货到库存', 'RECEIPT'),
('261', '发货到客户', 'ISSUE'),
('311', '库存地点间转储', 'TRANSFER'),
('501', '库存盘点差异', 'ADJUSTMENT'),
('601', '货物退回', 'RETURN');

-- 插入物料凭证头数据
INSERT INTO erp_material_document (
    material_document_id, material_document, material_document_year, 
    plant_id, posting_date, document_date, created_by, created_at
) VALUES
(1, 'MD001', '2024', 1001, '2024-01-15', '2024-01-15', 'admin', '2024-01-15 10:00:00'),
(2, 'MD002', '2024', 1001, '2024-01-16', '2024-01-16', 'admin', '2024-01-16 11:00:00'),
(3, 'MD003', '2024', 1002, '2024-01-17', '2024-01-17', 'user01', '2024-01-17 09:30:00'),
(4, 'MD004', '2024', 1002, '2024-01-18', '2024-01-18', 'user02', '2024-01-18 14:00:00'),
(5, 'MD005', '2024', 1003, '2024-01-19', '2024-01-19', 'admin', '2024-01-19 16:20:00');

-- 插入物料凭证项目数据
INSERT INTO erp_material_document_item (
    material_document_id, item_no, mat_id, quantity, unit, movement_type, storage_loc
) VALUES
-- 第一个凭证的项目
(1, 1, 10001, 10.000, 'EA', '101', 'WH01'),
(1, 2, 10002, 5.000, 'EA', '101', 'WH01'),
-- 第二个凭证的项目
(2, 1, 10003, 20.000, 'EA', '261', 'WH01'),
(2, 2, 10004, 15.000, 'EA', '261', 'WH02'),
-- 第三个凭证的项目
(3, 1, 10005, 8.000, 'EA', '311', 'WH02'),
(3, 2, 10001, 3.000, 'EA', '311', 'WH03'),
-- 第四个凭证的项目
(4, 1, 10002, 12.000, 'EA', '501', 'WH01'),
-- 第五个凭证的项目
(5, 1, 10003, 6.000, 'EA', '601', 'WH03'),
(5, 2, 10004, 4.000, 'EA', '601', 'WH03');

-- 插入业务流程关联数据（可选，不影响基本查询）
INSERT INTO erp_material_document_process (material_document_id, dlv_id, bill_id, so_id) VALUES
(1, NULL, NULL, NULL),
(2, NULL, NULL, NULL),
(3, NULL, NULL, NULL),
(4, NULL, NULL, NULL),
(5, NULL, NULL, NULL);

-- 验证数据插入结果的查询语句
-- 请在数据插入后执行以下查询来验证：

-- 1. 检查物料凭证头数据
-- SELECT * FROM erp_material_document;

-- 2. 检查物料凭证项目数据
-- SELECT * FROM erp_material_document_item;

-- 3. 检查完整的连接查询（应该返回数据）
-- SELECT 
--     md.material_document_id,
--     md.material_document,
--     md.material_document_year,
--     md.posting_date,
--     md.document_date,
--     pn.plant_name,
--     mdi.item_no,
--     m.mat_desc,
--     mdi.quantity,
--     mdi.unit,
--     mt.description as movement_desc,
--     sl.name as storage_location_name
-- FROM erp_material_document md
-- LEFT JOIN erp_plant_name pn ON md.plant_id = pn.plant_id
-- LEFT JOIN erp_material_document_item mdi ON md.material_document_id = mdi.material_document_id
-- LEFT JOIN erp_material m ON mdi.mat_id = m.mat_id
-- LEFT JOIN erp_movement_type mt ON mdi.movement_type = mt.movement_type
-- LEFT JOIN erp_storage_location sl ON mdi.storage_loc = sl.loc_id
-- ORDER BY md.material_document_id, mdi.item_no;

-- 4. 检查数据总数
-- SELECT COUNT(*) as total_documents FROM erp_material_document;
-- SELECT COUNT(*) as total_items FROM erp_material_document_item;

COMMIT;