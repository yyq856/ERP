# Quotation统一Item表修复文档

## 问题描述

`create-quotation-from-inquiry`接口在查询inquiry的items时使用的是旧的`erp_inquiry_item`表，导致查询失败。错误日志显示：

```
==>  Preparing: SELECT inquiry_id AS inquiryId, item_no AS itemNo, mat_id AS matId, quantity, net_price AS netPrice, item_value AS itemValue, plant_id AS plantId, su FROM erp_inquiry_item WHERE inquiry_id = ?
==> Parameters: 46(String)
<==      Total: 0
```

## 修复方案

### 1. 修改QuotationMapper.xml

#### 修改前（使用旧表）：
```xml
<select id="selectInquiryItemsById" parameterType="string" resultType="webserver.pojo.InquiryItemDTO">
    SELECT
        inquiry_id AS inquiryId,
        item_no AS itemNo,
        mat_id AS matId,
        quantity,
        net_price AS netPrice,
        item_value AS itemValue,
        plant_id AS plantId,
        su
    FROM erp_inquiry_item
    WHERE inquiry_id = #{inquiryId}
</select>
```

#### 修改后（使用统一表）：
```xml
<select id="selectInquiryItemsById" parameterType="string" resultType="webserver.pojo.InquiryItemDTO">
    SELECT
        document_id AS inquiryId,
        item_no AS itemNo,
        mat_id AS matId,
        quantity,
        net_price AS netPrice,
        item_value AS itemValue,
        plant_id AS plantId,
        su,
        item_code AS itemCode,
        material_code AS materialCode,
        order_quantity_str AS orderQuantityStr,
        order_quantity_unit AS orderQuantityUnit,
        description,
        req_deliv_date AS reqDelivDate,
        net_value_str AS netValueStr,
        net_value_unit AS netValueUnit,
        tax_value_str AS taxValueStr,
        tax_value_unit AS taxValueUnit,
        pricing_date AS pricingDate,
        order_probability AS orderProbability,
        pricing_elements_json AS pricingElementsJson
    FROM erp_item
    WHERE document_id = #{inquiryId} AND document_type = 'inquiry'
    ORDER BY item_no
</select>
```

### 2. 修改插入报价单项目的SQL

#### 修改前（插入到旧表）：
```xml
<insert id="insertQuotationItemsFromInquiry">
    <foreach collection="items" item="item" separator=",">
        INSERT INTO erp_quotation_item (
        quotation_id, item_no, mat_id, quantity, net_price,
        item_discount_pct, item_value, plant_id, su, cnty, tax_value
        ) VALUES (
        #{quotationId}, #{item.itemNo}, #{item.matId}, #{item.quantity}, #{item.netPrice},
        0, #{item.itemValue}, #{item.plantId}, #{item.su}, 0, 0
        )
    </foreach>
</insert>
```

#### 修改后（插入到统一表）：
```xml
<insert id="insertQuotationItemsFromInquiry">
    INSERT INTO erp_item (
        document_id, document_type, item_no, mat_id, quantity, net_price,
        item_value, plant_id, su, item_code, material_code, order_quantity_str,
        order_quantity_unit, description, req_deliv_date, net_value_str,
        net_value_unit, tax_value_str, tax_value_unit, pricing_date,
        order_probability, pricing_elements_json
    ) VALUES
    <foreach collection="items" item="item" separator=",">
        (
            #{quotationId}, 'quotation', #{item.itemNo}, #{item.matId}, #{item.quantity}, #{item.netPrice},
            #{item.itemValue}, #{item.plantId}, #{item.su}, #{item.itemCode}, #{item.materialCode}, 
            #{item.orderQuantityStr}, #{item.orderQuantityUnit}, #{item.description}, #{item.reqDelivDate},
            #{item.netValueStr}, #{item.netValueUnit}, #{item.taxValueStr}, #{item.taxValueUnit},
            #{item.pricingDate}, #{item.orderProbability}, #{item.pricingElementsJson}
        )
    </foreach>
</insert>
```

### 3. 扩展InquiryItemDTO

为了支持新的字段，扩展了`InquiryItemDTO`类，添加了完整的ItemValidation字段：

```java
@Data
public class InquiryItemDTO {
    // 基础字段
    private Long inquiryId;
    private Integer itemNo;
    private Long matId;
    private Integer quantity;
    private Float netPrice;
    private Float itemValue;
    private Long plantId;
    private String su;
    
    // 扩展字段 - 支持完整的ItemValidation结构
    private String itemCode;          // 项目代码
    private String materialCode;      // 物料代码
    private String orderQuantityStr;  // 订单数量字符串
    private String orderQuantityUnit; // 订单数量单位
    private String description;       // 物料描述
    private String reqDelivDate;      // 要求交货日期
    private String netValueStr;       // 净值字符串
    private String netValueUnit;      // 净值单位
    private String taxValueStr;       // 税值字符串
    private String taxValueUnit;      // 税值单位
    private String pricingDate;       // 定价日期
    private String orderProbability;  // 订单概率
    private String pricingElementsJson; // 定价元素JSON数据
}
```

## 修复效果

修复后，`create-quotation-from-inquiry`接口将能够：

1. **正确查询inquiry的items**：从统一的`erp_item`表中查询，使用`document_type='inquiry'`过滤
2. **正确插入quotation的items**：插入到统一的`erp_item`表中，设置`document_type='quotation'`
3. **支持完整的ItemValidation字段**：包括定价元素、税值等复杂信息

## 测试步骤

1. 确保有一个包含items的inquiry记录在`erp_item`表中
2. 调用`/api/quotation/create-quotation-from-inquiry`接口
3. 验证能够成功创建quotation并复制所有item信息
4. 检查新创建的quotation items在`erp_item`表中的`document_type`为'quotation'

## 注意事项

- 确保所有inquiry的items都已经迁移到`erp_item`表中
- 新创建的quotation items将自动设置`document_type='quotation'`
- 保持了向后兼容性，现有的API调用不需要修改
