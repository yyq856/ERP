package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 过账物品请求DTO
 * 对应 /api/app/outbound-delivery/post-gis 接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostGIsRequest {

    /**
     * 交货单详情（可选）
     */
    private OutboundDeliveryDetailResponse.OutboundDeliveryDetail deliveryDetail;

    /**
     * 需要过账的物品列表
     */
    private List<OutboundDeliveryItemDTO> items;
}
