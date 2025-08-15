package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 出库交货单物品DTO
 * 对应前端outbound_item的数据结构，包含冗余字段和可编辑字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundDeliveryItemDTO {

    // 基础标识字段
    private Long dlvId;                          // 交货单ID（内部使用）
    private String item;                         // 行项目号（冗余，从引用的erp_item获取）
    private String material;                     // 物料号（冗余，从引用的erp_item获取）

    // 数量相关字段
    private String deliveryQuantity;             // 交货数量（冗余，=引用的erp_item的order_quantity_str）
    private String deliveryQuantityUnit;         // 交货数量单位（冗余，=引用的erp_item的order_quantity_unit）
    private BigDecimal pickingQuantity;          // 拣配数量（要存储，用户可编辑）
    private String pickingQuantityUnit;          // 拣配数量单位（冗余，=引用的erp_item的order_quantity_unit）

    // 状态字段
    private String pickingStatus;                // 拣配状态（要存储，动态计算）
    private String confirmationStatus;           // 确认状态（要存储，用户可编辑）

    // 订单信息
    private String salesOrder;                   // 销售订单号（冗余，从引用的erp_item获取）

    // 物品类型和转换
    private String itemType;                     // 项目类型（要存储，用户可编辑）
    private String originalDelivertyQuantity;    // 原始交货数量（拼接字符串）
    private BigDecimal conversionRate;           // 转换率（要存储，动态计算）
    private String baseUnitDeliveryQuantity;     // 基本单位交货数量（拼接字符串）

    // 重量体积
    private String grossWeight;                  // 毛重（拼接字符串，要存储）
    private String netWeight;                    // 净重（拼接字符串，要存储）
    private String volume;                       // 体积（拼接字符串，要存储）

    // 工厂和库存
    private String plant;                        // 工厂（冗余，从引用的erp_item获取）
    private String storageLocation;              // 库存地点（要存储，用户输入）
    private String storageLocationDescription;   // 库存地点描述（冗余，通过storageLocation查找）
    private String storageBin;                   // 储位（要存储，用户输入）

    // 其他
    private String materialAvailability;         // 物料可用性日期（冗余，从引用的erp_item获取req_deliv_date）

    // 内部字段（用于更新操作）
    private Integer itemNo;                      // 行项目号（数字形式，内部使用）
}
