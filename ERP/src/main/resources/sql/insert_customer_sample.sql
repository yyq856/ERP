-- 插入示例客户数据
-- 注意：请先执行 insert_reference_data.sql 来插入参考数据

-- 插入个人客户
INSERT INTO erp_customer (
    title,                    -- 公司类型
    name,                     -- 公司名称
    language,                 -- 语言
    street,                   -- 街道
    city,                     -- 城市
    region,                   -- 地区
    postal_code,              -- 邮政编码
    country,                  -- 国家
    company_code,             -- 公司代码
    reconciliation_account,   -- 统驭科目
    sort_key,                 -- 排序关键字
    sales_org,                -- 销售组织
    channel,                  -- 分销渠道
    division,                 -- 产品组
    currency,                 -- 货币
    sales_district,           -- 销售地区
    price_group,              -- 价格组
    customer_group,           -- 客户组
    delivery_priority,        -- 交货优先级
    shipping_condition,       -- 装运条件
    delivering_plant,         -- 交货工厂
    max_part_deliv,           -- 最大部分交货
    incoterms,                -- 国际贸易条款
    incoterms_location,       -- 贸易条款地点
    payment_terms,            -- 付款条件
    acct_assignment,          -- 科目分配组
    output_tax                -- 销项税
) VALUES (
    'PI',                     -- 个人类型
    'John Doe Company',       -- 公司名称
    'EN',                     -- 英语
    '123 Business Street',    -- 街道地址
    'New York',               -- 城市
    'NY',                     -- 地区
    '10001',                  -- 邮政编码
    'US',                     -- 美国
    '1000',                   -- 公司代码
    '140000',                 -- 统驭科目
    '001',                    -- 排序关键字
    '1000',                   -- 销售组织
    10,                       -- 分销渠道
    '01',                     -- 产品组
    'USD',                    -- 美元
    '000001',                 -- 销售地区
    '01',                     -- 价格组
    '01',                     -- 客户组
    '02',                     -- 交货优先级
    '01',                     -- 装运条件
    1000,                     -- 交货工厂ID
    5,                        -- 最大部分交货数量
    'EXW',                    -- 工厂交货
    'Factory',                -- 贸易条款地点
    '0001',                   -- 付款条件
    '01',                     -- 科目分配组
    1                         -- 销项税
);

-- 插入组织客户
INSERT INTO erp_customer (
    title, name, language, street, city, region, postal_code, country,
    company_code, reconciliation_account, sort_key, sales_org, channel,
    division, currency, sales_district, price_group, customer_group,
    delivery_priority, shipping_condition, delivering_plant, max_part_deliv,
    incoterms, incoterms_location, payment_terms, acct_assignment, output_tax
) VALUES (
    'GR',                     -- 组织类型
    'ABC Corporation Group',  -- 组织名称
    'EN',                     -- 英语
    '456 Corporate Ave',      -- 街道地址
    'Los Angeles',            -- 城市
    'CA',                     -- 地区
    '90001',                  -- 邮政编码
    'US',                     -- 美国
    '1000',                   -- 公司代码
    '140000',                 -- 统驭科目
    '002',                    -- 排序关键字（组织用）
    '1000',                   -- 销售组织
    10,                       -- 分销渠道
    '01',                     -- 产品组
    'USD',                    -- 美元
    '000001',                 -- 销售地区
    '02',                     -- 价格组（组织用）
    '02',                     -- 客户组（VIP）
    '01',                     -- 交货优先级（高）
    '02',                     -- 装运条件（快递）
    1000,                     -- 交货工厂ID
    10,                       -- 最大部分交货数量
    'EXW',                    -- 工厂交货
    'Factory',                -- 贸易条款地点
    '0001',                   -- 付款条件
    '01',                     -- 科目分配组
    1                         -- 销项税
);

-- 插入公司客户
INSERT INTO erp_customer (
    title, name, language, street, city, region, postal_code, country,
    company_code, reconciliation_account, sort_key, sales_org, channel,
    division, currency, sales_district, price_group, customer_group,
    delivery_priority, shipping_condition, delivering_plant, max_part_deliv,
    incoterms, incoterms_location, payment_terms, acct_assignment, output_tax
) VALUES (
    'COMP',                   -- 公司类型
    'Tech Solutions Ltd',     -- 公司名称
    'EN',                     -- 英语
    '789 Tech Park',          -- 街道地址
    'San Francisco',          -- 城市
    'CA',                     -- 地区
    '94102',                  -- 邮政编码
    'US',                     -- 美国
    '1000',                   -- 公司代码
    '140000',                 -- 统驭科目
    '001',                    -- 排序关键字
    '1000',                   -- 销售组织
    20,                       -- 分销渠道（零售）
    '01',                     -- 产品组
    'USD',                    -- 美元
    '000001',                 -- 销售地区
    '01',                     -- 价格组
    '03',                     -- 客户组（企业）
    '02',                     -- 交货优先级
    '01',                     -- 装运条件
    1000,                     -- 交货工厂ID
    5,                        -- 最大部分交货数量
    'EXW',                    -- 工厂交货
    'Factory',                -- 贸易条款地点
    '0002',                   -- 付款条件（60天）
    '01',                     -- 科目分配组
    1                         -- 销项税
);
