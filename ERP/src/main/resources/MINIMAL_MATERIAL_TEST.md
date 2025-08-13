# 最简化物品验证测试用例

## 测试目标
只输入 `material` 字段，其他所有字段为空，验证接口能返回完整的响应数据。

## 测试用例

### 1. 最简化请求（只有material）

```json
POST /api/app/items/items-tab-query
Content-Type: application/json

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
    "orderProbability": "",
    "pricingElements": []
  }
]
```

### 2. 期望的完整响应

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
      "netValue": "100.00",
      "netValueUnit": "CNY",
      "expectOralVal": "113.00",
      "expectOralValUnit": "CNY"
    },
    "breakdowns": [
      {
        "item": "1",
        "material": "MAT-001",
        "orderQuantity": "1",
        "orderQuantityUnit": "EA",
        "description": "物料 MAT-001",
        "reqDelivDate": "2025-01-13",
        "netValue": 100.0,
        "netValueUnit": "CNY",
        "taxValue": 13.0,
        "taxValueUnit": "CNY",
        "pricingDate": "2025-01-13",
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
    ]
  }
}
```

## 关键验证点

### ✅ 输入验证
- 只需要 `material: "MAT-001"`
- 其他所有字段都为空字符串或空数组
- 接口应该**不会**返回错误

### ✅ 响应验证
1. **成功状态**
   - `success: true`
   - `allDataLegal: 1`
   - `badRecordIndices: []` (空数组)

2. **自动填充的字段**
   - `item: "1"` (自动生成行号)
   - `orderQuantity: "1"` (默认数量)
   - `orderQuantityUnit: "EA"` (默认单位)
   - `description: "物料 MAT-001"` (根据material生成)
   - `reqDelivDate: "2025-01-13"` (当前日期)
   - `pricingDate: "2025-01-13"` (当前日期)
   - `orderProbability: "100"` (默认概率)

3. **计算字段**
   - `netValue: 100.0` (根据基础价格和数量计算)
   - `taxValue: 13.0` (netValue * 0.13)
   - `netValueUnit: "CNY"` (默认货币)
   - `taxValueUnit: "CNY"` (默认货币)

4. **定价元素**
   - 自动生成基础价格定价元素
   - `cnty: "BASE"`
   - `name: "基础价格"`
   - `amount: "100.00"` (物料标准价格)
   - `conditionValue: "100.00"` (amount * quantity)

### ✅ 总计验证
- `generalData.netValue: "100.00"` (所有item的净值总和)
- `generalData.expectOralVal: "113.00"` (净值 + 税值)

## 测试步骤

1. 启动ERP应用
2. 发送上述POST请求到 `/api/app/items/items-tab-query`
3. 验证响应状态为200
4. 验证响应JSON结构符合期望
5. 确认所有字段都有值，没有null或undefined

## 预期行为

- **输入**: 只有material字段有值
- **处理**: 接口自动填充所有其他必要字段
- **输出**: 完整的响应数据，包含所有计算结果

这个测试用例验证了接口的核心功能：**最小输入，最大输出**。