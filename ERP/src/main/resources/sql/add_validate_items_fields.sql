-- 物品验证接口所需的数据库字段添加脚本
-- 执行日期: 2025-01-13

-- =============================================
-- 1. 创建定价条件类型映射表
-- =============================================
CREATE TABLE IF NOT EXISTS erp_pricing_condition_type (
    cnty VARCHAR(10) NOT NULL COMMENT '条件类型代码',
    name VARCHAR(100) NOT NULL COMMENT '条件类型名称',
    description VARCHAR(200) COMMENT '条件描述',
    is_percentage BOOLEAN DEFAULT FALSE COMMENT '是否为百分比类型',
    default_currency VARCHAR(5) DEFAULT 'CNY' COMMENT '默认货币',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    PRIMARY KEY (cnty)
) COMMENT='定价条件类型映射表';

-- 插入基础的条件类型数据
INSERT IGNORE INTO erp_pricing_condition_type (cnty, name, description, is_percentage, default_currency) VALUES
('BASE', '基础价格', '物料的基础销售价格', FALSE, 'CNY'),
('DISC', '固定折扣', '固定金额的折扣', FALSE, 'CNY'),
('DISCP', '百分比折扣', '按百分比计算的折扣', TRUE, '%'),
('TAX', '税费', '商品税费', FALSE, 'CNY'),
('FREIGHT', '运费', '运输费用', FALSE, 'CNY'),
('HANDLING', '手续费', '处理手续费', FALSE, 'CNY'),
('MARKUP', '加价', '额外加价', FALSE, 'CNY'),
('REBATE', '回扣', '销售回扣', FALSE, 'CNY');

-- =============================================
-- 2. 修改erp_pricing_element表结构
-- =============================================
-- 备份现有数据
CREATE TABLE IF NOT EXISTS erp_pricing_element_backup AS 
SELECT * FROM erp_pricing_element;

-- 删除现有外键约束
ALTER TABLE erp_pricing_element DROP FOREIGN KEY IF EXISTS erp_pricing_element_ibfk_1;

-- 修改表结构以匹配需求
ALTER TABLE erp_pricing_element 
    MODIFY COLUMN element_id BIGINT AUTO_INCREMENT COMMENT '定价元素ID',
    ADD COLUMN IF NOT EXISTS document_type VARCHAR(20) DEFAULT 'INQUIRY' COMMENT '单据类型（INQUIRY/QUOTATION/SO/BILLING）',
    ADD COLUMN IF NOT EXISTS document_id BIGINT COMMENT '单据ID',
    MODIFY COLUMN cnty VARCHAR(10) COMMENT '条件类型代码',
    MODIFY COLUMN condition_name VARCHAR(100) COMMENT '条件名称',
    MODIFY COLUMN amount VARCHAR(50) COMMENT '金额',
    MODIFY COLUMN city VARCHAR(50) COMMENT '城市/货币标识(应改名为crcy)',
    MODIFY COLUMN per_value VARCHAR(20) DEFAULT '1' COMMENT '每单位数量',
    MODIFY COLUMN uom VARCHAR(20) DEFAULT 'EA' COMMENT '计量单位',
    MODIFY COLUMN condition_value VARCHAR(50) COMMENT '条件值',
    MODIFY COLUMN currency VARCHAR(10) COMMENT '货币代码',
    MODIFY COLUMN status VARCHAR(50) COMMENT '状态',
    MODIFY COLUMN numC VARCHAR(20) COMMENT '数量条件',
    MODIFY COLUMN ato_mts_component VARCHAR(100) COMMENT 'ATO/MTS组件标识',
    MODIFY COLUMN oun VARCHAR(20) COMMENT 'OUn字段',
    MODIFY COLUMN ccon_de VARCHAR(20) COMMENT 'CConDe字段',
    MODIFY COLUMN un VARCHAR(20) COMMENT 'Un字段',
    MODIFY COLUMN condition_value2 VARCHAR(50) COMMENT '条件值2',
    MODIFY COLUMN cd_cur VARCHAR(10) COMMENT 'CdCur字段',
    MODIFY COLUMN stat BOOLEAN DEFAULT TRUE COMMENT '统计标志';

-- 添加外键约束
ALTER TABLE erp_pricing_element 
    ADD CONSTRAINT fk_pricing_condition_type 
    FOREIGN KEY (cnty) REFERENCES erp_pricing_condition_type(cnty);

-- =============================================
-- 3. 创建物品验证配置表
-- =============================================
CREATE TABLE IF NOT EXISTS erp_item_validation_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    application_type VARCHAR(20) NOT NULL COMMENT '应用类型（inquiry/quotation/so/billing）',
    endpoint_path VARCHAR(100) NOT NULL COMMENT '端点路径',
    tax_rate DECIMAL(5,4) DEFAULT 0.13 COMMENT '默认税率',
    default_currency VARCHAR(5) DEFAULT 'CNY' COMMENT '默认货币',
    default_probability VARCHAR(10) DEFAULT '100' COMMENT '默认订单概率',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='物品验证配置表';

