package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 出库交货单详情原始DTO
 * 用于Mapper查询返回的原始数据，需要进一步转换为前端格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundDeliveryDetailRawDTO {

    // 基础信息
    private Long id;                           // 交货单ID
    private Boolean posted;                    // 是否已过账
    private Boolean readyToPost;               // 是否准备好过账

    // 日期字段
    private Date actualGIDate;                 // 实际发货日期
    private Date plannedGIDate;                // 计划发货日期
    private Date actualDate;                   // 实际日期
    private Date loadingDate;                  // 装载日期
    private Date deliveryDate;                 // 交货日期

    // 状态字段
    private String pickingStatus;              // 拣配状态
    private String overallStatus;              // 整体状态
    private String giStatus;                   // 发货状态

    // 销售订单信息
    private Long soId;                         // 销售订单ID

    // 客户信息
    private Long shipToParty;                  // 送达方客户ID
    private String shipToPartyName;            // 送达方客户名称
    private String shippingPoint;              // 装运点
    private String address;                    // 地址

    // 重量体积信息
    private BigDecimal grossWeight;            // 总毛重
    private String grossWeightUnit;            // 毛重单位
    private BigDecimal netWeight;              // 总净重
    private String netWeightUnit;              // 净重单位
    private BigDecimal volume;                 // 总体积
    private String volumeUnit;                 // 体积单位

    // 其他信息
    private String priority;                   // 优先级
}
