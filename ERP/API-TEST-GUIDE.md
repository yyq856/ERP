# 物品验证服务接口测试指南

## 问题诊断和解决方案

您遇到的400错误可能有以下几个原因和解决方案：

### 1. 快速测试方案

我已经创建了一个简化的测试接口，不依赖数据库：

**测试端点**: `POST /test-items-tab-query`

**测试命令**:
```bash
curl -X POST http://localhost:8080/test-items-tab-query \
  -H "Content-Type: application/json" \
  -d '[{
    "item": "001",
    "material": "1001", 
    "orderQuantity": "2",
    "orderQuantityUnit": "PC",
    "description": "测试物品",
    "reqDelivDate": "2024-03-15",
    "pricingDate": "2024-03-01",
    "orderProbability": "90"
  }]'
```

### 2. 原始接口测试

**正式端点**: `POST /items-tab-query`

#### 前置条件：
1. 确保数据库已启动
2. 执行测试数据脚本：
```sql
SOURCE ERP/src/main/resources/sql/build.sql;
SOURCE ERP/src/main/resources/sql/insert_validate_items_test_data.sql;
```

#### 测试命令：
```bash
curl -X POST http://localhost:8080/items-tab-query \
  -H "Content-Type: application/json" \
  -d '[{
    "item": "001",
    "material": "1001",
    "orderQuantity": "2", 
    "orderQuantityUnit": "PC",
    "description": "标准电脑主板采购",
    "reqDelivDate": "2024-03-15",
    "netValue": "2400.00",
    "netValueUnit": "CNY",
    "taxValue": "240.00", 
    "taxValueUnit": "CNY",
    "pricingDate": "2024-03-01",
    "orderProbability": "90",
    "pricingElements": []
  }]'
```

### 3. 常见400错误原因及解决方案

#### 3.1 JSON格式错误
- **问题**: 请求体JSON格式不正确
- **解决**: 确保JSON格式正确，注意括号、引号、逗号
- **检查**: 使用JSON验证工具验证请求体

#### 3.2 Content-Type错误
- **问题**: 缺少或错误的Content-Type头
- **解决**: 确保设置 `Content-Type: application/json`

#### 3.3 必填字段缺失
- **问题**: 缺少必填字段: item, material, orderQuantity, orderQuantityUnit
- **解决**: 确保所有必填字段都有值

#### 3.4 数据类型错误
- **问题**: 字段类型不匹配
- **解决**: 
  - `material` 应为数字字符串 (如 "1001")
  - `orderQuantity` 应为数字字符串 (如 "2")
  - `orderProbability` 应为数字字符串 (如 "90")

### 4. 调试步骤

#### 步骤1: 检查应用启动
```bash
curl -X GET http://localhost:8080/actuator/health
```

#### 步骤2: 测试简化接口
```bash
curl -X POST http://localhost:8080/test-items-tab-query \
  -H "Content-Type: application/json" \
  -d '[{"item":"001","material":"1001","orderQuantity":"1","orderQuantityUnit":"PC"}]'
```

#### 步骤3: 查看日志
检查应用日志中的错误信息，特别关注：
- JSON解析错误
- 数据库连接错误  
- 参数验证错误

### 5. 最小测试用例

**最简请求体**:
```json
[{
  "item": "001",
  "material": "1001", 
  "orderQuantity": "1",
  "orderQuantityUnit": "PC"
}]
```

**期望响应**:
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
      "netValue": "1000.0",
      "netValueUnit": "CNY", 
      "expectOralVal": "1100.0",
      "expectOralValUnit": "CNY"
    },
    "breakdowns": [
      {
        "item": "001",
        "material": "1001",
        "orderQuantity": "1",
        "orderQuantityUnit": "PC",
        "netValue": 1000.0,
        "netValueUnit": "CNY",
        "taxValue": 100.0,
        "taxValueUnit": "CNY",
        "pricingElements": []
      }
    ]
  }
}
```

### 6. Postman测试配置

1. **方法**: POST
2. **URL**: `http://localhost:8080/test-items-tab-query`
3. **Headers**: 
   - `Content-Type: application/json`
4. **Body**: 选择 raw + JSON，粘贴测试数据

### 7. 如果仍然出现400错误

请提供以下信息以进一步诊断：
1. 完整的错误响应内容
2. 应用日志中的错误信息
3. 您使用的具体请求命令或Postman配置
4. Spring Boot应用是否正常启动

### 8. 验证应用状态

```bash
# 检查应用是否启动
curl -X GET http://localhost:8080/actuator/health

# 检查其他已有接口是否正常
curl -X GET http://localhost:8080/api/material/debug/count
```

---

建议先使用简化的测试接口 `/test-items-tab-query` 来验证JSON格式和基本功能，成功后再使用正式的 `/items-tab-query` 接口。