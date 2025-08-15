-- 开票凭证测试数据插入脚本

-- 插入开票凭证头数据
INSERT IGNORE INTO erp_billing_hdr (bill_id, dlv_id, customer_id, billing_date, net, tax, gross, status) VALUES 
(1, 33, 1, '2025-08-15', 0.00, 0.00, 0.00, 'OPEN'),
(2, 14, 1, '2025-08-14', 1500.00, 195.00, 1695.00, 'OPEN');

-- 插入开票凭证项目数据
INSERT IGNORE INTO erp_billing_item (bill_id, item_no, mat_id, quantity, net_price, tax_rate) VALUES 
-- 开票凭证1的项目（对应交货单33）
(1, 1, 1, 1, 0.00, 13),
(1, 2, 1, 4, 0.00, 13),
(1, 3, 4004, 1, 0.00, 13),
-- 开票凭证2的项目（对应交货单14）
(2, 1, 1, 2, 750.00, 13);

-- 为开票凭证创建对应的erp_item记录（billing_doc类型）
INSERT IGNORE INTO erp_item (
    document_id, document_type, item_no, mat_id, material_code, quantity, net_price, item_value, 
    plant_id, su, order_quantity_str, order_quantity_unit, req_deliv_date, 
    pricing_date, order_probability, pricing_elements_json
) VALUES 
-- 开票凭证1的统一item记录
(1, 'billing_doc', 1, 1, '1', 1, 0.00, 0.00, 1000, 'EA', '1', 'EA', '2025-08-15', '2025-08-15', '100', 
 '[{"cnty":"BASE","name":"基本价格","amount":"1500.0","city":"CNY","per":"1","uom":"EA","conditionValue":"1500.0","curr":"CNY","status":"","numC":"","atoMtsComponent":"","oun":"","cconDe":"","un":"","conditionValue2":"","cdCur":"","stat":true}]'),
(1, 'billing_doc', 2, 1, '1', 4, 0.00, 0.00, 1000, 'EA', '4', 'EA', '2025-08-15', '2025-08-15', '100',
 '[{"cnty":"BASE","name":"基本价格","amount":"1500.0","city":"CNY","per":"1","uom":"EA","conditionValue":"6000.0","curr":"CNY","status":"","numC":"","atoMtsComponent":"","oun":"","cconDe":"","un":"","conditionValue2":"","cdCur":"","stat":true}]'),
(1, 'billing_doc', 3, 4004, '4004', 1, 0.00, 0.00, 1000, 'EA', '1', 'EA', '2025-08-15', '2025-08-15', '100',
 '[{"cnty":"BASE","name":"基本价格","amount":"75.25","city":"CNY","per":"1","uom":"EA","conditionValue":"75.25","curr":"CNY","status":"","numC":"","atoMtsComponent":"","oun":"","cconDe":"","un":"","conditionValue2":"","cdCur":"","stat":true}]'),
-- 开票凭证2的统一item记录
(2, 'billing_doc', 1, 1, '1', 2, 750.00, 1500.00, 1000, 'EA', '2', 'EA', '2025-08-14', '2025-08-14', '100',
 '[{"cnty":"BASE","name":"基本价格","amount":"750.0","city":"CNY","per":"1","uom":"EA","conditionValue":"1500.0","curr":"CNY","status":"","numC":"","atoMtsComponent":"","oun":"","cconDe":"","un":"","conditionValue2":"","cdCur":"","stat":true}]');
