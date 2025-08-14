# Inquiry API 完整字段支持测试文档

## 概述
本文档描述了更新后的 Inquiry 系统，现在支持与 ItemValidation 相同的完整字段结构。

## 数据库变更

### 1. 扩展 `erp_inquiry_item` 表
执行以下 SQL 脚本添加新字段：
```sql
-- 位置：ERP/src/main/resources/sql/extend_inquiry_item_table.sql
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
```

## API 接口测试

### 1. 询价单创建 (POST /api/app/inquiry/edit)

#### 请求示例
```json
{
  "meta": {
    "id": ""
  },
  "basicInfo": {
    "soldToParty": "CUST-1",
    "shipToParty": "SHIP-1",
    "customerReference": "REF-2024-001",
    "netValue": 15000.00,
    "customerReferenceDate": "2024-01-15"
  },
  "itemOverview": {
    "validFrom": "2024-01-15",
    "validTo": "2024-07-15",
    "reqDelivDate": "2024-02-15",
    "expectOralVal": "16500.00",
    "expectOralValUnit": "USD",
    "items": [
      {
        "item": "1",
        "material": "MAT-001",
        "orderQuantity": "10",
        "orderQuantityUnit": "EA",
        "description": "高质量产品A",
        "reqDelivDate": "2024-02-15",
        "netValue": "1500.00",
        "netValueUnit": "USD",
        "taxValue": "195.00",
        "taxValueUnit": "USD",
        "pricingDate": "2024-01-15",
        "orderProbability": "95",
        "pricingElements": [
          {
            "cnty": "BASE",
            "name": "Base Price",
            "amount": "150.00",
            "city": "USD",
            "per": "1",
            "uom": "EA",
            "conditionValue": "1500.00",
            "curr": "USD",
            "status": "Active",
            "numC": "",
            "atoMtsComponent": "",
            "oun": "",
            "cconDe": "",
            "un": "",
            "conditionValue2": "",
            "cdCur": "",
            "stat": true
          }
        ]
      }
    ]
  }
}
```

#### 预期响应
```json
{
  "success": true,
  "message": "创建询价单成功",
  "data": {
    "message": "Inquiry INQ-2024-001 has been created successfully",
    "content": {
      "id": "INQ-2024-001"
    }
  }
}
```

### 2. 询价单查询 (POST /api/app/inquiry/get)

#### 请求示例
```json
{
  "inquiryId": "1"
}
```

#### 预期响应
```json
{
  "success": true,
  "message": "获取询价单成功",
  "data": {
    "content": {
      "meta": {
        "id": "INQ-2024-001"
      },
      "basicInfo": {
        "inquiry": "INQ-2024-001",
        "soldToParty": "CUST-1",
        "shipToParty": "SHIP-1",
        "customerReference": "REF-2024-001",
        "netValue": 15000.0,
        "netValueUnit": "USD",
        "customerReferenceDate": "2024-01-15"
      },
      "itemOverview": {
        "validFrom": "2024-01-15",
        "validTo": "2024-07-15",
        "reqDelivDate": "2024-02-15",
        "expectOralVal": "16500.00",
        "expectOralValUnit": "USD",
        "items": [
          {
            "item": "1",
            "material": "MAT-001",
            "orderQuantity": "10",
            "orderQuantityUnit": "EA",
            "description": "高质量产品A",
            "reqDelivDate": "2024-02-15",
            "netValue": "1500.00",
            "netValueUnit": "USD",
            "taxValue": "195.00",
            "taxValueUnit": "USD",
            "pricingDate": "2024-01-15",
            "orderProbability": "95",
            "pricingElements": [
              {
                "cnty": "BASE",
                "name": "Base Price",
                "amount": "150.00",
                "city": "USD",
                "per": "1",
                "uom": "EA",
                "conditionValue": "1500.00",
                "curr": "USD",
                "status": "Active",
                "numC": "",
                "atoMtsComponent": "",
                "oun": "",
                "cconDe": "",
                "un": "",
                "conditionValue2": "",
                "cdCur": "",
                "stat": true
              }
            ]
          }
        ]
      }
    }
  }
}
```

### 3. 物品批量查询 (POST /api/app/inquiry/items-tab-query)

#### 请求示例
```json
[
  {
    "item": "",
    "material": "MAT-001",
    "orderQuantity": "",
    "orderQuantityUnit": "",
    "description": "",
    "reqDelivDate": "",
    "netValue": "",
    "netValueUnit": "",
    "taxValue": "",
    "taxValueUnit": "",
    "pricingDate": "",
    "orderProbability": ""
  }
]
```

#### 预期响应
```json
{
  "success": true,
  "message": "批量查询成功",
  "data": {
    "summary": {
      "totalNetValue": "100.00",
      "totalExpectOralVal": "110.00",
      "currency": "CNY"
    },
    "breakdowns": [
      {
        "item": "1",
        "material": "MAT-001",
        "orderQuantity": "1",
        "orderQuantityUnit": "EA",
        "description": "物料 MAT-001",
        "reqDelivDate": "2024-01-15",
        "netValue": 100.0,
        "netValueUnit": "CNY",
        "taxValue": 13.0,
        "taxValueUnit": "CNY",
        "pricingDate": "2024-01-15",
        "orderProbability": "100",
        "pricingElements": [
          {
            "cnty": "BASE",
            "name": "基础价格",
            "amount": "100.00",
            "city": "CNY",
            "per": "1",
            "uom": "EA",
            "conditionValue": "100.00",
            "curr": "CNY",
            "status": "",
            "numC": "",
            "atoMtsComponent": "",
            "oun": "",
            "cconDe": "",
            "un": "",
            "conditionValue2": "",
            "cdCur": "",
            "stat": true
          }
        ]
      }
    ],
    "badRecordIndices": []
  }
}
```

## 核心特性

### 1. 完整字段支持
- ✅ 支持所有 ItemValidation 的字段
- ✅ 保存时将完整字段存储到数据库
- ✅ 查询时返回完整字段信息

### 2. 智能数据填充
- ✅ 最小输入：只需要 `material` 字段
- ✅ 自动填充：缺失字段使用默认值或数据库查询结果

### 3. 定价元素支持
- ✅ 完整的定价元素结构
- ✅ JSON 格式存储复杂定价信息
- ✅ 支持多种定价条件类型

### 4. 数据一致性
- ✅ Inquiry 与 ItemValidation 使用相同的数据结构
- ✅ 支持数据在不同模块间流转
- ✅ 保持向后兼容性

## 测试步骤

1. **数据库准备**
   ```bash
   # 执行数据库扩展脚本
   mysql -u your_user -p your_database < ERP/src/main/resources/sql/extend_inquiry_item_table.sql
   ```

2. **应用启动**
   ```bash
   cd ERP
   mvn spring-boot:run
   ```

3. **接口测试**
   - 使用 Postman 或其他 HTTP 客户端
   - 按照上述示例进行测试
   - 验证数据保存和提取的完整性

## 常见问题

### Q: 如何处理空字段？
A: 系统会自动使用默认值填充空字段，确保数据完整性。

### Q: 定价元素如何存储？
A: 定价元素以 JSON 格式存储在 `pricing_elements_json` 字段中。

### Q: 是否保持向后兼容？
A: 是的，现有的 Inquiry 功能完全保持不变，新功能是扩展性的。

### Q: 如何验证数据完整性？
A: 每次保存和查询都会验证数据结构，确保字段完整性。