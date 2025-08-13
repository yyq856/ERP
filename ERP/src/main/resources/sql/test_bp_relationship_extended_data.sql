-- 测试业务伙伴关系扩展数据功能的示例数据

-- 首先执行 ALTER TABLE 语句（如果还未执行）
-- source create_bp_relationship_extended_tables.sql

-- 插入测试数据
-- Customer 类型的业务伙伴关系
INSERT INTO erp_relation (
    rel_category, bp1, bp2, management, department, `function`, 
    valid_from, valid_to,
    customer_code, customer_name, contact_person
) VALUES (
    'customer', 1, 1, 1, '01', '01',
    '2024-01-01', '2024-12-31',
    'CUST001', '测试客户公司', '张三'
);

-- ContactPerson 类型的业务伙伴关系
INSERT INTO erp_relation (
    rel_category, bp1, bp2, management, department, `function`, 
    valid_from, valid_to,
    test_field, description
) VALUES (
    'ContactPerson', 1, 1, 1, '01', '01',
    '2024-01-01', '2024-12-31',
    '测试字段值', '这是一个联系人类型的业务伙伴关系描述'
);

-- test 类型的业务伙伴关系
INSERT INTO erp_relation (
    rel_category, bp1, bp2, management, department, `function`, 
    valid_from, valid_to,
    test_field, description,
    extended_data
) VALUES (
    'test', 1, 1, 1, '01', '01',
    '2024-01-01', '2024-12-31',
    '高级测试字段', '测试类型的详细描述信息',
    '{"additionalField": "额外数据", "priority": "high"}'
);

-- 查询验证数据
SELECT 
    relation_id, rel_category, bp1, bp2,
    customer_code, customer_name, contact_person,
    test_field, description, extended_data,
    created_at, updated_at
FROM erp_relation 
WHERE rel_category IN ('customer', 'ContactPerson', 'test')
ORDER BY relation_id DESC;