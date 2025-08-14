-- 扩展 erp_inquiry_item 表以支持完整的 ItemValidation 字段
-- 这些字段对应 ItemValidationRequest 中的所有字段

-- 逐个添加字段，避免重复添加已存在的字段
-- 如果字段已存在，会报错但不会影响后续操作

ALTER TABLE erp_inquiry_item ADD COLUMN item_code VARCHAR(50) COMMENT '项目代码';
ALTER TABLE erp_inquiry_item ADD COLUMN material_code VARCHAR(100) COMMENT '物料代码';
ALTER TABLE erp_inquiry_item ADD COLUMN order_quantity_str VARCHAR(20) COMMENT '订单数量字符串';
ALTER TABLE erp_inquiry_item ADD COLUMN order_quantity_unit VARCHAR(10) COMMENT '订单数量单位';
ALTER TABLE erp_inquiry_item ADD COLUMN description VARCHAR(500) COMMENT '物料描述';
ALTER TABLE erp_inquiry_item ADD COLUMN req_deliv_date VARCHAR(20) COMMENT '要求交货日期';
ALTER TABLE erp_inquiry_item ADD COLUMN net_value_str VARCHAR(20) COMMENT '净值字符串';
ALTER TABLE erp_inquiry_item ADD COLUMN net_value_unit VARCHAR(10) COMMENT '净值单位';
ALTER TABLE erp_inquiry_item ADD COLUMN tax_value_str VARCHAR(20) COMMENT '税值字符串';
ALTER TABLE erp_inquiry_item ADD COLUMN tax_value_unit VARCHAR(10) COMMENT '税值单位';
ALTER TABLE erp_inquiry_item ADD COLUMN pricing_date VARCHAR(20) COMMENT '定价日期';
ALTER TABLE erp_inquiry_item ADD COLUMN order_probability VARCHAR(10) COMMENT '订单概率';
ALTER TABLE erp_inquiry_item ADD COLUMN pricing_elements_json TEXT COMMENT '定价元素JSON数据';

-- 为新字段添加索引
CREATE INDEX idx_inquiry_item_material_code ON erp_inquiry_item(material_code);
CREATE INDEX idx_inquiry_item_item_code ON erp_inquiry_item(item_code);

-- 添加注释
ALTER TABLE erp_inquiry_item COMMENT = '询价单项目表 - 扩展支持完整ItemValidation字段';

-- 查看表结构确认
-- SHOW CREATE TABLE erp_inquiry_item;