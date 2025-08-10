-- 物料凭证相关表结构
-- 用于支持物料凭证概览页面功能

-- 1. 物料凭证头表
CREATE TABLE erp_material_document (
    material_document_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物料凭证ID',
    material_document VARCHAR(20) NOT NULL COMMENT '物料凭证号码',
    material_document_year VARCHAR(4) NOT NULL COMMENT '物料凭证年份',
    plant_id BIGINT NOT NULL COMMENT '工厂ID',
    posting_date DATE NOT NULL COMMENT '过账日期',
    document_date DATE NOT NULL COMMENT '凭证日期',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_material_doc_year (material_document, material_document_year),
    FOREIGN KEY (plant_id) REFERENCES erp_plant_name(plant_id)
);

-- 2. 物料凭证项目表
CREATE TABLE erp_material_document_item (
    material_document_id BIGINT NOT NULL COMMENT '物料凭证ID',
    item_no SMALLINT NOT NULL COMMENT '项目编号',
    mat_id BIGINT NOT NULL COMMENT '物料ID',
    quantity DECIMAL(13,3) NOT NULL COMMENT '数量',
    unit VARCHAR(10) NOT NULL COMMENT '单位',
    movement_type VARCHAR(10) NOT NULL COMMENT '移动类型',
    storage_loc VARCHAR(10) COMMENT '库存地点',
    
    PRIMARY KEY (material_document_id, item_no),
    FOREIGN KEY (material_document_id) REFERENCES erp_material_document(material_document_id),
    FOREIGN KEY (mat_id) REFERENCES erp_material(mat_id),
    FOREIGN KEY (storage_loc) REFERENCES erp_storage_location(loc_id)
);

-- 3. 物料凭证业务流程关联表
CREATE TABLE erp_material_document_process (
    material_document_id BIGINT NOT NULL COMMENT '物料凭证ID',
    dlv_id BIGINT COMMENT '交货单ID',
    bill_id BIGINT COMMENT '会计凭证ID',
    so_id BIGINT COMMENT '销售订单ID',
    
    PRIMARY KEY (material_document_id),
    FOREIGN KEY (material_document_id) REFERENCES erp_material_document(material_document_id),
    FOREIGN KEY (dlv_id) REFERENCES erp_outbound_delivery(dlv_id),
    FOREIGN KEY (bill_id) REFERENCES erp_billing_hdr(bill_id),
    FOREIGN KEY (so_id) REFERENCES erp_sales_order_hdr(so_id)
);

-- 4. 移动类型参考表
CREATE TABLE erp_movement_type (
    movement_type VARCHAR(10) PRIMARY KEY COMMENT '移动类型代码',
    description VARCHAR(100) NOT NULL COMMENT '移动类型描述',
    movement_indicator VARCHAR(10) COMMENT '移动指示符（收货/发货/转储等）'
);

-- 插入基础移动类型数据
INSERT INTO erp_movement_type (movement_type, description, movement_indicator) VALUES
('101', '收货到库存', 'RECEIPT'),
('261', '发货到客户', 'ISSUE'),
('311', '库存地点间转储', 'TRANSFER'),
('501', '库存盘点差异', 'ADJUSTMENT'),
('601', '货物退回', 'RETURN');
