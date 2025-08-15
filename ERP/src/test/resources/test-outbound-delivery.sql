-- 测试出库交货单功能的SQL脚本

-- 1. 测试表结构是否正确创建
DESCRIBE erp_outbound_delivery;
DESCRIBE erp_outbound_item;

-- 2. 检查material表是否添加了新字段
SHOW COLUMNS FROM erp_material LIKE 'base_%';

-- 3. 插入测试数据
-- 首先确保有销售订单数据
INSERT IGNORE INTO erp_sales_order_hdr (so_id, customer_no, req_delivery_date, ship_tp) 
VALUES (6001, 1001, '2024-01-15', 1001);

-- 确保有统一item数据（销售订单）
INSERT IGNORE INTO erp_item (
    document_id, document_type, item_no, mat_id, quantity, net_price, item_value, 
    plant_id, su, order_quantity_str, order_quantity_unit, req_deliv_date
) VALUES 
(6001, 'sales', 10, 10001, 100, 50.0, 5000.0, 1000, 'EA', '100', 'EA', '2024-01-15'),
(6001, 'sales', 20, 10002, 50, 80.0, 4000.0, 1000, 'EA', '50', 'EA', '2024-01-15');

-- 更新material表的基础重量体积数据
UPDATE erp_material SET 
    base_gross_weight = 0.5,
    base_gross_weight_unit = 'KG',
    base_net_weight = 0.48,
    base_net_weight_unit = 'KG',
    base_volume = 0.012,
    base_volume_unit = 'M3'
WHERE mat_id IN (10001, 10002);

-- 4. 测试创建出库交货单
INSERT INTO erp_outbound_delivery (
    so_id, ship_tp, posted, ready_to_post,
    planned_gi_date, loading_date, delivery_date,
    picking_status, overall_status, gi_status, priority
) VALUES (
    6001, 1001, 0, 0,
    '2024-01-15', '2024-01-15', '2024-01-15',
    'IN_PROGRESS', 'IN_PROGRESS', 'IN_PROGRESS', 'Normal Items'
);

-- 获取刚插入的交货单ID
SET @dlv_id = LAST_INSERT_ID();

-- 5. 测试插入出库物品
INSERT INTO erp_outbound_item (
    dlv_id, item_no, ref_document_id, ref_document_type, ref_item_no,
    picking_quantity, picking_status, confirmation_status, item_type, conversion_rate,
    gross_weight, gross_weight_unit, net_weight, net_weight_unit, volume, volume_unit
)
SELECT
    @dlv_id,
    ei.item_no,
    ei.document_id,
    ei.document_type,
    ei.item_no,
    CAST(ei.order_quantity_str AS DECIMAL(13,3)),
    'Completed',
    'Not Confirmed',
    'Standard',
    1.000,
    COALESCE(m.base_gross_weight * CAST(ei.order_quantity_str AS DECIMAL(13,3)), 0),
    m.base_gross_weight_unit,
    COALESCE(m.base_net_weight * CAST(ei.order_quantity_str AS DECIMAL(13,3)), 0),
    m.base_net_weight_unit,
    COALESCE(m.base_volume * CAST(ei.order_quantity_str AS DECIMAL(13,3)), 0),
    m.base_volume_unit
FROM erp_item ei
LEFT JOIN erp_material m ON ei.mat_id = m.mat_id
WHERE ei.document_id = 6001 AND ei.document_type = 'sales'
ORDER BY ei.item_no;

-- 6. 测试查询出库交货单详情（模拟前端格式）
SELECT
    CAST(oi.item_no AS CHAR) AS item,
    ei.material_code AS material,
    ei.order_quantity_str AS deliveryQuantity,
    ei.order_quantity_unit AS deliveryQuantityUnit,
    oi.picking_quantity AS pickingQuantity,
    ei.order_quantity_unit AS pickingQuantityUnit,
    oi.picking_status AS pickingStatus,
    oi.confirmation_status AS confirmationStatus,
    CONCAT('SO-', ei.document_id) AS salesOrder,
    oi.item_type AS itemType,
    CONCAT(ei.order_quantity_str, ' ', ei.order_quantity_unit) AS originalDelivertyQuantity,
    oi.conversion_rate AS conversionRate,
    CONCAT('1 ', ei.order_quantity_unit) AS baseUnitDeliveryQuantity,
    CONCAT(oi.gross_weight, ' ', oi.gross_weight_unit) AS grossWeight,
    CONCAT(oi.net_weight, ' ', oi.net_weight_unit) AS netWeight,
    CONCAT(oi.volume, ' ', oi.volume_unit) AS volume,
    ei.plant_id AS plant,
    oi.storage_loc AS storageLocation,
    sl.name AS storageLocationDescription,
    oi.storage_bin AS storageBin,
    ei.req_deliv_date AS materialAvailability
FROM erp_outbound_item oi
LEFT JOIN erp_item ei ON oi.ref_document_id = ei.document_id 
                       AND oi.ref_document_type = ei.document_type 
                       AND oi.ref_item_no = ei.item_no
LEFT JOIN erp_storage_location sl ON oi.storage_loc = sl.loc_id
WHERE oi.dlv_id = @dlv_id
ORDER BY oi.item_no;

-- 7. 测试更新交货单重量体积合计
UPDATE erp_outbound_delivery od
SET 
    gross_weight = (
        SELECT COALESCE(SUM(oi.gross_weight), 0)
        FROM erp_outbound_item oi WHERE oi.dlv_id = od.dlv_id
    ),
    gross_weight_unit = (
        SELECT oi.gross_weight_unit
        FROM erp_outbound_item oi WHERE oi.dlv_id = od.dlv_id LIMIT 1
    ),
    net_weight = (
        SELECT COALESCE(SUM(oi.net_weight), 0)
        FROM erp_outbound_item oi WHERE oi.dlv_id = od.dlv_id
    ),
    net_weight_unit = (
        SELECT oi.net_weight_unit
        FROM erp_outbound_item oi WHERE oi.dlv_id = od.dlv_id LIMIT 1
    ),
    volume = (
        SELECT COALESCE(SUM(oi.volume), 0)
        FROM erp_outbound_item oi WHERE oi.dlv_id = od.dlv_id
    ),
    volume_unit = (
        SELECT oi.volume_unit
        FROM erp_outbound_item oi WHERE oi.dlv_id = od.dlv_id LIMIT 1
    )
WHERE od.dlv_id = @dlv_id;

-- 8. 验证结果
SELECT 
    dlv_id,
    so_id,
    posted,
    ready_to_post,
    picking_status,
    overall_status,
    gi_status,
    gross_weight,
    gross_weight_unit,
    net_weight,
    net_weight_unit,
    volume,
    volume_unit,
    priority
FROM erp_outbound_delivery 
WHERE dlv_id = @dlv_id;

-- 9. 清理测试数据（可选）
-- DELETE FROM erp_outbound_item WHERE dlv_id = @dlv_id;
-- DELETE FROM erp_outbound_delivery WHERE dlv_id = @dlv_id;
