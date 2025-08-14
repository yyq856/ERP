# 统一项目表设计文档

## 概述

本文档描述了ERP系统中统一项目表(`erp_item`)的设计和实现，该表通过`document_type`字段统一管理所有业务类型的项目数据。

## 设计目标

1. **统一数据结构**: 所有业务类型的项目都使用相同的字段结构
2. **支持ItemValidation**: 完整支持物品验证的所有字段
3. **业务隔离**: 通过`document_type`字段区分不同业务类型
4. **数据一致性**: 统一的数据操作逻辑和验证规则

## 表结构设计

### erp_item表结构

```sql
CREATE TABLE `erp_item` (
  `document_id` bigint NOT NULL COMMENT '文档ID (inquiry_id/quotation_id/so_id/dlv_id/bill_id)',
  `document_type` varchar(20) NOT NULL COMMENT '文档类型 (inquiry/quotation/sales/outbound/billdoc)',
  `item_no` smallint NOT NULL COMMENT '项目号',
  `mat_id` bigint NOT NULL COMMENT '物料ID',
  `quantity` smallint NOT NULL COMMENT '数量',
  `net_price` float NOT NULL COMMENT '净价',
  `item_value` float NOT NULL COMMENT '项目总值',
  `plant_id` bigint NOT NULL COMMENT '工厂ID',
  `su` varchar(10) NOT NULL COMMENT '单位',
  `item_code` varchar(50) DEFAULT NULL COMMENT '项目代码',
  `material_code` varchar(100) DEFAULT NULL COMMENT '物料代码',
  `order_quantity_str` varchar(20) DEFAULT NULL COMMENT '订单数量字符串',
  `order_quantity_unit` varchar(10) DEFAULT NULL COMMENT '订单数量单位',
  `description` varchar(500) DEFAULT NULL COMMENT '物料描述',
  `req_deliv_date` varchar(20) DEFAULT NULL COMMENT '要求交货日期',
  `net_value_str` varchar(20) DEFAULT NULL COMMENT '净值字符串',
  `net_value_unit` varchar(10) DEFAULT NULL COMMENT '净值单位',
  `tax_value_str` varchar(20) DEFAULT NULL COMMENT '税值字符串',
  `tax_value_unit` varchar(10) DEFAULT NULL COMMENT '税值单位',
  `pricing_date` varchar(20) DEFAULT NULL COMMENT '定价日期',
  `order_probability` varchar(10) DEFAULT NULL COMMENT '订单概率',
  `pricing_elements_json` text DEFAULT NULL COMMENT '定价元素JSON数据',
  `created_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`document_id`,`document_type`,`item_no`),
  KEY `mat_id` (`mat_id`),
  KEY `plant_id` (`plant_id`),
  KEY `idx_item_material_code` (`material_code`),
  KEY `idx_item_item_code` (`item_code`),
  KEY `idx_item_document_type` (`document_type`),
  KEY `idx_item_document_id_type` (`document_id`, `document_type`),
  CONSTRAINT `erp_item_ibfk_1` FOREIGN KEY (`mat_id`) REFERENCES `erp_material` (`mat_id`),
  CONSTRAINT `erp_item_ibfk_2` FOREIGN KEY (`plant_id`) REFERENCES `erp_plant_name` (`plant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

### 主键设计

- **复合主键**: (`document_id`, `document_type`, `item_no`)
- **业务隔离**: 通过`document_type`确保不同业务类型的数据隔离
- **无外键约束**: `document_id`不设置外键约束，提高灵活性

## 文档类型定义

| document_type | 说明 | 对应原表 |
|---------------|------|----------|
| inquiry | 询价单 | erp_inquiry_item |
| quotation | 报价单 | erp_quotation_item |
| sales | 销售订单 | erp_sales_item |
| outbound | 出库交货单 | erp_outbound_delivery_item |
| billdoc | 账单凭证 | erp_billing_item |

## 代码实现

### 1. 统一实体类 (Item.java)

```java
@Data
public class Item {
    private Long documentId;          // 文档ID
    private String documentType;      // 文档类型
    private Integer itemNo;           // 项目号
    // ... 其他字段
    
    public static class DocumentType {
        public static final String INQUIRY = "inquiry";
        public static final String QUOTATION = "quotation";
        public static final String SALES = "sales";
        public static final String OUTBOUND = "outbound";
        public static final String BILLDOC = "billdoc";
    }
}
```

### 2. 统一Mapper接口 (ItemMapper.java)

```java
@Mapper
public interface ItemMapper {
    // 根据文档ID和类型查询项目
    List<Item> findItemsByDocumentIdAndType(@Param("documentId") Long documentId, 
                                           @Param("documentType") String documentType);
    
    // 插入项目
    int insertItem(Item item);
    
    // 更新项目
    int updateItem(Item item);
    
    // 删除项目
    int deleteItemsByDocumentIdAndType(@Param("documentId") Long documentId, 
                                      @Param("documentType") String documentType);
}
```

### 3. 业务适配器

每个业务模块保持原有的实体类和接口不变，通过适配器方法进行转换：

```java
// InquiryServiceImpl中的适配器方法
private Item convertInquiryItemToItem(InquiryItem inquiryItem) {
    Item item = new Item();
    item.setDocumentId(inquiryItem.getInquiryId());
    item.setDocumentType(Item.DocumentType.INQUIRY);
    // ... 其他字段映射
    return item;
}
```

## 数据操作示例

### 查询询价单项目
```sql
SELECT * FROM erp_item 
WHERE document_id = 123 AND document_type = 'inquiry' 
ORDER BY item_no;
```

### 插入报价单项目
```sql
INSERT INTO erp_item (document_id, document_type, item_no, mat_id, ...)
VALUES (456, 'quotation', 1, 1001, ...);
```

### 删除销售订单项目
```sql
DELETE FROM erp_item 
WHERE document_id = 789 AND document_type = 'sales';
```

## 迁移策略

1. **创建新表**: 创建`erp_item`表
2. **数据迁移**: 将现有各业务表的数据迁移到新表
3. **代码适配**: 修改各业务模块的Mapper使用新表
4. **测试验证**: 确保所有功能正常工作
5. **清理旧表**: 删除原有的item表

## 优势

1. **统一管理**: 所有项目数据在一个表中，便于维护
2. **扩展性强**: 新增业务类型只需添加新的document_type
3. **数据一致性**: 统一的字段结构和验证逻辑
4. **性能优化**: 统一的索引策略和查询优化

## 注意事项

1. **数据隔离**: 必须在所有查询中包含document_type条件
2. **索引优化**: 合理使用复合索引提高查询性能
3. **事务管理**: 确保跨业务类型的操作在同一事务中
4. **数据完整性**: 虽然没有外键约束，但需要在应用层保证数据一致性
