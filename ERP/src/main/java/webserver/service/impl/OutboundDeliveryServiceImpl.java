package webserver.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.common.Response;
import webserver.pojo.*;
import webserver.service.OutboundDeliveryService;
import webserver.mapper.OutboundDeliveryMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OutboundDeliveryServiceImpl implements OutboundDeliveryService {

    private final OutboundDeliveryMapper outboundDeliveryMapper;

    public OutboundDeliveryServiceImpl(OutboundDeliveryMapper outboundDeliveryMapper) {
        this.outboundDeliveryMapper = outboundDeliveryMapper;
    }

    @Override
    @Transactional
    public Response<CreateOutboundDeliveryResponseData> createFromOrders(CreateOutboundDeliveryRequest request) {
        List<String> createdIds = new ArrayList<>();
        StringBuilder messageBuilder = new StringBuilder();
        int successCount = 0;

        for (CreateOutboundDeliveryRequest.SalesOrderIdDTO so : request.getSelectedOrders()) {
            String soId = so.getId();
            outboundDeliveryMapper.insertOutboundDeliveryFromSalesOrder(soId);

            Long dlvId = outboundDeliveryMapper.getLastInsertedDeliveryId();

            List<SalesItemDTO> items = outboundDeliveryMapper.getSalesItemsBySalesOrderId(soId);
            if (!items.isEmpty()) {
                for (SalesItemDTO item : items) {
                    outboundDeliveryMapper.insertOutboundDeliveryItem(dlvId, item);
                }
            }

            String deliveryNumber = "DEL-" + dlvId;
            createdIds.add(deliveryNumber);
            successCount++;

        }

        CreateOutboundDeliveryResponseData respData = new CreateOutboundDeliveryResponseData();
        respData.setMessage("成功创建 " + successCount + " 条出库交货单");
        respData.setCreatedDeliveries(createdIds);
        return Response.success(respData);
    }


    @Override
    public Response<?> getOutboundDeliverySummaries(GetOutboundDeliverySummaryRequest request) {
        List<OutboundDeliverySummaryDTO> deliveries = outboundDeliveryMapper.getDeliverySummaries(
                request.getOverallStatus());

        Map<String, Object> result = new HashMap<>();
        result.put("deliveries", deliveries);
        return Response.success(result);
    }

    @Override
    public Response<OutboundDeliveryDetailDTO> getOutboundDeliveryDetail(String deliveryId) {
        OutboundDeliveryDetailDTO detail = outboundDeliveryMapper.getOutboundDeliveryDetail(deliveryId);
        if (detail == null) {
            return Response.error("未找到交货单: " + deliveryId);
        }
        List<OutboundDeliveryItemDTO> items = outboundDeliveryMapper.getDeliveryItems(deliveryId);
        detail.setItems(items);
        return Response.success(detail);
    }

    @Override
    public Response<ValidateItemsResponse> validateAndCompleteDeliveryItems(List<OutboundDeliveryItemDTO> items) {
        List<Integer> badIndices = new ArrayList<>();
        List<OutboundDeliveryItemDTO> completedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            OutboundDeliveryItemDTO item = items.get(i);

            if (item.getMaterial() == null || item.getPlant() == null) {
                badIndices.add(i);
                completedItems.add(item);
                continue;
            }

            OutboundDeliveryItemDTO dbItem = outboundDeliveryMapper.findItemByMaterialAndPlant(
                    item.getMaterial(), item.getPlant()
            );

            if (dbItem == null) {
                badIndices.add(i);
                completedItems.add(item);
                continue;
            }

            // 补全缺失字段
            if (item.getMaterialDescription() == null) {
                item.setMaterialDescription(dbItem.getMaterialDescription());
            }
            if (item.getPickingQuantity() == 0) {
                item.setPickingQuantity(dbItem.getPickingQuantity());
            }
            if (item.getPlantName() == null) {
                item.setPlantName(dbItem.getPlantName());
            }
            if (item.getStorageLocation() == null) {
                item.setStorageLocation(dbItem.getStorageLocation());
            }
            if (item.getStorageLocationDescription() == null) {
                item.setStorageLocationDescription(dbItem.getStorageLocationDescription());
            }

            completedItems.add(item);
        }

        ValidateItemsResponse.ValidationResult result = new ValidateItemsResponse.ValidationResult();
        result.setAllDataLegal(badIndices.isEmpty() ? 1 : 0);
        result.setBadRecordIndices(badIndices);

        ValidateItemsResponse.ValidateItemsData responseData = new ValidateItemsResponse.ValidateItemsData();
        responseData.setResult(result);
        responseData.setGeneralData(null); // 可以根据需要设置
        responseData.setBreakdowns(null); // 这里需要转换为适当的类型

        ValidateItemsResponse response = new ValidateItemsResponse();
        response.setSuccess(true);
        response.setMessage("验证完成");
        response.setData(responseData);

        return Response.success(response);
    }

    @Override
    public Response<?> postGIsById(PostGIsByIdRequest request) {
        List<String> ids = request.getDeliveryIds();
        List<OutboundDeliverySummaryDTO> summaries = new ArrayList<>();

        for (String id : ids) {
            // 更新出库交货单状态和过账时间
            outboundDeliveryMapper.updateGIStatusToPosted(id);

            // 更新该交货单所有明细的确认状态
            outboundDeliveryMapper.updateItemsConfirmStatusToPosted(id);

            // 查询更新后的汇总信息
            OutboundDeliverySummaryDTO summary = outboundDeliveryMapper.getDeliverySummary(id);
            summaries.add(summary);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("breakdowns", summaries);
        return Response.success(data);
    }


    @Override
    public Response<?> postGIs(List<PostGIsRequest> requests) {
        List<Map<String, Object>> breakdowns = new ArrayList<>();

        for (PostGIsRequest req : requests) {
            OutboundDeliveryDetailDTO detail = req.getDeliveryDetail();
            String id = detail.getId();

            // 直接更新主表信息
            outboundDeliveryMapper.updateDeliveryDetailForPostGI(detail);

            // 更新 item 状态
            for (OutboundDeliveryItemDTO item : req.getItems()) {
                outboundDeliveryMapper.updateItemPostStatus(id, item.getItem());
            }

            // 设置返回 breakdown
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("detail", outboundDeliveryMapper.getOutboundDeliveryDetail(id));
            breakdown.put("items", outboundDeliveryMapper.getDeliveryItems(id));
            breakdowns.add(breakdown);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("breakdowns", breakdowns);
        return Response.success(result);
    }

}
