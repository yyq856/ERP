-- 物品验证接口所需的数据库字段添加脚本（最终版）
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
-- 2. 添加erp_pricing_element表的新字段
-- =============================================
-- 添加document_type字段
SET @sql = CONCAT('SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ''erp_pricing_element'' AND COLUMN_NAME = ''document_type''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果字段不存在则添加
SET @sql = 'SELECT 1';  -- 默认语句
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE erp_pricing_element ADD COLUMN document_type VARCHAR(20) DEFAULT ''INQUIRY'' COMMENT ''单据类型（INQUIRY/QUOTATION/SO/BILLING）''', 
    'SELECT ''document_type column already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加document_id字段
SET @sql = CONCAT('SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ''erp_pricing_element'' AND COLUMN_NAME = ''document_id''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE erp_pricing_element ADD COLUMN document_id BIGINT COMMENT ''单据ID''', 
    'SELECT ''document_id column already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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
-- 添加tax_classification字段
SET @sql = CONCAT('SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ''erp_material'' AND COLUMN_NAME = ''tax_classification''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE erp_material ADD COLUMN tax_classification VARCHAR(10) DEFAULT ''TAXABLE'' COMMENT ''税务分类''', 
    'SELECT ''tax_classification column already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加is_active字段
SET @sql = CONCAT('SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ''erp_material'' AND COLUMN_NAME = ''is_active''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE erp_material ADD COLUMN is_active BOOLEAN DEFAULT TRUE COMMENT ''是否有效''', 
    'SELECT ''is_active column already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加price_unit字段
SET @sql = CONCAT('SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ''erp_material'' AND COLUMN_NAME = ''price_unit''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE erp_material ADD COLUMN price_unit VARCHAR(10) DEFAULT ''CNY'' COMMENT ''价格单位''', 
    'SELECT ''price_unit column already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 6. 创建条件类型与货币单位映射表
-- =============================================
CREATE TABLE IF NOT EXISTS erp_condition_currency_mapping (
    mapping_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '映射ID',
    cnty VARCHAR(10) NOT NULL COMMENT '条件类型代码',
    default_city_unit VARCHAR(10) NOT NULL COMMENT '默认城市/货币单位',
    description VARCHAR(100) COMMENT '描述',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认值'
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
-- 7. 创建索引以提高查询性能
-- =============================================
-- 检查并创建索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'erp_pricing_element' 
  AND INDEX_NAME = 'idx_pricing_element_document';

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_pricing_element_document ON erp_pricing_element(document_type, document_id)', 
    'SELECT ''Index idx_pricing_element_document already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建cnty索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'erp_pricing_element' 
  AND INDEX_NAME = 'idx_pricing_element_cnty';

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_pricing_element_cnty ON erp_pricing_element(cnty)', 
    'SELECT ''Index idx_pricing_element_cnty already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建物料活跃状态索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'erp_material' 
  AND INDEX_NAME = 'idx_material_active';

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_material_active ON erp_material(is_active)', 
    'SELECT ''Index idx_material_active already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 8. 添加约束条件
-- =============================================
-- 检查税率约束
SET @constraint_exists = 0;
SELECT COUNT(*) INTO @constraint_exists 
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'erp_item_validation_config' 
  AND CONSTRAINT_NAME = 'chk_tax_rate';

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE erp_item_validation_config ADD CONSTRAINT chk_tax_rate CHECK (tax_rate >= 0 AND tax_rate <= 1)', 
    'SELECT ''Constraint chk_tax_rate already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查物料价格约束
SET @constraint_exists = 0;
SELECT COUNT(*) INTO @constraint_exists 
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'erp_material' 
  AND CONSTRAINT_NAME = 'chk_std_price_positive';

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE erp_material ADD CONSTRAINT chk_std_price_positive CHECK (srd_price >= 0)', 
    'SELECT ''Constraint chk_std_price_positive already exists'' as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 执行完成标记
-- =============================================
INSERT INTO erp_item_validation_log (session_id, application_type, request_data, validation_result, error_message) 
VALUES ('SYSTEM_INIT', 'SETUP', JSON_OBJECT('action', 'database_schema_update_final'), TRUE, 'Database schema updated successfully for item validation interface');

SELECT 'Database schema update completed successfully!' as result;