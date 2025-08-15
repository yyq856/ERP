package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Post GIs 接口响应类
 * 对应 /api/app/outbound-delivery/post-gis 接口的返回数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostGIsResponse {
    
    /**
     * 过账结果
     */
    private PostGIsResult result;
    
    /**
     * 过账后的交货单详情列表
     */
    private List<PostGIsBreakdown> breakdowns;
    
    /**
     * 过账结果信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostGIsResult {
        private Integer allDataLegal;           // 1表示所有数据合法，0表示存在不合法数据
        private List<Integer> badRecordIndices; // 不合法数据的索引列表 (从0开始)
    }
    
    /**
     * 过账后的交货单详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostGIsBreakdown {
        private OutboundDeliveryDetailResponse.OutboundDeliveryDetail detail; // 交货单详情
        private List<OutboundDeliveryItemDTO> items;                          // 物品列表
    }
}
