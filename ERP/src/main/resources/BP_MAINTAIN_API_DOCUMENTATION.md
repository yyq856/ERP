# BP 维护页面接口文档

## 接口概述

BP 维护页面提供了完整的业务伙伴管理功能，包括搜索、查看详情、创建和编辑业务伙伴信息。

## 数据库表结构更新

已对 `erp_customer` 表进行了扩展，新增以下字段：
- `first_name VARCHAR(60)` - person类型的名字
- `last_name VARCHAR(60)` - person类型的姓氏  
- `bp_type VARCHAR(10)` - 业务伙伴类型：person/org/group
- `search_term VARCHAR(60)` - 搜索词

## 接口列表

### 1. 业务伙伴搜索接口

**接口地址：** `POST /api/bp/search`

**接口描述：** 根据查询条件搜索业务伙伴列表

**请求参数：**
```json
{
  "query": {
    "customerId": "string" // 业务伙伴ID，支持模糊查询
  }
}
```

**响应结果：**
```json
{
  "success": true,
  "message": "查询成功",
  "data": [
    {
      "customerId": "string",    // 业务伙伴ID
      "name": "string",          // org和group类型名称
      "firstName": "string",     // person类型名字
      "lastName": "string",      // person类型姓氏
      "city": "string",          // 城市
      "country": "string",       // 国家
      "bpRole": "string",        // BP角色
      "type": "string"           // 客户类型：person/org/group
    }
  ]
}
```

### 2. 获取业务伙伴详情接口

**接口地址：** `GET /api/bp/get/{customerId}`

**接口描述：** 根据业务伙伴ID获取其详细信息

**请求参数：** 
- `customerId`: string (路径参数) - 业务伙伴ID

**响应结果：**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "bpIdAndRoleSection": {
      "customerId": "string",
      "bpRole": "string",
      "type": "string"           // person/org/group
    },
    "name": {
      "title": "string",
      "name": "string",
      "firstName": "string",     // person类型
      "lastName": "string"       // person类型
    },
    "searchTerms": {
      "searchTerm": "string"
    },
    "address": {
      "country": "string",
      "street": "string",
      "postalCode": "string",
      "city": "string"
    }
  }
}
```

### 3. 创建/编辑业务伙伴接口

**接口地址：** `POST /api/bp/edit`

**接口描述：** 用于创建新的业务伙伴或修改现有业务伙伴的详细信息

**请求参数：**
```json
{
  "bpIdAndRoleSection": {
    "customerId": "string",    // 编辑时必填，创建时可为空
    "bpRole": "string",
    "type": "string"           // person/org/group
  },
  "name": {
    "title": "string",
    "name": "string",
    "firstName": "string",     // person类型
    "lastName": "string"       // person类型
  },
  "searchTerms": {
    "searchTerm": "string"
  },
  "address": {
    "country": "string",
    "street": "string",
    "postalCode": "string",
    "city": "string"
  }
}
```

**响应结果：**
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "customerId": "string"     // 创建时返回新生成的ID，修改时返回原ID
  }
}
```

### 4. 搜索辅助接口

#### 4.1 业务伙伴搜索辅助接口
**接口地址：** `GET /api/search/business-partner?query={keyword}`

**接口描述：** 用于为customerId字段提供搜索建议或校验

#### 4.2 关系搜索辅助接口  
**接口地址：** `GET /api/search/relation?query={keyword}`

**接口描述：** 用于搜索关系类型，为关系分类字段提供搜索功能

## 实现特点

1. **完整的CRUD操作** - 支持创建、读取、更新业务伙伴信息
2. **类型支持** - 支持person、org、group三种业务伙伴类型
3. **模糊搜索** - 支持按业务伙伴ID进行模糊查询
4. **数据验证** - 完整的参数验证和错误处理
5. **统一响应格式** - 所有接口使用统一的响应格式
6. **日志记录** - 完整的操作日志记录

## 错误处理

所有接口都包含完整的错误处理机制：
- 参数验证错误
- 业务逻辑错误  
- 系统异常错误

错误响应格式：
```json
{
  "success": false,
  "message": "错误描述信息"
}
```
