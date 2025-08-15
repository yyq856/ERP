package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.common.Response;
import webserver.pojo.*;
import webserver.service.OutboundDeliveryService;
import webserver.mapper.OutboundDeliveryMapper;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 出库交货单服务实现类
 */
@Service
public class OutboundDeliveryServiceImpl implements OutboundDeliveryService {

    @Autowired
    private OutboundDeliveryMapper outboundDeliveryMapper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    @Transactional
    public Response<CreateOutboundDeliveryResponseData> createFromOrders(CreateOutboundDeliveryRequest request) {
        try {
            List<String> salesOrderIds = extractSalesOrderIds(request);
            List<String> createdDeliveries = new ArrayList<>();
            List<String> failedSalesOrderIds = new ArrayList<>();
            List<String> failureReasons = new ArrayList<>();

            System.out.println("开始处理销售订单: " + salesOrderIds);

            for (String soId : salesOrderIds) {
                try {
                    System.out.println("处理销售订单: " + soId);

                    // 1. 插入出库交货单主表
                    outboundDeliveryMapper.insertOutboundDeliveryFromSalesOrder(soId);

                    // 2. 获取刚插入的交货单ID
                    Long dlvId = outboundDeliveryMapper.getLastInsertedDeliveryId();
                    System.out.println("创建的交货单ID: " + dlvId);

                    // 3. 批量插入出库物品明细
                    int itemCount = outboundDeliveryMapper.insertOutboundItem(dlvId, soId);
                    System.out.println("插入的物品数量: " + itemCount);

                    if (itemCount > 0) {
                        // 4. 更新交货单重量体积合计
                        outboundDeliveryMapper.updateDeliveryWeightVolume(String.valueOf(dlvId));

                        // 5. 更新交货单状态
                        outboundDeliveryMapper.updateDeliveryStatuses(String.valueOf(dlvId));

                        createdDeliveries.add(String.valueOf(dlvId));
                        System.out.println("成功创建交货单: " + dlvId);
                    } else {
                        failedSalesOrderIds.add(soId);
                        failureReasons.add("销售订单 " + soId + " 在erp_item表中没有找到对应的物品记录");
                        System.out.println("销售订单 " + soId + " 没有找到物品记录");
                    }
                } catch (Exception e) {
                    failedSalesOrderIds.add(soId);
                    failureReasons.add("创建交货单失败: " + e.getMessage());
                    System.err.println("创建交货单失败: " + soId + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            CreateOutboundDeliveryResponseData responseData = new CreateOutboundDeliveryResponseData();
            responseData.setMessage("成功创建 " + createdDeliveries.size() + " 个出库交货单");
            responseData.setCreatedDeliveries(createdDeliveries);
            responseData.setSuccessCount(createdDeliveries.size());
            responseData.setFailedSalesOrderIds(failedSalesOrderIds);
            responseData.setFailureReasons(failureReasons);

            return Response.success(responseData);
        } catch (Exception e) {
            return Response.error("创建出库交货单失败: " + e.getMessage());
        }
    }

    @Override
    public Response<?> getOutboundDeliverySummaries(GetOutboundDeliverySummaryRequest request) {
        try {
            List<OutboundDeliverySummaryDTO> summaries = outboundDeliveryMapper.getDeliverySummaries(request.getOverallStatus());

            // 构建返回数据，包装在deliveries中
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("deliveries", summaries);

            return Response.success(data);
        } catch (Exception e) {
            return Response.error("获取出库交货单汇总失败: " + e.getMessage());
        }
    }

    @Override
    public Response<OutboundDeliveryDetailResponse> getOutboundDeliveryDetail(String deliveryId) {
        try {
            // 1. 获取交货单详情
            OutboundDeliveryDetailRawDTO rawDetail = outboundDeliveryMapper.getOutboundDeliveryDetail(deliveryId);
            if (rawDetail == null) {
                return Response.error("交货单不存在: " + deliveryId);
            }

            // 2. 获取交货单物品列表
            List<OutboundDeliveryItemDTO> items = outboundDeliveryMapper.getDeliveryItems(deliveryId);

            // 3. 转换为前端格式
            OutboundDeliveryDetailResponse response = convertToDetailResponse(rawDetail, items);

            return Response.success(response);
        } catch (Exception e) {
            return Response.error("获取交货单详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response<ValidateItemsResponse> validateAndCompleteDeliveryItems(List<OutboundDeliveryItemDTO> items) {
        try {
            System.out.println("开始验证和完善物品数据，物品数量: " + items.size());

            // 1. 验证物品数据
            List<Integer> badRecordIndices = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                OutboundDeliveryItemDTO item = items.get(i);
                if (!validateItem(item)) {
                    badRecordIndices.add(i);
                    System.out.println("物品验证失败，索引: " + i + ", 物品: " + item.getItem());
                }
            }

            String deliveryId = null;
            ValidateItemsResponse.GeneralData generalData = new ValidateItemsResponse.GeneralData();
            List<OutboundDeliveryItemDTO> completedItems = new ArrayList<>();

            // 2. 更新有效的物品数据到数据库
            if (badRecordIndices.isEmpty() && !items.isEmpty()) {
                deliveryId = String.valueOf(items.get(0).getDlvId());
                System.out.println("更新交货单物品，交货单ID: " + deliveryId);

                // 逐个更新物品数据（保留用户设定的行号等信息）
                for (OutboundDeliveryItemDTO item : items) {
                    System.out.println("更新物品: " + item.getItem() + ", 拣货数量: " + item.getPickingQuantity());

                    // 设置默认值
                    if (item.getItemType() == null) {
                        item.setItemType("Standard");
                    }
                    if (item.getConfirmationStatus() == null) {
                        item.setConfirmationStatus("Not Confirmed");
                    }
                    if (item.getConversionRate() == null) {
                        item.setConversionRate(new BigDecimal("1.000"));
                    }

                    outboundDeliveryMapper.updateOutboundItem(item);
                }

                // 更新交货单状态和重量体积
                outboundDeliveryMapper.updateDeliveryStatuses(deliveryId);
                outboundDeliveryMapper.updateDeliveryWeightVolume(deliveryId);
            }

            // 3. 获取更新后的物品数据和交货单总体信息
            if (deliveryId != null) {
                // 只获取前端传入的物品的更新后数据
                completedItems = new ArrayList<>();
                for (OutboundDeliveryItemDTO item : items) {
                    List<OutboundDeliveryItemDTO> updatedItems = outboundDeliveryMapper.getDeliveryItemsByItemNo(deliveryId, item.getItem());
                    if (!updatedItems.isEmpty()) {
                        completedItems.add(updatedItems.get(0));
                    }
                }
                System.out.println("获取前端传入物品的更新后数据，数量: " + completedItems.size());

                // 获取交货单的总体信息（基于所有物品计算）
                OutboundDeliveryDetailRawDTO deliveryDetail = outboundDeliveryMapper.getOutboundDeliveryDetail(deliveryId);
                if (deliveryDetail != null) {
                    generalData.setPickingStatus(deliveryDetail.getPickingStatus());
                    generalData.setOverallStatus(deliveryDetail.getOverallStatus());
                    generalData.setGiStatus(deliveryDetail.getGiStatus());
                    generalData.setReadyToPost(deliveryDetail.getReadyToPost());
                    generalData.setGrossWeight(deliveryDetail.getGrossWeight() != null ? deliveryDetail.getGrossWeight().toString() : "0.000");
                    generalData.setGrossWeightUnit(deliveryDetail.getGrossWeightUnit());
                    generalData.setNetWeight(deliveryDetail.getNetWeight() != null ? deliveryDetail.getNetWeight().toString() : "0.000");
                    generalData.setNetWeightUnit(deliveryDetail.getNetWeightUnit());
                    generalData.setVolume(deliveryDetail.getVolume() != null ? deliveryDetail.getVolume().toString() : "0.000");
                    generalData.setVolumeUnit(deliveryDetail.getVolumeUnit());
                }
            } else {
                // 如果没有更新数据库，返回原始数据
                completedItems = items;
            }

            // 4. 构造响应
            ValidateItemsResponse.ValidationResult validationResult = new ValidateItemsResponse.ValidationResult();
            validationResult.setAllDataLegal(badRecordIndices.isEmpty() ? 1 : 0);
            validationResult.setBadRecordIndices(badRecordIndices);

            ValidateItemsResponse.ValidateItemsData data = new ValidateItemsResponse.ValidateItemsData();
            data.setResult(validationResult);
            data.setGeneralData(generalData);
            data.setBreakdowns(completedItems);

            ValidateItemsResponse response = new ValidateItemsResponse();
            response.setSuccess(true);
            response.setMessage(badRecordIndices.isEmpty() ? "数据验证通过" : "存在验证失败的记录");
            response.setData(data);

            System.out.println("验证完成，返回物品数量: " + completedItems.size());
            return Response.success(response);
        } catch (Exception e) {
            System.err.println("验证物品失败: " + e.getMessage());
            e.printStackTrace();
            return Response.error("验证物品失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Response<?> postGIsById(PostGIsByIdRequest request) {
        try {
            List<Integer> badRecordIndices = new ArrayList<>();
            List<OutboundDeliverySummaryDTO> breakdowns = new ArrayList<>();

            // 按照请求顺序处理每个交货单
            for (int i = 0; i < request.getDeliveryIds().size(); i++) {
                String deliveryId = request.getDeliveryIds().get(i);
                boolean isSuccess = false;

                try {
                    // 检查是否准备好过账
                    Boolean readyToPost = outboundDeliveryMapper.checkReadyToPost(deliveryId);
                    if (readyToPost != null && readyToPost) {
                        // 执行过账
                        int updated = outboundDeliveryMapper.postGIByDeliveryId(deliveryId);
                        if (updated > 0) {
                            // 更新物品确认状态
                            outboundDeliveryMapper.updateItemsConfirmStatusToPosted(deliveryId);

                            // 过账完成后重新计算状态
                            outboundDeliveryMapper.updateDeliveryStatuses(deliveryId);

                            isSuccess = true;
                        }
                    }
                } catch (Exception e) {
                    // 过账失败，记录到badRecordIndices
                }

                // 无论成功失败，都要添加到breakdowns中，保持顺序一致
                if (!isSuccess) {
                    badRecordIndices.add(i); // 记录失败的索引（从0开始）
                }

                // 获取交货单摘要信息（无论成功失败都要返回当前状态）
                OutboundDeliverySummaryDTO summary = outboundDeliveryMapper.getDeliverySummaryById(deliveryId);
                if (summary != null) {
                    breakdowns.add(summary);
                } else {
                    // 如果获取不到摘要信息，创建一个默认的
                    OutboundDeliverySummaryDTO defaultSummary = new OutboundDeliverySummaryDTO();
                    defaultSummary.setOutboundDelivery(deliveryId);
                    defaultSummary.setPickingDate("");
                    defaultSummary.setPickingStatus("Unknown");
                    defaultSummary.setGiStatus("Unknown");
                    breakdowns.add(defaultSummary);
                }
            }

            // 构建返回数据，使用summary格式
            PostGIsResponse.PostGIsResult result = new PostGIsResponse.PostGIsResult();
            result.setAllDataLegal(badRecordIndices.isEmpty() ? 1 : 0);
            result.setBadRecordIndices(badRecordIndices);

            // 创建专门的响应对象，包含summary级别的breakdowns
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("result", result);
            responseData.put("breakdowns", breakdowns);

            return Response.success(responseData);
        } catch (Exception e) {
            return Response.error("批量过账失败: " + e.getMessage());
        }
    }

    /**
     * 创建PostGIsBreakdown对象的辅助方法
     */
    private PostGIsResponse.PostGIsBreakdown createBreakdown(String deliveryId) {
        try {
            // 获取详细信息
            OutboundDeliveryDetailRawDTO deliveryDetail = outboundDeliveryMapper.getOutboundDeliveryDetail(deliveryId);
            List<OutboundDeliveryItemDTO> items = outboundDeliveryMapper.getDeliveryItems(deliveryId);

            if (deliveryDetail != null) {
                PostGIsResponse.PostGIsBreakdown breakdown = new PostGIsResponse.PostGIsBreakdown();

                // 构建detail
                OutboundDeliveryDetailResponse.OutboundDeliveryDetail detail = new OutboundDeliveryDetailResponse.OutboundDeliveryDetail();
                OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta meta = new OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta();
                meta.setId(deliveryDetail.getId().toString());
                meta.setPosted(deliveryDetail.getPosted());
                meta.setReadyToPost(deliveryDetail.getReadyToPost());
                detail.setMeta(meta);

                // 设置其他字段
                detail.setActualGIDate(deliveryDetail.getActualGIDate() != null ? deliveryDetail.getActualGIDate().toString() : null);
                detail.setPlannedGIDate(deliveryDetail.getPlannedGIDate() != null ? deliveryDetail.getPlannedGIDate().toString() : null);
                detail.setActualDate(deliveryDetail.getActualDate() != null ? deliveryDetail.getActualDate().toString() : null);
                detail.setLoadingDate(deliveryDetail.getLoadingDate() != null ? deliveryDetail.getLoadingDate().toString() : null);
                detail.setDeliveryDate(deliveryDetail.getDeliveryDate() != null ? deliveryDetail.getDeliveryDate().toString() : null);
                detail.setPickingStatus(deliveryDetail.getPickingStatus());
                detail.setOverallStatus(deliveryDetail.getOverallStatus());
                detail.setGiStatus(deliveryDetail.getGiStatus());
                detail.setShipToParty(deliveryDetail.getShipToPartyName());
                detail.setAddress(deliveryDetail.getAddress());
                detail.setGrossWeight(deliveryDetail.getGrossWeight() != null ? deliveryDetail.getGrossWeight().toString() : null);
                detail.setGrossWeightUnit(deliveryDetail.getGrossWeightUnit());
                detail.setNetWeight(deliveryDetail.getNetWeight() != null ? deliveryDetail.getNetWeight().toString() : null);
                detail.setNetWeightUnit(deliveryDetail.getNetWeightUnit());
                detail.setVolume(deliveryDetail.getVolume() != null ? deliveryDetail.getVolume().toString() : null);
                detail.setVolumeUnit(deliveryDetail.getVolumeUnit());
                detail.setPriority(deliveryDetail.getPriority());
                detail.setShippingPoint(deliveryDetail.getShippingPoint());

                breakdown.setDetail(detail);
                breakdown.setItems(items != null ? items : new ArrayList<>());
                return breakdown;
            }
        } catch (Exception e) {
            System.err.println("创建breakdown失败，交货单ID: " + deliveryId + ", 错误: " + e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public Response<?> postGIs(List<PostGIsRequest> requests) {
        try {
            int successCount = 0;
            int failedCount = 0;

            for (PostGIsRequest request : requests) {
                try {
                    System.out.println("处理过账请求: " + request);
                    System.out.println("请求中的物品数据: " + request.getItems());
                    // 先更新物品数据
                    if (request.getItems() != null && !request.getItems().isEmpty()) {
                        System.out.println("开始更新物品数据，物品数量: " + request.getItems().size());
                        for (OutboundDeliveryItemDTO item : request.getItems()) {
                            System.out.println("更新物品: " + item.getItem() + ", 拣货状态: " + item.getPickingStatus());
                            outboundDeliveryMapper.updateOutboundItem(item);
                        }
                        
                        String deliveryId = String.valueOf(request.getItems().get(0).getDlvId());
                        
                        // 更新交货单状态
                        outboundDeliveryMapper.updateDeliveryStatuses(deliveryId);
                        outboundDeliveryMapper.updateDeliveryWeightVolume(deliveryId);
                        
                        // 检查并执行过账
                        Boolean readyToPost = outboundDeliveryMapper.checkReadyToPost(deliveryId);
                        System.out.println("交货单 " + deliveryId + " 的 readyToPost 状态: " + readyToPost);
                        if (readyToPost != null && readyToPost) {
                            System.out.println("开始执行过账操作...");
                            int updated = outboundDeliveryMapper.postGIByDeliveryId(deliveryId);
                            System.out.println("过账操作影响的行数: " + updated);
                            if (updated > 0) {
                                outboundDeliveryMapper.updateItemsConfirmStatusToPosted(deliveryId);

                                // 过账完成后重新计算状态
                                System.out.println("过账完成，重新计算交货单状态...");
                                outboundDeliveryMapper.updateDeliveryStatuses(deliveryId);

                                System.out.println("成功过账交货单: " + deliveryId);
                                successCount++;
                            } else {
                                System.out.println("过账失败，没有更新任何行");
                                failedCount++;
                            }
                        } else {
                            System.out.println("交货单不满足过账条件，readyToPost: " + readyToPost);
                            failedCount++;
                        }
                    }
                } catch (Exception e) {
                    failedCount++;
                }
            }

            // 构建返回数据
            PostGIsResponse response = new PostGIsResponse();
            PostGIsResponse.PostGIsResult result = new PostGIsResponse.PostGIsResult();
            result.setAllDataLegal(successCount > 0 ? 1 : 0);
            result.setBadRecordIndices(new ArrayList<>());
            response.setResult(result);

            // 获取过账后的交货单详情
            List<PostGIsResponse.PostGIsBreakdown> breakdowns = new ArrayList<>();
            for (PostGIsRequest request : requests) {
                if (request.getDeliveryDetail() != null && request.getDeliveryDetail().getMeta() != null) {
                    String deliveryId = request.getDeliveryDetail().getMeta().getId();

                    // 获取更新后的交货单详情
                    OutboundDeliveryDetailRawDTO deliveryDetail = outboundDeliveryMapper.getOutboundDeliveryDetail(deliveryId);
                    List<OutboundDeliveryItemDTO> items = outboundDeliveryMapper.getDeliveryItems(deliveryId);

                    if (deliveryDetail != null) {
                        PostGIsResponse.PostGIsBreakdown breakdown = new PostGIsResponse.PostGIsBreakdown();

                        // 构建detail
                        OutboundDeliveryDetailResponse.OutboundDeliveryDetail detail = new OutboundDeliveryDetailResponse.OutboundDeliveryDetail();
                        OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta meta = new OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta();
                        meta.setId(deliveryDetail.getId().toString());
                        meta.setPosted(deliveryDetail.getPosted());
                        meta.setReadyToPost(deliveryDetail.getReadyToPost());
                        detail.setMeta(meta);

                        // 日期转换为字符串格式
                        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
                        detail.setActualGIDate(deliveryDetail.getActualGIDate() != null ? dateFormat.format(deliveryDetail.getActualGIDate()) : null);
                        detail.setPlannedGIDate(deliveryDetail.getPlannedGIDate() != null ? dateFormat.format(deliveryDetail.getPlannedGIDate()) : null);
                        detail.setActualDate(deliveryDetail.getActualDate() != null ? dateFormat.format(deliveryDetail.getActualDate()) : null);
                        detail.setLoadingDate(deliveryDetail.getLoadingDate() != null ? dateFormat.format(deliveryDetail.getLoadingDate()) : null);
                        detail.setDeliveryDate(deliveryDetail.getDeliveryDate() != null ? dateFormat.format(deliveryDetail.getDeliveryDate()) : null);
                        detail.setPickingStatus(deliveryDetail.getPickingStatus());
                        detail.setOverallStatus(deliveryDetail.getOverallStatus());
                        detail.setGiStatus(deliveryDetail.getGiStatus());
                        detail.setShipToParty(deliveryDetail.getShipToPartyName());
                        detail.setAddress(deliveryDetail.getAddress());
                        detail.setGrossWeight(deliveryDetail.getGrossWeight() != null ? deliveryDetail.getGrossWeight().toString() : "0.000");
                        detail.setGrossWeightUnit(deliveryDetail.getGrossWeightUnit());
                        detail.setNetWeight(deliveryDetail.getNetWeight() != null ? deliveryDetail.getNetWeight().toString() : "0.000");
                        detail.setNetWeightUnit(deliveryDetail.getNetWeightUnit());
                        detail.setVolume(deliveryDetail.getVolume() != null ? deliveryDetail.getVolume().toString() : "0.000");
                        detail.setVolumeUnit(deliveryDetail.getVolumeUnit());
                        detail.setPriority(deliveryDetail.getPriority());
                        detail.setShippingPoint(deliveryDetail.getShippingPoint());

                        breakdown.setDetail(detail);
                        breakdown.setItems(items != null ? items : new ArrayList<>());
                        breakdowns.add(breakdown);
                    }
                }
            }

            response.setBreakdowns(breakdowns);
            return Response.success(response);
        } catch (Exception e) {
            return Response.error("过账失败: " + e.getMessage());
        }
    }

    // 辅助方法：提取销售订单ID列表
    private List<String> extractSalesOrderIds(CreateOutboundDeliveryRequest request) {
        if (request.getSalesOrderIds() != null && !request.getSalesOrderIds().isEmpty()) {
            return request.getSalesOrderIds();
        }
        if (request.getSelectedOrders() != null && !request.getSelectedOrders().isEmpty()) {
            return request.getSelectedOrders().stream()
                    .map(CreateOutboundDeliveryRequest.SalesOrderIdDTO::getId)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // 辅助方法：验证物品数据
    private boolean validateItem(OutboundDeliveryItemDTO item) {
        return item != null 
            && item.getPickingQuantity() != null 
            && item.getPickingQuantity().compareTo(BigDecimal.ZERO) > 0;
    }

    // 辅助方法：转换详情数据为前端格式
    private OutboundDeliveryDetailResponse convertToDetailResponse(OutboundDeliveryDetailRawDTO rawDetail, List<OutboundDeliveryItemDTO> items) {
        // 转换详情数据
        OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta meta = 
            new OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta();
        meta.setId(String.valueOf(rawDetail.getId()));
        meta.setPosted(rawDetail.getPosted());
        meta.setReadyToPost(rawDetail.getReadyToPost());

        OutboundDeliveryDetailResponse.OutboundDeliveryDetail detail = 
            new OutboundDeliveryDetailResponse.OutboundDeliveryDetail();
        detail.setMeta(meta);
        detail.setActualGIDate(rawDetail.getActualGIDate() != null ? dateFormat.format(rawDetail.getActualGIDate()) : null);
        detail.setPlannedGIDate(rawDetail.getPlannedGIDate() != null ? dateFormat.format(rawDetail.getPlannedGIDate()) : null);
        detail.setActualDate(rawDetail.getActualDate() != null ? dateFormat.format(rawDetail.getActualDate()) : null);
        detail.setLoadingDate(rawDetail.getLoadingDate() != null ? dateFormat.format(rawDetail.getLoadingDate()) : null);
        detail.setDeliveryDate(rawDetail.getDeliveryDate() != null ? dateFormat.format(rawDetail.getDeliveryDate()) : null);
        detail.setPickingStatus(rawDetail.getPickingStatus());
        detail.setOverallStatus(rawDetail.getOverallStatus());
        detail.setGiStatus(rawDetail.getGiStatus());
        detail.setShipToParty(rawDetail.getShipToPartyName());
        detail.setAddress(rawDetail.getAddress());
        detail.setGrossWeight(rawDetail.getGrossWeight() != null ? rawDetail.getGrossWeight().toString() : "0.000");
        detail.setGrossWeightUnit(rawDetail.getGrossWeightUnit());
        detail.setNetWeight(rawDetail.getNetWeight() != null ? rawDetail.getNetWeight().toString() : "0.000");
        detail.setNetWeightUnit(rawDetail.getNetWeightUnit());
        detail.setVolume(rawDetail.getVolume() != null ? rawDetail.getVolume().toString() : "0.000");
        detail.setVolumeUnit(rawDetail.getVolumeUnit());
        detail.setPriority(rawDetail.getPriority());
        detail.setShippingPoint(rawDetail.getShippingPoint());

        OutboundDeliveryDetailResponse.OutboundDeliveryItems itemsWrapper = 
            new OutboundDeliveryDetailResponse.OutboundDeliveryItems();
        itemsWrapper.setItems(items);

        OutboundDeliveryDetailResponse response = new OutboundDeliveryDetailResponse();
        response.setDetail(detail);
        response.setItems(itemsWrapper);

        return response;
    }
}
