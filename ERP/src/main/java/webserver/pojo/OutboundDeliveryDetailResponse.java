package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 出库交货单详情响应DTO
 * 对应 /api/app/outbound-delivery/get-detail 接口的响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboundDeliveryDetailResponse {

    /**
     * 交货单详情信息
     */
    private OutboundDeliveryDetail detail;

    /**
     * 交货单物品信息
     */
    private OutboundDeliveryItems items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundDeliveryDetail {

        /**
         * 元数据信息
         */
        private Meta meta;

        // 日期字段
        private String actualGIDate;           // 实际发货日期
        private String plannedGIDate;          // 计划发货日期
        private String actualDate;             // 实际日期
        private String loadingDate;            // 装载日期
        private String deliveryDate;           // 交货日期

        // 状态字段
        private String pickingStatus;          // 拣配状态
        private String overallStatus;          // 整体状态
        private String giStatus;               // 发货状态

        // 客户和地址信息
        private String shipToParty;            // 送达方
        private String address;                // 地址

        // 重量体积信息
        private String grossWeight;            // 总毛重
        private String grossWeightUnit;        // 毛重单位
        private String netWeight;              // 总净重
        private String netWeightUnit;          // 净重单位
        private String volume;                 // 总体积
        private String volumeUnit;             // 体积单位

        // 其他信息
        private String priority;               // 优先级
        private String shippingPoint;          // 装运点

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Meta {
            private String id;                 // 交货单ID
            private Boolean posted;            // 是否已过账
            private Boolean readyToPost;       // 是否准备好过账
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundDeliveryItems {

        /**
         * 物品列表
         */
        private List<OutboundDeliveryItemDTO> items;
    }
}