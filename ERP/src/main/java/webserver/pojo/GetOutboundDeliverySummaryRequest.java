package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取出库交货单汇总请求DTO
 * 对应 /api/app/outbound-delivery/get-deliveries-summary 接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutboundDeliverySummaryRequest {

    /**
     * 整体状态过滤条件
     * 可选值：In Progress, Completed 等
     */
    private String overallStatus;

    /**
     * 创建人过滤条件（可选）
     */
    private String createdBy;

    /**
     * 其他查询条件（可扩展）
     */
    private String additionalFilter;
}
