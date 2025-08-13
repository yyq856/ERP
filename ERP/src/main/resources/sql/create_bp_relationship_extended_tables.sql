-- 在现有 erp_relation 表基础上添加扩展字段
-- 用于存储根据不同关系类型的额外数据

-- 添加 Customer 类型的扩展字段
ALTER TABLE erp_relation
ADD COLUMN customer_code VARCHAR(50) COMMENT '客户代码（customer类型专用）',
ADD COLUMN customer_name VARCHAR(100) COMMENT '客户名称（customer类型专用）',
ADD COLUMN contact_person VARCHAR(100) COMMENT '联系人（customer类型专用）';

-- 添加 ContactPerson 和 test 类型的扩展字段
ALTER TABLE erp_relation
ADD COLUMN test_field VARCHAR(200) COMMENT '测试字段（ContactPerson/test类型专用）',
ADD COLUMN description TEXT COMMENT '描述（ContactPerson/test类型专用）';

-- 添加通用扩展字段
ALTER TABLE erp_relation
ADD COLUMN extended_data JSON COMMENT '扩展数据（JSON格式存储动态字段）',
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 为了性能优化，添加索引
CREATE INDEX idx_relation_customer_code ON erp_relation(customer_code);
CREATE INDEX idx_relation_test_field ON erp_relation(test_field);