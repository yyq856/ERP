# ERP数据库测试数据插入指南

## 执行顺序

请按照以下顺序执行SQL脚本，确保外键约束得到满足：

### 1. 检查当前数据状态（可选）
```sql
source ERP/src/main/resources/sql/check_reference_data.sql;
```

### 2. 插入所有参考表数据（必须先执行）
```sql
source ERP/src/main/resources/sql/insert_complete_test_data.sql;
```

### 3. 插入业务数据
```sql
source ERP/src/main/resources/sql/insert_business_test_data.sql;
```

## 数据概览

执行完成后，您将拥有以下测试数据：

### 参考数据
- **4个公司代码**: 1000, 2000, 3000, 4000
- **7种语言**: EN, ZH, DE, FR, ES, JA, KO
- **7种货币**: USD, EUR, CNY, GBP, JPY, KRW, CAD
- **7种客户称谓**: PI, GR, ORG, COMP, MR, MS, DR
- **5个工厂**: 1000-5000
- **6个销售地区**: 000001-000006
- **完整的价格组、客户组、付款条件等**

### 业务数据
- **7个测试客户**:
  - 2个个人客户 (PI)
  - 3个公司客户 (COMP)
  - 1个组织客户 (GR)
  - 1个国际客户
- **8个联系人**
- **7个客户-联系人关系**
- **12种物料**
- **12条库存记录**
- **5个测试账号**

## 测试用例数据

### 可用于API测试的客户数据

1. **个人客户**: John Smith Individual (customer_id: 1)
2. **公司客户**: ABC Corporation (customer_id: 3)
3. **组织客户**: Global Partners Group (customer_id: 5)

### 可用于登录测试的账号

- 账号: 1001, 密码: TestPass123
- 账号: 1002, 密码: AdminPass456
- 账号: 1003, 密码: UserPass789

## 验证数据插入

执行以下查询验证数据是否正确插入：

```sql
-- 检查所有表的记录数
SELECT 'erp_customer' as table_name, COUNT(*) as count FROM erp_customer
UNION ALL
SELECT 'erp_contact', COUNT(*) FROM erp_contact
UNION ALL
SELECT 'erp_material', COUNT(*) FROM erp_material
UNION ALL
SELECT 'erp_account', COUNT(*) FROM erp_account;

-- 查看客户列表
SELECT customer_id, title, name, city, country FROM erp_customer;

-- 查看账号列表
SELECT id FROM erp_account;
```

## 注意事项

1. **执行顺序很重要**: 必须先执行参考数据，再执行业务数据
2. **使用INSERT IGNORE**: 避免重复执行时出错
3. **外键约束**: 所有外键引用都已正确设置
4. **测试友好**: 数据设计便于API接口测试

## 清理数据（如需重新开始）

如果需要清理所有数据重新开始：

```sql
-- 注意：这将删除所有数据！
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE erp_stock;
TRUNCATE TABLE erp_relation;
TRUNCATE TABLE erp_contact;
TRUNCATE TABLE erp_customer;
TRUNCATE TABLE erp_material;
TRUNCATE TABLE erp_account;
-- 清理所有参考表...
SET FOREIGN_KEY_CHECKS = 1;
```
