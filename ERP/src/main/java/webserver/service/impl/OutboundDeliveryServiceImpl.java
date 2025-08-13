package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.common.Response;
import webserver.pojo.*;
import webserver.service.OutboundDeliveryService;
import webserver.mapper.OutboundDeliveryMapper;
import webserver.mapper.StockMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OutboundDeliveryServiceImpl implements OutboundDeliveryService {

    private final OutboundDeliveryMapper outboundDeliveryMapper;

    @Autowired
    private StockMapper stockMapper;

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
                    // 预留：若要在创建交货时“承诺库存”，可在此调用 reserveStock（当前先不启用）
                    // Long bpId = outboundDeliveryMapper.getShipToByDeliveryId(dlvId);
                    // stockMapper.reserveStock(item.getPlantId(), item.getMatId(), bpId, item.getStorageLoc(), item.getPickQuantity());
                }
            }

            String deliveryNumber = "" + dlvId;
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
        // Treat null or empty overallStatus as 'select all' (no filter)
        String status = request.getOverallStatus();
        if (status != null) {
            status = status.trim();
            if (status.isEmpty()) {
                status = null;
            }
        }
        List<OutboundDeliverySummaryDTO> deliveries = outboundDeliveryMapper.getDeliverySummaries(status);

        Map<String, Object> result = new HashMap<>();
        result.put("deliveries", deliveries);
        return Response.success(result);
    }

    @Override
    public Response<OutboundDeliveryDetailResponse> getOutboundDeliveryDetail(String deliveryId) {
        // 查询原始数据
        OutboundDeliveryDetailRawDTO raw = outboundDeliveryMapper.getOutboundDeliveryDetail(deliveryId);
        if (raw == null) {
            return Response.error("未找到交货单: " + deliveryId);
        }

        // 手动映射到 DTO
        OutboundDeliveryDetailDTO detail = new OutboundDeliveryDetailDTO();

        // Meta
        OutboundDeliveryDetailDTO.Meta meta = new OutboundDeliveryDetailDTO.Meta();
        meta.setId(raw.getId());
        meta.setPosted(false);
        meta.setReadyToPost(true);
        detail.setMeta(meta);

        // 基础字段
        detail.setPlannedGIDate(raw.getPlannedGIDate() != null ? raw.getPlannedGIDate() : "");
        detail.setActualGIDate(raw.getActualGIDate() != null ? raw.getActualGIDate() : "");
        detail.setShipToParty(raw.getShipToParty() != null ? raw.getShipToParty() : "");
        detail.setShippingPoint(raw.getShippingPoint() != null ? raw.getShippingPoint() : "");
        detail.setPickingStatus(raw.getPickingStatus() != null ? raw.getPickingStatus() : "Not Started");
        detail.setGiStatus(raw.getGiStatus() != null ? raw.getGiStatus() : "Not Started");

        // 查询明细
        List<OutboundDeliveryItemDTO> items = outboundDeliveryMapper.getDeliveryItems(deliveryId);

        // 如果明细为空，也保证返回结构
        if (items == null) items = new ArrayList<>();

        // 填充默认值（非空处理和格式化）
        for (OutboundDeliveryItemDTO item : items) {
            if (item.getMaterialDescription() == null) item.setMaterialDescription("");
            if (item.getDeliveryQuantity() == null) item.setDeliveryQuantity("0");
            if (item.getDeliveryQuantityUnit() == null) item.setDeliveryQuantityUnit("EA");
            if (item.getPickingQuantity() == null) item.setPickingQuantity("0");
            if (item.getPickingQuantityUnit() == null) item.setPickingQuantityUnit("EA");
            if (item.getPickingStatus() == null) item.setPickingStatus("Not Started");
            if (item.getConfirmationStatus() == null) item.setConfirmationStatus("Not Confirmed");
            if (item.getSalesOrder() == null) item.setSalesOrder("");
            if (item.getItemType() == null) item.setItemType("TAN");
            if (item.getOriginalDeliveryQuantity() == null) item.setOriginalDeliveryQuantity("0 EA");
            if (item.getConversionRate() == null) item.setConversionRate("1.000");
            if (item.getBaseUnitDeliveryQuantity() == null) item.setBaseUnitDeliveryQuantity("0 EA");
            if (item.getGrossWeight() == null) item.setGrossWeight("0.0 KG");
            if (item.getNetWeight() == null) item.setNetWeight("0.0 KG");
            if (item.getVolume() == null) item.setVolume("0.0 M3");
            if (item.getPlant() == null) item.setPlant("1000");
            if (item.getStorageLocation() == null) item.setStorageLocation("0001");
            if (item.getStorageLocationDescription() == null) item.setStorageLocationDescription("");
            if (item.getStorageBin() == null) item.setStorageBin("");
            if (item.getMaterialAvailability() == null) item.setMaterialAvailability("");
        }

        detail.setItems(new OutboundDeliveryDetailDTO.ItemsWrapper());
        detail.getItems().setItems(items);

        // 构造 Response
        OutboundDeliveryDetailResponse response = new OutboundDeliveryDetailResponse();
        response.setDetail(detail);

        OutboundDeliveryDetailDTO.ItemsWrapper itemsWrapper = new OutboundDeliveryDetailDTO.ItemsWrapper();
        itemsWrapper.setItems(items);
        response.setItems(itemsWrapper);

        return Response.success(response);
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

            // 库存扣减与释放承诺
            OutboundDeliveryDetailRawDTO raw = outboundDeliveryMapper.getOutboundDeliveryDetail(id);
            if (raw == null) {
                return Response.error("未找到交货单: " + id);
            }

            // 手动映射到 DTO
            OutboundDeliveryDetailDTO detail = new OutboundDeliveryDetailDTO();

            // Meta
            OutboundDeliveryDetailDTO.Meta meta = new OutboundDeliveryDetailDTO.Meta();
            meta.setId(raw.getId());
            meta.setPosted(false);
            meta.setReadyToPost(true);
            detail.setMeta(meta);

            // 基础字段
            detail.setPlannedGIDate(raw.getPlannedGIDate() != null ? raw.getPlannedGIDate() : "");
            detail.setActualGIDate(raw.getActualGIDate() != null ? raw.getActualGIDate() : "");
            detail.setShipToParty(raw.getShipToParty() != null ? raw.getShipToParty() : "");
            detail.setShippingPoint(raw.getShippingPoint() != null ? raw.getShippingPoint() : "");
            detail.setPickingStatus(raw.getPickingStatus() != null ? raw.getPickingStatus() : "Not Started");
            detail.setGiStatus(raw.getGiStatus() != null ? raw.getGiStatus() : "Not Started");

            Long bpId = null;
            try { bpId = Long.valueOf(detail.getShipToParty()); } catch (Exception ignored) {}

            List<OutboundDeliveryItemDTO> giItems = outboundDeliveryMapper.getDeliveryItems(id);

            if (giItems != null) {
                for (OutboundDeliveryItemDTO it : giItems) {
                    Long plantId = parseLongSafe(it.getPlant());
                    Long matId = parseLongSafe(it.getMaterial());
                    String storageLoc = it.getStorageLocation();
                    int qty = Math.round(Float.parseFloat(it.getPickingQuantity()));
                    if (plantId != null && matId != null && qty > 0) {
                        stockMapper.issueAndRelease(plantId, matId, bpId, storageLoc, qty);
                    }
                }
            }

            // 查询更新后的汇总信息
            OutboundDeliverySummaryDTO summary = outboundDeliveryMapper.getDeliverySummary(id);

            // 填充默认值，保证前端字段完整
            if (summary == null) summary = new OutboundDeliverySummaryDTO();
            if (summary.getOutboundDelivery() == null) summary.setOutboundDelivery(id);
            if (summary.getPickingDate() == null) summary.setPickingDate("");
            if (summary.getPickingStatus() == null) summary.setPickingStatus("Not Started");
            if (summary.getGiStatus() == null) summary.setGiStatus("Not Started");

            summaries.add(summary);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("breakdowns", summaries);
        return Response.success(data);
    }

    @Override
    public Response<?> postGIs(List<PostGIsRequest> requests) {
        List<OutboundDeliveryDetailResponse> breakdowns = new ArrayList<>();

        for (PostGIsRequest req : requests) {
            OutboundDeliveryDetailDTO detail = req.getDeliveryDetail();
            String id = detail.getMeta().getId();

            // 更新主表信息
            outboundDeliveryMapper.updateDeliveryDetailForPostGI(detail.getMeta().getId());

            // 更新 item 状态
            if (req.getItems() != null) {
                for (OutboundDeliveryItemDTO item : req.getItems()) {
                    outboundDeliveryMapper.updateItemPostStatus(id, item.getItem());
                }
            }

            // 查询最新 detail 和 items
            OutboundDeliveryDetailRawDTO raw = outboundDeliveryMapper.getOutboundDeliveryDetail(id);
            if (raw == null) {
                return Response.error("未找到交货单: " + id);
            }

            // 手动映射到 DTO
            OutboundDeliveryDetailDTO updatedDetail = new OutboundDeliveryDetailDTO();

            // Meta
            OutboundDeliveryDetailDTO.Meta meta = new OutboundDeliveryDetailDTO.Meta();
            meta.setId(raw.getId());
            meta.setPosted(false);
            meta.setReadyToPost(true);
            updatedDetail.setMeta(meta);

            updatedDetail.setPlannedGIDate(raw.getPlannedGIDate() != null ? raw.getPlannedGIDate() : "");
            updatedDetail.setActualGIDate(raw.getActualGIDate() != null ? raw.getActualGIDate() : "");
            updatedDetail.setShipToParty(raw.getShipToParty() != null ? raw.getShipToParty() : "");
            updatedDetail.setShippingPoint(raw.getShippingPoint() != null ? raw.getShippingPoint() : "");
            updatedDetail.setPickingStatus(raw.getPickingStatus() != null ? raw.getPickingStatus() : "Not Started");
            updatedDetail.setGiStatus(raw.getGiStatus() != null ? raw.getGiStatus() : "Not Started");
            List<OutboundDeliveryItemDTO> updatedItems = outboundDeliveryMapper.getDeliveryItems(id);
            if (updatedItems == null) updatedItems = new ArrayList<>();

            // 填充默认值 - detail
            if (updatedDetail.getMeta() == null) {
                OutboundDeliveryDetailDTO.Meta meta1 = new OutboundDeliveryDetailDTO.Meta();
                meta1.setId(id);
                meta1.setPosted(true);
                meta1.setReadyToPost(true);
                updatedDetail.setMeta(meta1);
            } else {
                updatedDetail.getMeta().setPosted(true);
            }

            if (updatedDetail.getPickingStatus() == null) updatedDetail.setPickingStatus("Completed");
            if (updatedDetail.getOverallStatus() == null) updatedDetail.setOverallStatus("Completed");
            if (updatedDetail.getGiStatus() == null) updatedDetail.setGiStatus("Posted");

            // 填充默认值 - items
            for (OutboundDeliveryItemDTO item : updatedItems) {
                if (item.getPickingStatus() == null) item.setPickingStatus("Completed");
                if (item.getConfirmationStatus() == null) item.setConfirmationStatus("Posted");
                if (item.getMaterialDescription() == null) item.setMaterialDescription("");
                if (item.getDeliveryQuantity() == null) item.setDeliveryQuantity("0");
                if (item.getDeliveryQuantityUnit() == null) item.setDeliveryQuantityUnit("EA");
                if (item.getPickingQuantity() == null) item.setPickingQuantity("0");
                if (item.getPickingQuantityUnit() == null) item.setPickingQuantityUnit("EA");
                if (item.getSalesOrder() == null) item.setSalesOrder("");
                if (item.getItemType() == null) item.setItemType("TAN");
                if (item.getOriginalDeliveryQuantity() == null) item.setOriginalDeliveryQuantity("0 EA");
                if (item.getConversionRate() == null) item.setConversionRate("1.000");
                if (item.getBaseUnitDeliveryQuantity() == null) item.setBaseUnitDeliveryQuantity("0 EA");
                if (item.getGrossWeight() == null) item.setGrossWeight("0.0 KG");
                if (item.getNetWeight() == null) item.setNetWeight("0.0 KG");
                if (item.getVolume() == null) item.setVolume("0.0 M3");
                if (item.getPlant() == null) item.setPlant("1000");
                if (item.getStorageLocation() == null) item.setStorageLocation("0001");
                if (item.getStorageLocationDescription() == null) item.setStorageLocationDescription("");
                if (item.getStorageBin() == null) item.setStorageBin("");
                if (item.getMaterialAvailability() == null) item.setMaterialAvailability("");
            }

            // 构造 breakdown
            OutboundDeliveryDetailResponse response = new OutboundDeliveryDetailResponse();
            response.setDetail(updatedDetail);

            OutboundDeliveryDetailDTO.ItemsWrapper itemsWrapper = new OutboundDeliveryDetailDTO.ItemsWrapper();
            itemsWrapper.setItems(updatedItems);
            response.setItems(itemsWrapper);

            breakdowns.add(response);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("breakdowns", breakdowns);
        return Response.success(result);
    }


    // 安全解析 Long
    private Long parseLongSafe(String v){
        try { return v==null? null: Long.valueOf(v); } catch (Exception e){ return null; }
    }
}
