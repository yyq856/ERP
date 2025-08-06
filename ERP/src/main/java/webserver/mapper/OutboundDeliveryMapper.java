package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.OutboundDeliveryDetailDTO;
import webserver.pojo.OutboundDeliveryItemDTO;
import webserver.pojo.OutboundDeliverySummaryDTO;

import java.util.List;

@Mapper
public interface OutboundDeliveryMapper {
    // 1. 插入出库交货单（返回影响的行数）
    int insertOutboundDeliveryFromSalesOrder(@Param("soId") String soId);

    // 2. 根据销售订单 ID 查询刚插入的交货单编号
    String getDeliveryNumberBySalesOrderId(@Param("soId") String soId);

    List<OutboundDeliverySummaryDTO> getDeliverySummaries(@Param("overallStatus") String overallStatus);

    OutboundDeliveryDetailDTO getOutboundDeliveryDetail(@Param("deliveryId") String deliveryId);
    List<OutboundDeliveryItemDTO> getDeliveryItems(@Param("deliveryId") String deliveryId);

    OutboundDeliveryItemDTO findItemByMaterialAndPlant(@Param("material") String material, @Param("plant") String plant);

    int updateGIStatusToPosted(@Param("deliveryId") String deliveryId);

    OutboundDeliverySummaryDTO getDeliverySummary(@Param("deliveryId") String deliveryId);

    void updateDeliveryDetailForPostGI(OutboundDeliveryDetailDTO detail);

    void updateItemPostStatus(@Param("deliveryId") String deliveryId, @Param("item") String itemNo);
}
