package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建出库交货单响应数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutboundDeliveryResponseData {

    /**
     * 响应消息
     */
    private String message;

    /**
     * 创建成功的交货单ID列表
     */
    private List<String> createdDeliveries;

    /**
     * 创建成功的数量
     */
    private Integer successCount;

    /**
     * 失败的销售订单ID列表
     */
    private List<String> failedSalesOrderIds;

    /**
     * 失败原因列表
     */
    private List<String> failureReasons;
}
