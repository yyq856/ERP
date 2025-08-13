package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.OutboundDeliveryDetailDTO;
import webserver.pojo.OutboundDeliveryItemDTO;
import webserver.pojo.OutboundDeliverySummaryDTO;
import webserver.pojo.SalesItemDTO;

import java.util.List;

@Mapper
public interface OutboundDeliveryMapper {
    // 插入出库交货单，并返回生成的dlv_id
    void insertOutboundDeliveryFromSalesOrder(@Param("soId") String soId);

    // 获取刚插入的出库交货单的自增id
    Long getLastInsertedDeliveryId();

    // 获取销售订单明细
    List<SalesItemDTO> getSalesItemsBySalesOrderId(@Param("soId") String soId);

    // 批量插入出库交货单明细
    int insertOutboundDeliveryItem(@Param("dlvId") Long dlvId, @Param("item") SalesItemDTO item);

    List<OutboundDeliverySummaryDTO> getDeliverySummaries(@Param("overallStatus") String overallStatus);

    OutboundDeliveryDetailDTO getOutboundDeliveryDetail(@Param("deliveryId") String deliveryId);
    List<OutboundDeliveryItemDTO> getDeliveryItems(@Param("deliveryId") String deliveryId);

    OutboundDeliveryItemDTO findItemByMaterialAndPlant(@Param("material") String material, @Param("plant") String plant);

    int updateGIStatusToPosted(@Param("deliveryId") String deliveryId);

    OutboundDeliverySummaryDTO getDeliverySummary(@Param("deliveryId") String deliveryId);

    void updateDeliveryDetailForPostGI(OutboundDeliveryDetailDTO detail);

    void updateItemsConfirmStatusToPosted(@Param("id") String deliveryId);

    void updateItemPostStatus(@Param("deliveryId") String deliveryId, @Param("item") String itemNo);

    // 新增：根据交货单ID获取 ship_to_party（bp_id）
    Long getShipToByDeliveryId(@Param("dlvId") Long dlvId);
}
