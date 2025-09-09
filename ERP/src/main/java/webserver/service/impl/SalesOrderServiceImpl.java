package webserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import webserver.common.Response;
import webserver.mapper.OutboundDeliveryMapper;
import webserver.mapper.SalesOrderMapper;
import webserver.pojo.*;
import webserver.service.SalesOrderService;
import webserver.service.SalesOrderCalculationService;
import webserver.service.ValidateItemsService;
import webserver.service.UnifiedItemService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {
    
    private final SalesOrderMapper salesOrderMapper;

    @Autowired
    private OutboundDeliveryMapper outboundDeliveryMapper;

    @Autowired
    private ValidateItemsService validateItemsService;

    @Autowired
    private UnifiedItemService unifiedItemService;

    @Autowired
    private SalesOrderCalculationService salesOrderCalculationService;

    @Override
    public Response<?> searchSalesOrders(SalesOrderSearchRequest request) {
        try {
            List<Map<String, Object>> orders = salesOrderMapper.searchSalesOrders(request);

            // 将结果转换为指定的格式
            List<Map<String, Object>> formattedResults = orders.stream()
                    .map(order -> {
                        Map<String, Object> result = new HashMap<>();

                        // 构建meta部分
                        Map<String, Object> meta = new HashMap<>();
                        String materialIds = (String) order.get("materialIds");
                        if (materialIds != null && !materialIds.isEmpty()) {
                            meta.put("id", Arrays.asList(materialIds.split(",")));
                        } else {
                            meta.put("id", Collections.emptyList());
                        }
                        result.put("meta", meta);

                        // 构建basicInfo部分
                        Map<String, Object> basicInfo = new HashMap<>();
                        basicInfo.put("quotation_id", order.get("quotationId"));
                        basicInfo.put("so_id", order.get("soId"));
                        basicInfo.put("soldToParty", order.get("customerNo"));
                        basicInfo.put("shipToParty", order.get("shipToParty"));
                        basicInfo.put("customerReference", order.get("customerReference"));
                        basicInfo.put("netValue", order.get("netValue"));
                        basicInfo.put("netValueUnit", order.get("currency"));
                        basicInfo.put("customerReferenceDate", order.get("docDate"));
                        basicInfo.put("status", order.get("status")); // 添加status字段
                        result.put("basicInfo", basicInfo);

                        // 构建itemOverview部分
                        Map<String, Object> itemOverview = new HashMap<>();
                        itemOverview.put("reqDelivDate", order.get("reqDeliveryDate"));
                        result.put("itemOverview", itemOverview);

                        return result;
                    })
                    .collect(Collectors.toList());

            // 直接返回结果，不额外包装
            return Response.success(formattedResults);
        } catch (Exception e) {
            log.error("Sales order search error: " + e.getMessage());
            e.printStackTrace();
            return Response.error("Failed to search sales orders: " + e.getMessage());
        }
    }
    
    @Override
    public Response<SalesOrderDetailDTO> getSalesOrderDetails(String soId) {
        try {
            Map<String, Object> orderData = salesOrderMapper.getSalesOrderDetails(soId);
            if (orderData == null || orderData.isEmpty()) {
                return Response.error("Sales order not found");
            }

            // ✅ 使用统一服务读取items并转换为前端格式
            List<Map<String, Object>> frontendItems = unifiedItemService.getDocumentItemsAsFrontendFormat(Long.parseLong(soId), "sales_order");

            // ✅ 转换前端格式为SalesOrderDetailDTO.Item
            List<SalesOrderDetailDTO.Item> salesOrderItems = convertFrontendItemsToSalesOrderItems(frontendItems);

            SalesOrderDetailDTO orderDetail = buildSalesOrderDetail(orderData, salesOrderItems);
            return Response.success(orderDetail);
        } catch (Exception e) {
            log.error("Error getting sales order details: ", e);
            return Response.error("Failed to load sales order details.");
        }
    }
    
    // 方法访问权限从 private 改为 public
    public SalesOrderDetailDTO buildSalesOrderDetail(Map<String, Object> orderData, List<SalesOrderDetailDTO.Item> items) {
        SalesOrderDetailDTO result = new SalesOrderDetailDTO();

        // 构建meta部分 - 使用物料ID列表
        String materialIds = convertToString(orderData.get("materialIds"));
        if (materialIds != null && !materialIds.isEmpty()) {
            result.getMeta().setId(materialIds);
        } else {
            result.getMeta().setId("");
        }

        // 构建basicInfo部分
        SalesOrderDetailDTO.BasicInfo basicInfo = result.getBasicInfo();
        basicInfo.setSalesQuotationId(convertToString(orderData.get("quotationId")));
        basicInfo.setSo_id(convertToString(orderData.get("soId")));
        basicInfo.setSoldToParty(convertToString(orderData.get("soldToParty"))); // 客户ID而不是客户名称
        basicInfo.setShipToParty(convertToString(orderData.get("shipToParty"))); // 客户ID而不是客户名称
        basicInfo.setCustomerReference(convertToString(orderData.get("customerReference")));
        basicInfo.setNetValue(convertToString(orderData.get("netValue")));
        basicInfo.setNetValueUnit(convertToString(orderData.get("currency")));
        basicInfo.setCustomerReferenceDate(convertToString(orderData.get("customerReferenceDate")));

        // 构建itemOverview部分
        result.getItemOverview().setReqDelivDate(convertToString(orderData.get("reqDeliveryDate")));

        // ✅ 直接使用已经转换好的SalesOrderDetailDTO.Item列表
        // 统一服务已经处理了所有字段映射和JSON解析

        result.getItemOverview().setItems(items);

        return result;
    }

    /**
     * 安全地将对象转换为字符串
     * @param obj 要转换的对象
     * @return 字符串表示形式
     */
    private String convertToString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }

    /**
     * 创建销售订单
     * @param request 请求参数
     * @return 响应结果
     */
    @Override
    @Transactional
    public Response<?> createSalesOrder(SalesOrderCreateRequest request) {
        try {
            log.info("开始创建销售订单");

            // 1. 验证请求数据
            if (request == null || request.getBasicInfo() == null) {
                log.warn("创建销售订单请求数据为空");
                return Response.error("请求数据不能为空");
            }

            // 2. 构建销售订单对象
            SalesOrder order = buildSalesOrderFromRequest(request);
            log.debug("销售订单对象构建完成: {}", order);

            // 3. 插入订单头
            int result = salesOrderMapper.insertSalesOrder(order);
            if (result <= 0) {
                log.error("创建订单头失败");
                return Response.error("创建订单头失败");
            }

            // 4. 获取生成的订单ID
            Long soId = order.getSoId();
            log.info("销售订单头创建成功，订单ID: {}", soId);

            // 5. 插入订单项目 - 使用统一方法
            if (request.getItemOverview() != null && request.getItemOverview().getItems() != null) {
                // 转换为统一的前端数据格式
                List<Map<String, Object>> frontendItems = convertSalesOrderItemsToFrontendFormat(request.getItemOverview().getItems());

                // 调用统一服务（pricingElements已经包含在frontendItems中，不需要单独插入）
                unifiedItemService.updateDocumentItems(soId, "sales_order", frontendItems);
            }

            // 7. 构建成功响应
            Map<String, Object> data = new HashMap<>();
            data.put("so_id", soId.toString());
            log.info("销售订单创建成功，订单ID: {}", soId);
            return Response.success("Sales Order " + soId + " created successfully!", data);

        } catch (Exception e) {
            log.error("创建销售订单失败: ", e);
            return Response.error("Operation failed.");
        }
    }

    /**
     * 将SalesOrderCreateRequest.Item转换为统一的前端数据格式
     */
    private List<Map<String, Object>> convertSalesOrderItemsToFrontendFormat(List<SalesOrderCreateRequest.Item> items) {
        List<Map<String, Object>> frontendItems = new ArrayList<>();

        for (SalesOrderCreateRequest.Item item : items) {
            Map<String, Object> frontendItem = new HashMap<>();

            // 基础字段
            frontendItem.put("item", item.getItem());
            frontendItem.put("material", item.getMaterial());
            frontendItem.put("orderQuantity", item.getOrderQuantity());
            frontendItem.put("orderQuantityUnit", item.getOrderQuantityUnit());
            frontendItem.put("description", item.getDescription());

            // ItemValidation字段
            frontendItem.put("reqDelivDate", item.getReqDelivDate());
            frontendItem.put("netValue", item.getNetValue());
            frontendItem.put("netValueUnit", item.getNetValueUnit());
            frontendItem.put("taxValue", item.getTaxValue());
            frontendItem.put("taxValueUnit", item.getTaxValueUnit());
            frontendItem.put("pricingDate", item.getPricingDate());
            frontendItem.put("orderProbability", item.getOrderProbability());
            frontendItem.put("pricingElements", item.getPricingElements());

            frontendItems.add(frontendItem);
        }

        return frontendItems;
    }

    /**
     * 从请求构建销售订单对象
     * @param request 请求参数
     * @return 销售订单对象
     */
    private SalesOrder buildSalesOrderFromRequest(SalesOrderCreateRequest request) {
        SalesOrder order = new SalesOrder();

        // 1. 处理报价单ID（可选）
        if (StringUtils.hasText(request.getBasicInfo().getQuotation_id())) {
            try {
                order.setQuotationId(Long.valueOf(request.getBasicInfo().getQuotation_id()));
                log.debug("设置报价单ID: {}", order.getQuotationId());
            } catch (NumberFormatException e) {
                log.warn("报价单ID格式不正确: {}", request.getBasicInfo().getQuotation_id());
            }
        } 
        // 处理新的报价单ID字段名
        else if (StringUtils.hasText(request.getBasicInfo().getSalesQuotationId())) {
            try {
                order.setQuotationId(Long.valueOf(request.getBasicInfo().getSalesQuotationId()));
                log.debug("设置报价单ID: {}", order.getQuotationId());
            } catch (NumberFormatException e) {
                log.warn("报价单ID格式不正确: {}", request.getBasicInfo().getSalesQuotationId());
            }
        }

        // 2. 设置售达方和送达方客户ID（必需）
        try {
            // 售达方处理 (soldToParty)
            if (StringUtils.hasText(request.getBasicInfo().getSoldToParty())) {
                String soldToParty = request.getBasicInfo().getSoldToParty();
                Long customerId = parseIdWithPrefix(soldToParty, "CUST-");
                order.setSoldTp(customerId);  // 使用新的sold_tp字段
                log.debug("设置售达方ID: {}", customerId);
            } else {
                throw new IllegalArgumentException("soldToParty不能为空");
            }

            // 送达方处理 (shipToParty)
            if (StringUtils.hasText(request.getBasicInfo().getShipToParty())) {
                String shipToParty = request.getBasicInfo().getShipToParty();
                Long customerId = parseIdWithPrefix(shipToParty, "CUST-");
                order.setShipTp(customerId);  // 使用新的ship_tp字段
                log.debug("设置送达方ID: {}", customerId);
            } else {
                throw new IllegalArgumentException("shipToParty不能为空");
            }
        } catch (Exception e) {
            log.error("售达方或送达方ID解析失败: ", e);
            throw new IllegalArgumentException("售达方或送达方ID格式不正确", e);
        }

        // 3. 日期处理
        try {
            if (StringUtils.hasText(request.getItemOverview().getReqDelivDate())) {
                try {
                    LocalDate reqDeliveryDate = webserver.util.DateUtil.parseDate(request.getItemOverview().getReqDelivDate());
                    order.setReqDeliveryDate(reqDeliveryDate);
                    log.debug("设置要求交货日期: {}", reqDeliveryDate);
                } catch (Exception e) {
                    log.error("请求交货日期格式错误: {}", e.getMessage());
                    throw new RuntimeException("请求交货日期格式不正确: " + e.getMessage());
                }
            } else {
                // 默认为当前日期
                order.setReqDeliveryDate(LocalDate.now());
                log.debug("使用默认交货日期: {}", order.getReqDeliveryDate());
            }
        } catch (Exception e) {
            log.error("交货日期解析失败: ", e);
            throw new IllegalArgumentException("交货日期格式不正确", e);
        }

        // 4. 设置货币信息
        String currency = request.getBasicInfo().getNetValueUnit();
        if (!StringUtils.hasText(currency)) {
            currency = "USD"; // 默认货币
        }
        order.setCurrency(currency);
        log.debug("设置货币: {}", currency);

        // 5. 金额处理
        try {
            if (StringUtils.hasText(request.getBasicInfo().getNetValue())) {
                // 移除千位分隔符（逗号）后再解析
                String netValueStr = request.getBasicInfo().getNetValue().replaceAll(",", "");
                double netValue = Double.parseDouble(netValueStr);
                order.setNetValue(netValue);
                order.setTaxValue(netValue * 0.1);  // 假设10%税
                order.setGrossValue(netValue + order.getTaxValue());
                log.debug("设置金额: 净值={}, 税额={}, 总值={}", netValue, order.getTaxValue(), order.getGrossValue());
            } else {
                order.setNetValue(0.0);
                order.setTaxValue(0.0);
                order.setGrossValue(0.0);
                log.debug("使用默认金额值: 0.0");
            }
        } catch (Exception e) {
            log.error("金额解析失败: ", e);
            throw new IllegalArgumentException("金额格式不正确", e);
        }

        // 6. 设置默认值
        order.setIncoterms("EXW"); // 默认贸易条款
        order.setPaymentTerms("0001"); // 默认付款条件

        return order;
    }

    /**
     * 从请求构建销售订单对象（不设置金额，金额将通过计算服务设置）
     * @param request 请求参数
     * @return 销售订单对象
     */
    private SalesOrder buildSalesOrderFromRequestWithoutAmount(SalesOrderCreateRequest request) {
        SalesOrder order = new SalesOrder();

        // 1. 处理报价单ID（可选）
        if (StringUtils.hasText(request.getBasicInfo().getQuotation_id())) {
            try {
                order.setQuotationId(Long.valueOf(request.getBasicInfo().getQuotation_id()));
                log.debug("设置报价单ID: {}", order.getQuotationId());
            } catch (NumberFormatException e) {
                log.warn("报价单ID格式不正确: {}", request.getBasicInfo().getQuotation_id());
            }
        }
        // 处理新的报价单ID字段名
        else if (StringUtils.hasText(request.getBasicInfo().getSalesQuotationId())) {
            try {
                order.setQuotationId(Long.valueOf(request.getBasicInfo().getSalesQuotationId()));
                log.debug("设置报价单ID: {}", order.getQuotationId());
            } catch (NumberFormatException e) {
                log.warn("报价单ID格式不正确: {}", request.getBasicInfo().getSalesQuotationId());
            }
        }

        // 2. 设置售达方和送达方客户ID（必需）
        try {
            // 售达方处理 (soldToParty)
            if (StringUtils.hasText(request.getBasicInfo().getSoldToParty())) {
                String soldToParty = request.getBasicInfo().getSoldToParty();
                Long customerId = parseIdWithPrefix(soldToParty, "CUST-");
                order.setSoldTp(customerId);  // 使用新的sold_tp字段
                log.debug("设置售达方ID: {}", customerId);
            } else {
                throw new IllegalArgumentException("soldToParty不能为空");
            }

            // 送达方处理 (shipToParty)
            if (StringUtils.hasText(request.getBasicInfo().getShipToParty())) {
                String shipToParty = request.getBasicInfo().getShipToParty();
                Long customerId = parseIdWithPrefix(shipToParty, "CUST-");
                order.setShipTp(customerId);  // 使用新的ship_tp字段
                log.debug("设置送达方ID: {}", customerId);
            } else {
                throw new IllegalArgumentException("shipToParty不能为空");
            }
        } catch (Exception e) {
            log.error("售达方或送达方ID解析失败: ", e);
            throw new IllegalArgumentException("售达方或送达方ID格式不正确", e);
        }

        // 3. 日期处理
        try {
            if (StringUtils.hasText(request.getItemOverview().getReqDelivDate())) {
                try {
                    LocalDate reqDeliveryDate = webserver.util.DateUtil.parseDate(request.getItemOverview().getReqDelivDate());
                    order.setReqDeliveryDate(reqDeliveryDate);
                    log.debug("设置要求交货日期: {}", reqDeliveryDate);
                } catch (Exception e) {
                    log.error("请求交货日期格式错误: {}", e.getMessage());
                    throw new RuntimeException("请求交货日期格式不正确: " + e.getMessage());
                }
            } else {
                // 默认为当前日期
                order.setReqDeliveryDate(LocalDate.now());
                log.debug("使用默认交货日期: {}", order.getReqDeliveryDate());
            }
        } catch (Exception e) {
            log.error("交货日期解析失败: ", e);
            throw new IllegalArgumentException("交货日期格式不正确", e);
        }

        // 4. 设置货币信息
        String currency = request.getBasicInfo().getNetValueUnit();
        if (!StringUtils.hasText(currency)) {
            currency = "USD"; // 默认货币
        }
        order.setCurrency(currency);
        log.debug("设置货币: {}", currency);

        // 注意：不设置金额字段，这些将通过 SalesOrderCalculationService 计算和设置
        log.debug("金额字段将通过计算服务设置，不使用前端传来的数据");

        // 5. 设置默认值
        order.setIncoterms("EXW"); // 默认贸易条款
        order.setPaymentTerms("0001"); // 默认付款条件

        return order;
    }

    // insertOrderItems方法已删除，现在使用UnifiedItemService



    /**
     * 解析带前缀的ID
     * @param idWithPrefix 带前缀的ID
     * @param prefix 前缀
     * @return 解析后的ID
     */
    private Long parseIdWithPrefix(String idWithPrefix, String prefix) {
        if (!StringUtils.hasText(idWithPrefix)) {
            throw new IllegalArgumentException("ID不能为空");
        }

        try {
            if (idWithPrefix.startsWith(prefix)) {
                return Long.valueOf(idWithPrefix.substring(prefix.length()));
            } else {
                return Long.valueOf(idWithPrefix);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("ID格式不正确: " + idWithPrefix, e);
        }
    }

    /**
     * 解析物料ID
     * @param material 物料ID字符串
     * @return 解析后的物料ID
     */
    private Long parseMaterialId(String material) {
        if (!StringUtils.hasText(material)) {
            log.warn("物料ID为空，使用默认值: 1");
            return 1L; // 默认物料ID
        }

        try {
            if (material.startsWith("MAT-")) {
                return Long.valueOf(material.substring(4));
            } else {
                return Long.valueOf(material);
            }
        } catch (Exception e) {
            log.warn("物料ID格式不正确，使用默认值: {}, 错误: {}", material, e.getMessage());
            return 1L; // 默认值
        }
    }

    /**
     * 解析数量
     * @param quantityStr 数量字符串
     * @return 解析后的数量
     */
    private int parseQuantity(String quantityStr) {
        if (!StringUtils.hasText(quantityStr)) {
            log.warn("数量为空，使用默认值: 1");
            return 1; // 默认数量
        }

        try {
            return Integer.parseInt(quantityStr);
        } catch (Exception e) {
            log.warn("数量格式不正确，使用默认值: {}, 错误: {}", quantityStr, e.getMessage());
            return 1; // 默认数量
        }
    }

    /**
     * 解析单价
     * @param netPriceStr 单价字符串
     * @return 解析后的单价
     */
    private double parseNetPrice(String netPriceStr) {
        if (!StringUtils.hasText(netPriceStr)) {
            log.warn("单价为空，使用默认值: 100.0");
            return 100.0; // 默认单价
        }

        try {
            return Double.parseDouble(netPriceStr);
        } catch (Exception e) {
            log.warn("单价格式不正确，使用默认值: {}, 错误: {}", netPriceStr, e.getMessage());
            return 100.0; // 默认单价
        }
    }

    /**
     * 修改销售订单
     * @param soId 订单ID
     * @param request 请求参数
     * @return 响应结果
     */
    @Override
    @Transactional
    public Response<?> updateSalesOrder(String soId, SalesOrderCreateRequest request) {
        try {
            log.info("开始修改销售订单: {}", soId);

            // 1. 验证请求数据
            if (request == null || request.getBasicInfo() == null) {
                log.warn("修改销售订单请求数据为空");
                return Response.error("请求数据不能为空");
            }



            if (!StringUtils.hasText(request.getBasicInfo().getSo_id()) ||
                !request.getBasicInfo().getSo_id().equals(soId)) {
                log.warn("basicInfo.so_id和请求路径ID不一致");
                return Response.error("basicInfo.so_id和请求路径ID不一致");
            }

            // 3. 构建销售订单对象（使用前端传来的netValue）
            SalesOrder order = buildSalesOrderFromRequest(request);
            order.setSoId(Long.valueOf(soId)); // 设置要更新的订单ID

            // 4. 先更新订单头（包含前端传来的金额）
            int result = salesOrderMapper.updateSalesOrder(order);
            if (result <= 0) {
                log.error("更新订单头失败: {}", soId);
                return Response.error("更新订单头失败");
            }

            // 5. 更新订单项目 - 使用统一方法（但不触发金额重新计算）
            if (request.getItemOverview() != null && request.getItemOverview().getItems() != null) {
                // 转换为统一的前端数据格式
                List<Map<String, Object>> frontendItems = convertSalesOrderItemsToFrontendFormat(request.getItemOverview().getItems());

                // 调用统一服务，但禁用事件触发以避免金额被重新计算
                unifiedItemService.updateDocumentItemsWithoutEvent(order.getSoId(), "sales_order", frontendItems);
            }

            log.info("销售订单修改成功: {}", soId);
            return Response.success("Sales Order SO-" + soId + " updated successfully!");

        } catch (Exception e) {
            log.error("修改销售订单失败: ", e);
            return Response.error("Operation failed.");
        }
    }



    @Override
    public SalesOrdersResponse getSalesOrders(SalesOrdersRequest request) {
        List<OrderSummary> orders = salesOrderMapper.selectSalesOrders1(
                request.getShipToParty(),
                request.getPlannedCreationDate(),
                request.getRelevantForTM()
        );

        SalesOrdersResponse.DataContent dataContent = new SalesOrdersResponse.DataContent();
        dataContent.setOrders(orders);

        SalesOrdersResponse response = new SalesOrdersResponse();
        response.setSuccess(true);
        response.setMessage("获取销售订单成功");
        response.setData(dataContent);

        return response;
    }

    @Override
    public SalesOrderResponse itemsTabQuery(List<SalesOrderItemsTabQueryRequest.ItemQuery> items) {
        try {
            if (items == null || items.isEmpty()) {
                return SalesOrderResponse.error("查询项目不能为空");
            }

            // 转换为 ItemValidationRequest 格式并调用验证服务（复用inquiry的逻辑）
            List<ItemValidationRequest> validationRequests = convertSalesOrderItemsToValidationRequests(items);
            ItemValidationResponse validationResponse = validateItemsService.validateItems(validationRequests);

            // 将验证结果转换为 SalesOrder 的响应格式
            Map<String, Object> data = convertValidationResponseToSalesOrderFormat(validationResponse);

            log.info("物品批量查询成功，查询项目数: {}", items.size());
            return SalesOrderResponse.success(data, "批量查询成功");

        } catch (Exception e) {
            log.error("物品批量查询异常: {}", e.getMessage(), e);
            return SalesOrderResponse.error("服务器内部错误");
        }
    }

    /**
     * 转换 SalesOrderItemsTabQueryRequest.ItemQuery 到 ItemValidationRequest
     */
    private List<ItemValidationRequest> convertSalesOrderItemsToValidationRequests(List<SalesOrderItemsTabQueryRequest.ItemQuery> items) {
        List<ItemValidationRequest> validationRequests = new ArrayList<>();

        for (SalesOrderItemsTabQueryRequest.ItemQuery queryItem : items) {
            ItemValidationRequest request = new ItemValidationRequest();
            request.setItem(queryItem.getItem());
            request.setMaterial(queryItem.getMaterial());
            request.setOrderQuantity(queryItem.getOrderQuantity());
            request.setOrderQuantityUnit(queryItem.getOrderQuantityUnit());
            request.setDescription(queryItem.getDescription());
            request.setReqDelivDate(queryItem.getReqDelivDate());
            request.setNetValue(queryItem.getNetValue());
            request.setNetValueUnit(queryItem.getNetValueUnit());
            request.setTaxValue(queryItem.getTaxValue());
            request.setTaxValueUnit(queryItem.getTaxValueUnit());
            request.setPricingDate(queryItem.getPricingDate());
            request.setOrderProbability(queryItem.getOrderProbability());
            validationRequests.add(request);
        }

        return validationRequests;
    }

    /**
     * 将验证结果转换为 SalesOrder 的响应格式
     */
    private Map<String, Object> convertValidationResponseToSalesOrderFormat(ItemValidationResponse validationResponse) {
        Map<String, Object> data = new HashMap<>();

        if (validationResponse != null && validationResponse.getData() != null) {
            // 转换验证结果
            if (validationResponse.getData().getResult() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("allDataLegal", validationResponse.getData().getResult().getAllDataLegal());
                result.put("badRecordIndices", validationResponse.getData().getResult().getBadRecordIndices());
                data.put("result", result);
            }

            // 转换汇总信息
            if (validationResponse.getData().getGeneralData() != null) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalNetValue", validationResponse.getData().getGeneralData().getNetValue());
                summary.put("totalNetValueUnit", validationResponse.getData().getGeneralData().getNetValueUnit());
                summary.put("totalExpectOralVal", validationResponse.getData().getGeneralData().getExpectOralVal());
                summary.put("totalExpectOralValUnit", validationResponse.getData().getGeneralData().getExpectOralValUnit());
                data.put("summary", summary);
            }

            // 转换明细列表
            if (validationResponse.getData().getBreakdowns() != null) {
                List<Map<String, Object>> breakdowns = new ArrayList<>();
                for (ItemValidationResponse.ItemBreakdown breakdown : validationResponse.getData().getBreakdowns()) {
                    Map<String, Object> breakdownMap = new HashMap<>();
                    breakdownMap.put("item", breakdown.getItem());
                    breakdownMap.put("material", breakdown.getMaterial());
                    breakdownMap.put("orderQuantity", breakdown.getOrderQuantity());
                    breakdownMap.put("orderQuantityUnit", breakdown.getOrderQuantityUnit());
                    breakdownMap.put("description", breakdown.getDescription());
                    breakdownMap.put("reqDelivDate", breakdown.getReqDelivDate());
                    breakdownMap.put("netValue", breakdown.getNetValue());
                    breakdownMap.put("netValueUnit", breakdown.getNetValueUnit());
                    breakdownMap.put("taxValue", breakdown.getTaxValue());
                    breakdownMap.put("taxValueUnit", breakdown.getTaxValueUnit());
                    breakdownMap.put("pricingDate", breakdown.getPricingDate());
                    breakdownMap.put("orderProbability", breakdown.getOrderProbability());
                    breakdownMap.put("pricingElements", breakdown.getPricingElements());
                    breakdowns.add(breakdownMap);
                }
                data.put("breakdowns", breakdowns);
            }
        }

        return data;
    }

    /**
     * 将前端格式的items转换为SalesOrderDetailDTO.Item列表
     */
    private List<SalesOrderDetailDTO.Item> convertFrontendItemsToSalesOrderItems(List<Map<String, Object>> frontendItems) {
        List<SalesOrderDetailDTO.Item> items = new ArrayList<>();

        for (Map<String, Object> frontendItem : frontendItems) {
            SalesOrderDetailDTO.Item item = new SalesOrderDetailDTO.Item();

            // 基础字段
            item.setItem(getString(frontendItem, "item"));
            item.setMaterial(getString(frontendItem, "material"));
            item.setOrderQuantity(getString(frontendItem, "orderQuantity"));
            item.setOrderQuantityUnit(getString(frontendItem, "orderQuantityUnit"));
            item.setDescription(getString(frontendItem, "description"));

            // ItemValidation字段
            item.setReqDelivDate(getString(frontendItem, "reqDelivDate"));
            item.setNetValue(getString(frontendItem, "netValue"));
            item.setNetValueUnit(getString(frontendItem, "netValueUnit"));
            item.setTaxValue(getString(frontendItem, "taxValue"));
            item.setTaxValueUnit(getString(frontendItem, "taxValueUnit"));
            item.setPricingDate(getString(frontendItem, "pricingDate"));
            item.setOrderProbability(getString(frontendItem, "orderProbability"));

            // pricingElements - ✅ 使用独立的PricingElementDTO类，与QuotationServiceImpl保持一致
            Object pricingElementsObj = frontendItem.get("pricingElements");
            if (pricingElementsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> pricingElementMaps = (List<Map<String, Object>>) pricingElementsObj;
                List<PricingElementDTO> pricingElements = new ArrayList<>();

                for (Map<String, Object> elementMap : pricingElementMaps) {
                    PricingElementDTO element = new PricingElementDTO();
                    element.setCnty(getString(elementMap, "cnty"));
                    element.setName(getString(elementMap, "name"));
                    element.setAmount(getString(elementMap, "amount"));
                    element.setCity(getString(elementMap, "city"));
                    element.setPer(getString(elementMap, "per"));
                    element.setUom(getString(elementMap, "uom"));
                    element.setConditionValue(getString(elementMap, "conditionValue"));
                    element.setCurr(getString(elementMap, "curr"));
                    element.setStatus(getString(elementMap, "status"));
                    element.setNumC(getString(elementMap, "numC"));
                    element.setAtoMtsComponent(getString(elementMap, "atoMtsComponent"));
                    element.setOun(getString(elementMap, "oun"));
                    element.setCconDe(getString(elementMap, "cconDe"));
                    element.setUn(getString(elementMap, "un"));
                    element.setConditionValue2(getString(elementMap, "conditionValue2"));
                    element.setCdCur(getString(elementMap, "cdCur"));

                    Object statObj = elementMap.get("stat");
                    element.setStat(statObj instanceof Boolean ? (Boolean) statObj : false);

                    pricingElements.add(element);
                }

                item.setPricingElements(pricingElements);
            } else {
                item.setPricingElements(new ArrayList<>());
            }

            items.add(item);
        }

        return items;
    }

    /**
     * 安全地从Map中获取字符串值
     */
    private String getString(Map<String, Object> map, String key) {
        return getString(map, key, null);
    }

    /**
     * 安全地从Map中获取字符串值，带默认值
     */
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

}
