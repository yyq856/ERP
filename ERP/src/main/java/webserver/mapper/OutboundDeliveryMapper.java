package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.*;

import java.util.List;

@Mapper
public interface OutboundDeliveryMapper {
    // 插入出库交货单，并返回生成的dlv_id
    void insertOutboundDeliveryFromSalesOrder(@Param("soId") String soId);

    // 获取刚插入的出库交货单的自增id
    Long getLastInsertedDeliveryId();

    // 获取销售订单明细
    List<SalesItemDTO> getSalesItemsBySalesOrderId(@Param("soId") String soId);

    // 批量插入出库物品明细 - 基于销售订单的erp_item记录
    int insertOutboundItem(@Param("dlvId") Long dlvId, @Param("soId") String soId);

    List<OutboundDeliverySummaryDTO> getDeliverySummaries(@Param("overallStatus") String overallStatus);

    // 获取单个交货单摘要信息
    OutboundDeliverySummaryDTO getDeliverySummaryById(@Param("deliveryId") String deliveryId);

    OutboundDeliveryDetailRawDTO getOutboundDeliveryDetail(@Param("deliveryId") String deliveryId);
    List<OutboundDeliveryItemDTO> getDeliveryItems(@Param("deliveryId") String deliveryId);

    // 根据物品号获取特定物品
    List<OutboundDeliveryItemDTO> getDeliveryItemsByItemNo(@Param("deliveryId") String deliveryId, @Param("itemNo") String itemNo);

    // 批量更新出库物品 - 用于items-tab-query接口
    int updateOutboundItems(@Param("list") List<OutboundDeliveryItemDTO> items);

    // 单个更新出库物品 - 用于items-tab-query接口
    int updateOutboundItem(OutboundDeliveryItemDTO item);

    // 更新交货单状态 - 根据物品状态动态计算
    void updateDeliveryStatuses(@Param("deliveryId") String deliveryId);

    // 更新交货单重量体积合计
    void updateDeliveryWeightVolume(@Param("deliveryId") String deliveryId);

    // Post GI - 过账发货
    int postGIByDeliveryId(@Param("deliveryId") String deliveryId);

    // 更新物品确认状态为已过账
    void updateItemsConfirmStatusToPosted(@Param("deliveryId") String deliveryId);

    // 检查交货单是否准备好过账
    Boolean checkReadyToPost(@Param("deliveryId") String deliveryId);

    // 新增：根据交货单ID获取 ship_to_party（bp_id）
    Long getShipToByDeliveryId(@Param("dlvId") Long dlvId);
}