-- 插入各应用的验证端点配置
INSERT IGNORE INTO erp_item_validation_config (application_type, endpoint_path, tax_rate) VALUES
('inquiry', '/api/app/inquiry/items-tab-query', 0.13),
('quotation', '/api/app/quotation/items-tab-query', 0.13),
('so', '/api/app/so/items-tab-query', 0.13),
('billing', '/api/app/billing/items-tab-query', 0.13);

-- =============================================
-- 4. 创建物品验证日志表
-- =============================================
CREATE TABLE IF NOT EXISTS erp_item_validation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    session_id VARCHAR(50) COMMENT '会话ID',
    application_type VARCHAR(20) COMMENT '应用类型',
    request_data JSON COMMENT '请求数据',
    response_data JSON COMMENT '响应数据',
    validation_result BOOLEAN COMMENT '验证结果',
    error_message TEXT COMMENT '错误信息',
    processing_time_ms BIGINT COMMENT '处理时间(毫秒)',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT='物品验证日志表';

-- =============================================
-- 5. 添加物料表缺失字段
-- =============================================
-- 检查并添加物料表可能缺失的字段
ALTER TABLE erp_material 
    ADD COLUMN IF NOT EXISTS tax_classification VARCHAR(10) DEFAULT 'TAXABLE' COMMENT '税务分类',
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    ADD COLUMN IF NOT EXISTS price_unit VARCHAR(10) DEFAULT 'CNY' COMMENT '价格单位',
    ADD COLUMN IF NOT EXISTS weight_unit VARCHAR(10) DEFAULT 'KG' COMMENT '重量单位',
    ADD COLUMN IF NOT EXISTS weight DECIMAL(10,3) DEFAULT 0 COMMENT '重量';

-- =============================================
-- 6. 创建条件类型与货币单位映射表
-- =============================================
CREATE TABLE IF NOT EXISTS erp_condition_currency_mapping (
    mapping_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '映射ID',
    cnty VARCHAR(10) NOT NULL COMMENT '条件类型代码',
    default_city_unit VARCHAR(10) NOT NULL COMMENT '默认城市/货币单位',
    description VARCHAR(100) COMMENT '描述',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认值',
    FOREIGN KEY (cnty) REFERENCES erp_pricing_condition_type(cnty)
) COMMENT='条件类型与货币单位映射表';

-- 插入条件类型与货币单位的映射关系
INSERT IGNORE INTO erp_condition_currency_mapping (cnty, default_city_unit, description, is_default) VALUES
('BASE', 'CNY', '基础价格使用人民币', TRUE),
('DISC', 'CNY', '固定折扣使用人民币', TRUE),
('DISCP', '%', '百分比折扣使用百分号', TRUE),
('TAX', 'CNY', '税费使用人民币', TRUE),
('FREIGHT', 'CNY', '运费使用人民币', TRUE),
('HANDLING', 'CNY', '手续费使用人民币', TRUE),
('MARKUP', 'CNY', '加价使用人民币', TRUE),
('REBATE', 'CNY', '回扣使用人民币', TRUE);

-- =============================================
-- 7. 更新现有数据（如果需要）
-- =============================================
-- 确保现有定价元素数据有正确的文档类型
UPDATE erp_pricing_element 
SET document_type = 'SO' 
WHERE document_type IS NULL AND so_id IS NOT NULL;

-- =============================================
-- 8. 创建索引以提高查询性能
-- =============================================
-- 为常用查询字段创建索引
CREATE INDEX IF NOT EXISTS idx_pricing_element_document ON erp_pricing_element(document_type, document_id);
CREATE INDEX IF NOT EXISTS idx_pricing_element_cnty ON erp_pricing_element(cnty);
CREATE INDEX IF NOT EXISTS idx_material_active ON erp_material(is_active);
CREATE INDEX IF NOT EXISTS idx_validation_log_session ON erp_item_validation_log(session_id);
CREATE INDEX IF NOT EXISTS idx_validation_log_date ON erp_item_validation_log(created_date);

-- =============================================
-- 9. 添加数据验证约束
-- =============================================
-- 确保税率在合理范围内
ALTER TABLE erp_item_validation_config
    ADD CONSTRAINT chk_tax_rate CHECK (tax_rate >= 0 AND tax_rate <= 1);

-- 确保物料价格为正数
ALTER TABLE erp_material
    ADD CONSTRAINT chk_std_price_positive CHECK (srd_price >= 0);

-- =============================================
-- 执行完成标记
-- =============================================
-- 记录脚本执行时间
INSERT INTO erp_item_validation_log (session_id, application_type, request_data, validation_result, error_message) 
VALUES ('SYSTEM_INIT', 'SETUP', '{"action": "database_schema_update"}', TRUE, 'Database schema updated for item validation');
