# 物品条件查询接口测试文档

## 接口概述

物品条件查询接口（items-tab-query）支持批量验证和计算物品的定价信息，返回详细的定价元素和验证结果。

## 支持的应用端点

| 应用类型 | 端点路径 | 说明 |
|---------|---------|------|
| 询价单 | POST /api/app/inquiry/items-tab-query | 询价单物品验证服务 |
| 报价单 | POST /api/app/quotation/items-tab-query | 报价单物品验证服务 |
| 销售订单 | POST /api/app/so/items-tab-query | 销售订单物品验证服务 |
| 开票凭证 | POST /api/app/billing/items-tab-query | 开票凭证物品验证服务 |

## 请求格式

### Content-Type
```
application/json
```

### 请求体结构
```json
[
  {
    "item": "10",
    "material": "1001",
    "orderQuantity": "2",
    "orderQuantityUnit": "PC",
    "description": "标准电脑主板",
    "reqDelivDate": "2025-02-15",
    "netValue": "2400.00",
    "netValueUnit": "CNY",
    "taxValue": "312.00",
    "taxValueUnit": "CNY",
    "pricingDate": "2025-01-13",
    "orderProbability": "100",
    "pricingElements": [
      {
        "cnty": "BASE",
        "name": "基础价格",
        "amount": "1200.00",
        "city": "CNY",
        "per": "1",
        "uom": "PC",
        "conditionValue": "2400.00",
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
]
```

## 响应格式

### 成功响应
```json
{
  "success": true,
  "message": "批量验证成功",
  "data": {
    "result": {
      "allDataLegal": 1,
      "badRecordIndices": []
    },
    "generalData": {
      "netValue": "2400.00",
      "netValueUnit": "CNY",
      "expectOralVal": "2712.00",
      "expectOralValUnit": "CNY"
    },
    "breakdowns": [
      {
        "item": "1",
        "material": "1001",
        "orderQuantity": "2",
        "orderQuantityUnit": "PC",
        "description": "标准电脑主板",
        "reqDelivDate": "2025-02-15",
        "netValue": 2400.0,
        "netValueUnit": "CNY",
        "taxValue": 312.0,
        "taxValueUnit": "CNY",
        "pricingDate": "2025-01-13",
        "orderProbability": "100",
        "pricingElements": [
          {
            "cnty": "BASE",
            "name": "基础价格",
            "amount": "1200.00",
            "city": "CNY",
            "per": "1",
            "uom": "PC",
            "conditionValue": "2400.00",
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
    ]
  }
}
```

### 错误响应
```json
{
  "success": false,
  "message": "验证失败，请检查输入数据"
}
```

## 测试用例

### 测试用例1：单个物品验证
```bash
curl -X POST "http://localhost:8080/api/app/inquiry/items-tab-query" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "item": "10",
      "material": "1001",
      "orderQuantity": "1",
      "orderQuantityUnit": "PC",
      "description": "标准电脑主板",
      "reqDelivDate": "2025-02-15",
      "pricingDate": "2025-01-13",
      "orderProbability": "100"
    }
  ]'
```

### 测试用例2：多个物品批量验证
```bash
curl -X POST "http://localhost:8080/api/app/quotation/items-tab-query" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "item": "10",
      "material": "1001",
      "orderQuantity": "2",
      "orderQuantityUnit": "PC"
    },
    {
      "item": "20",
      "material": "1002",
      "orderQuantity": "1",
      "orderQuantityUnit": "PC"
    }
  ]'
```

### 测试用例3：包含定价元素的物品验证
```bash
curl -X POST "http://localhost:8080/api/app/so/items-tab-query" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "item": "10",
      "material": "1001",
      "orderQuantity": "1",
      "orderQuantityUnit": "PC",
      "pricingElements": [
        {
          "cnty": "BASE",
          "name": "基础价格",
          "amount": "1200.00",
          "city": "CNY",
          "per": "1",
          "uom": "PC",
          "conditionValue": "1200.00",
          "curr": "CNY",
          "stat": true
        },
        {
          "cnty": "DISC",
          "name": "固定折扣",
          "amount": "-100.00",
          "city": "CNY",
          "per": "1",
          "uom": "PC",
          "conditionValue": "-100.00",
          "curr": "CNY",
          "stat": true
        }
      ]
    }
  ]'
```

### 测试用例4：验证失败场景（不存在的物料）
```bash
curl -X POST "http://localhost:8080/api/app/billing/items-tab-query" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "item": "10",
      "material": "999999",
      "orderQuantity": "1",
      "orderQuantityUnit": "PC"
    }
  ]'
```

## 字段说明

### 请求字段
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| item | string | 是 | 项目号 |
| material | string | 是 | 物料号 |
| orderQuantity | string | 是 | 订单数量 |
| orderQuantityUnit | string | 是 | 订单数量单位 |
| description | string | 否 | 物料描述 |
| reqDelivDate | string | 否 | 要求交货日期 (YYYY-MM-DD) |
| netValue | string | 否 | 净值 |
| netValueUnit | string | 否 | 净值单位 |
| taxValue | string | 否 | 税值 |
| taxValueUnit | string | 否 | 税值单位 |
| pricingDate | string | 否 | 定价日期 (YYYY-MM-DD) |
| orderProbability | string | 否 | 订单概率 |
| pricingElements | array | 否 | 定价元素列表 |

### 响应字段
| 字段名 | 类型 | 说明 |
|--------|------|------|
| success | boolean | 请求是否成功 |
| message | string | 响应消息 |
| data.result.allDataLegal | integer | 1表示所有数据合法，0表示存在不合法数据 |
| data.result.badRecordIndices | array | 不合法数据的索引列表 |
| data.generalData.netValue | string | 总净值 |
| data.generalData.expectOralVal | string | 总预期口头值 |
| data.breakdowns | array | 物品明细列表 |

## 业务规则

1. **数据验证规则**
   - 必填字段：item、material、orderQuantity、orderQuantityUnit
   - 物料必须在系统中存在且有效
   - 数量必须为有效数值

2. **定价计算规则**
   - 基础价格 = 物料标准价格 × 订单数量
   - 税值 = 净值 × 税率（默认13%）
   - 预期口头值 = 净值 + 税值

3. **默认值规则**
   - 默认交货日期：当前日期
   - 默认定价日期：当前日期
   - 默认订单概率：100
   - 默认货币单位：CNY

## 注意事项

1. 所有接口都支持批量处理，可以同时验证多个物品
2. 验证失败的物品会在badRecordIndices中标记索引
3. 定价元素的计算遵循ERP标准定价逻辑
4. 接口支持实时验证，适用于用户输入时的即时反馈