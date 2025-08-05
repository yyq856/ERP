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
            try {
                // 插入交货单记录
                int inserted = outboundDeliveryMapper.insertOutboundDeliveryFromSalesOrder(soId);
                if (inserted > 0) {
                    // 查询生成的交货单编号
                    String deliveryId = outboundDeliveryMapper.getDeliveryNumberBySalesOrderId(soId);
                    if (deliveryId != null) {
                        createdIds.add(deliveryId);
                        successCount++;
                    } else {
                        createdIds.add("FAILED:" + soId);
                        messageBuilder.append("Failed to fetch delivery number for SO ").append(soId).append("; ");
                    }
                } else {
                    createdIds.add("FAILED:" + soId);
                    messageBuilder.append("No delivery inserted for SO ").append(soId).append("; ");
                }
            } catch (Exception e) {
                createdIds.add("ERROR:" + soId);
                messageBuilder.append("Exception creating delivery for SO ").append(soId)
                        .append(": ").append(e.getMessage()).append("; ");
            }
        }

        // 拼接最终返回信息
        String finalMessage = "Successfully created " + successCount + " outbound deliveries";
        if (messageBuilder.length() > 0) {
            finalMessage += ". Issues: " + messageBuilder.toString();
        }

        // 构建响应对象
        CreateOutboundDeliveryResponseData data = new CreateOutboundDeliveryResponseData();
        data.setMessage(finalMessage);
        data.setCreatedDeliveries(createdIds);

        return Response.success(data);
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
                continue;
            }

            OutboundDeliveryItemDTO dbItem = outboundDeliveryMapper.findItemByMaterialAndPlant(item.getMaterial(), item.getPlant());
            if (dbItem == null) {
                badIndices.add(i);
                continue;
            }

            // 补全字段
            if (item.getStorageLocation() == null) item.setStorageLocation(dbItem.getStorageLocation());
            if (item.getStorageLocationDescription() == null) item.setStorageLocationDescription(dbItem.getStorageLocationDescription());
            if (item.getStorageBin() == null) item.setStorageBin(dbItem.getStorageBin());
            if (item.getMaterialAvailability() == null) item.setMaterialAvailability(dbItem.getMaterialAvailability());

            completedItems.add(item);
        }

        ValidateItemsResponse.ValidateItemsResult result = new ValidateItemsResponse.ValidateItemsResult();
        result.setAllDataLegal(badIndices.isEmpty() ? 1 : 0);
        result.setBadRecordIndices(badIndices);

        ValidateItemsResponse response = new ValidateItemsResponse();
        response.setResult(result);
        response.setBreakdowns(completedItems);

        return Response.success(response);
    }

    @Override
    public Response<?> postGIsById(PostGIsByIdRequest request) {
        List<String> ids = request.getDeliveryIds();
        List<OutboundDeliverySummaryDTO> summaries = new ArrayList<>();

        for (String id : ids) {
            outboundDeliveryMapper.updateGIStatusToPosted(id);
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
            String id = detail.getMeta().getId();

            // 1. 校验合法性（可复用 validate 方法）
            // 2. 更新主表信息
            outboundDeliveryMapper.updateDeliveryDetailForPostGI(detail);

            // 3. 更新 item 状态
            for (OutboundDeliveryItemDTO item : req.getItems()) {
                outboundDeliveryMapper.updateItemPostStatus(id, item.getItem());
            }

            // 4. 设置返回 breakdown
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
