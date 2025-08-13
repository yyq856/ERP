# 业务伙伴关系扩展数据 API 测试指南

## 概述
此文档展示了如何测试增强后的业务伙伴关系API，支持根据不同的关系类型存储额外数据。

## 数据库准备
首先执行以下SQL脚本来准备数据库：
```sql
-- 1. 执行表结构扩展
source src/main/resources/sql/create_bp_relationship_extended_tables.sql

-- 2. 插入测试数据（可选）
source src/main/resources/sql/test_bp_relationship_extended_data.sql
```

## API 测试用例

### 1. Customer 类型 - 注册接口测试

**POST** `/api/app/bp-relationship/register`

```json
{
  "relation": {
    "relationShipCategory": "customer"
  },
  "default": {
    "businessPartner1": "1",
    "businessPartner2": "1",
    "validFrom": "2024-01-01",
    "validTo": "2024-12-31"
  },
  "generalData": {
    "customerCode": "CUST002",
    "customerName": "新测试客户",
    "contactPerson": "李四"
  }
}
```

### 2. ContactPerson 类型 - 编辑接口测试

**POST** `/api/app/bp-relationship/edit`

```json
{
  "bpRelationshipData": {
    "basicInfo": {
      "relation": {
        "relationShipCategory": "ContactPerson"
      },
      "default": {
        "businessPartner1": "1",
        "businessPartner2": "1",
        "validFrom": "2024-01-01",
        "validTo": "2024-12-31"
      }
    }
  },
  "generalData": {
    "testField": "联系人测试字段",
    "description": "这是联系人类型的详细描述"
  }
}
```

### 3. Test 类型 - 编辑接口测试

**POST** `/api/app/bp-relationship/edit`

```json
{
  "bpRelationshipData": {
    "meta": {
      "id": "1"
    },
    "basicInfo": {
      "relation": {
        "relationShipCategory": "test"
      },
      "default": {
        "businessPartner1": "1",
        "businessPartner2": "1",
        "validFrom": "2024-01-01",
        "validTo": "2024-12-31"
      }
    }
  },
  "generalData": {
    "testField": "更新的测试字段",
    "description": "更新后的描述信息",
    "customField": "自定义扩展字段"
  }
}
```

### 4. 查询接口测试

**POST** `/api/app/bp-relationship/get`

```json
{
  "relationshipId": "1"
}
```

**预期响应格式：**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "content": {
      "basicInfo": {
        "meta": {
          "id": "1"
        },
        "basicInfo": {
          "relation": {
            "relationShipCategory": "customer"
          },
          "default": {
            "businessPartner1": "1",
            "businessPartner2": "1",
            "validFrom": "2024-01-01",
            "validTo": "2024-12-31"
          }
        }
      },
      "generalData": {
        "customerCode": "CUST001",
        "customerName": "测试客户公司",
        "contactPerson": "张三"
      }
    },
    "formStruct": {
      // 动态表单结构
    }
  }
}
```

## 字段映射说明

### Customer 类型扩展字段
| 前端字段 | 数据库字段 | 说明 |
|---------|-----------|------|
| customerCode | customer_code | 客户代码 |
| customerName | customer_name | 客户名称 |
| contactPerson | contact_person | 联系人 |

### ContactPerson/Test 类型扩展字段
| 前端字段 | 数据库字段 | 说明 |
|---------|-----------|------|
| testField | test_field | 测试字段 |
| description | description | 描述 |

### 通用扩展字段
| 前端字段 | 数据库字段 | 说明 |
|---------|-----------|------|
| generalData的所有字段 | extended_data | JSON格式存储，支持动态扩展 |

## 测试验证点

1. **数据存储验证**：检查扩展字段是否正确存储到对应的数据库字段
2. **类型区分验证**：不同关系类型是否使用了正确的字段映射
3. **JSON扩展验证**：所有扩展数据是否正确序列化为JSON存储
4. **查询回显验证**：查询接口是否正确返回存储的扩展数据
5. **动态表单验证**：表单结构是否根据关系类型正确生成

## 数据库查询验证

```sql
-- 查看所有业务伙伴关系的扩展数据
SELECT 
    relation_id,
    rel_category,
    customer_code,
    customer_name,
    contact_person,
    test_field,
    description,
    extended_data,
    created_at,
    updated_at
FROM erp_relation 
WHERE customer_code IS NOT NULL 
   OR customer_name IS NOT NULL 
   OR contact_person IS NOT NULL 
   OR test_field IS NOT NULL 
   OR description IS NOT NULL 
   OR extended_data IS NOT NULL
ORDER BY relation_id DESC;
```

## 错误处理测试

1. **无效关系类型**：测试不支持的关系类型
2. **缺失必填字段**：测试基本信息缺失的情况
3. **数据格式错误**：测试日期格式、数字格式错误
4. **JSON序列化错误**：测试复杂对象的JSON转换

## 注意事项

1. 确保数据库表结构已正确扩展
2. 测试前需要确保基础数据（如客户、联系人）已存在
3. 扩展数据的处理是可选的，即使扩展数据处理失败也不会影响主要业务流程
4. JSON扩展字段支持存储任意复杂的数据结构，但建议保持数据结构的一致性