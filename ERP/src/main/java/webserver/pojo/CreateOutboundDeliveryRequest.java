package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建出库交货单请求DTO
 * 对应 /api/app/outbound-delivery/create-from-orders 接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutboundDeliveryRequest {

    /**
     * 销售订单ID数组
     * 前端传入需要创建出库交货单的销售订单ID列表
     */
    private List<String> salesOrderIds;

    /**
     * 兼容旧格式的选中订单列表
     */
    private List<SalesOrderIdDTO> selectedOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesOrderIdDTO {
        private String id;
    }
}
