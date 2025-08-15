package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销售订单物品DTO
 * 用于从erp_item表查询销售订单的物品信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesItemDTO {
    private Integer itemNo;           // 行项目号
    private Long matId;               // 物料ID
    private String pickQuantity;      // 拣配数量（字符串形式，来自order_quantity_str）
    private Long plantId;             // 工厂ID
    private String storageUnit;       // 存储单位（来自order_quantity_unit）
}
