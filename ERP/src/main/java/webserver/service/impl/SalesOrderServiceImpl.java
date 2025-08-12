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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {
    
    private final SalesOrderMapper salesOrderMapper;

    @Autowired
    private OutboundDeliveryMapper outboundDeliveryMapper;

    @Override
    public Response<?> searchSalesOrders(SalesOrderSearchRequest request) {
        try {
            List<Map<String, Object>> orders = salesOrderMapper.searchSalesOrders(request);

            // 将结果转换为符合要求的格式
            List<Map<String, Object>> formattedResults = orders.stream()
                    .map(order -> {
                        // 创建新的HashMap避免修改原始数据
                        Map<String, Object> formattedOrder = new HashMap<>(order);

                        // 更健壮的数值处理
                        Object netValueObj = formattedOrder.get("netValue");
                        if (netValueObj != null) {
                            try {
                                // 统一转换为BigDecimal处理
                                BigDecimal netValue = new BigDecimal(netValueObj.toString());
                                DecimalFormat df = new DecimalFormat("#.00");
                                formattedOrder.put("netValue", df.format(netValue));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid netValue format: " + netValueObj);
                                formattedOrder.put("netValue", "0.00");
                            }
                        }

                        return formattedOrder;
                    })
                    .collect(Collectors.toList());

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
            
            List<Map<String, Object>> itemsData = salesOrderMapper.getSalesOrderItems(soId);
            
            SalesOrderDetailDTO orderDetail = buildSalesOrderDetail(orderData, itemsData);
            return Response.success(orderDetail);
        } catch (Exception e) {
            log.error("Error getting sales order details: ", e);
            return Response.error("Failed to load sales order details.");
        }
    }
    
    // 方法访问权限从 private 改为 public
    public SalesOrderDetailDTO buildSalesOrderDetail(Map<String, Object> orderData, List<Map<String, Object>> itemsData) {
        SalesOrderDetailDTO result = new SalesOrderDetailDTO();

        // 构建meta部分
        result.getMeta().setId(convertToString(orderData.get("soId")));

        // 构建basicInfo部分
        SalesOrderDetailDTO.BasicInfo basicInfo = result.getBasicInfo();
        basicInfo.setQuotation_id(convertToString(orderData.get("quotationId")));
        basicInfo.setSo_id(convertToString(orderData.get("soId")));
        basicInfo.setSoldToParty(convertToString(orderData.get("soldToParty")));
        basicInfo.setShipToParty(convertToString(orderData.get("shipToParty")));
        basicInfo.setCustomerReference(convertToString(orderData.get("customerReference")));
        basicInfo.setNetValue(convertToString(orderData.get("netValue")));
        basicInfo.setNetValueUnit(convertToString(orderData.get("currency")));
        basicInfo.setCustomerReferenceDate(convertToString(orderData.get("customerReferenceDate")));

        // 构建itemOverview部分
        result.getItemOverview().setReqDelivDate(convertToString(orderData.get("reqDeliveryDate")));

        // 构建items部分
        List<SalesOrderDetailDTO.Item> items = itemsData.stream()
                .map(item -> {
                    SalesOrderDetailDTO.Item itemDTO = new SalesOrderDetailDTO.Item();
                    itemDTO.setItem(convertToString(item.get("item")));
                    itemDTO.setMaterial(convertToString(item.get("material")));
                    itemDTO.setOrderQuantity(convertToString(item.get("orderQuantity")));
                    itemDTO.setOrderQuantityUnit(convertToString(item.get("orderQuantityUnit")));
                    itemDTO.setDescription(convertToString(item.get("description")));
                    itemDTO.setReqDelivDate(convertToString(item.get("reqDelivDate")));
                    itemDTO.setNetValue(convertToString(item.get("netValue")));
                    itemDTO.setNetValueUnit(convertToString(item.get("netValueUnit")));
                    itemDTO.setTaxValue(convertToString(item.get("taxValue")));
                    itemDTO.setTaxValueUnit(convertToString(item.get("taxValueUnit")));
                    itemDTO.setPricingDate(convertToString(item.get("pricingDate")));
                    itemDTO.setOrderProbability(convertToString(item.get("orderProbability")));
                    return itemDTO;
                })
                .toList();

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

            // 5. 插入订单项目
            if (request.getItemOverview() != null && request.getItemOverview().getItems() != null) {
                insertOrderItems(soId, request.getItemOverview().getItems());
            }

            // 6. 构建成功响应
            Map<String, Object> data = new HashMap<>();
            data.put("so_id", soId.toString());

            log.info("销售订单创建成功，订单ID: {}", soId);
            return Response.success(data);

        } catch (Exception e) {
            log.error("创建销售订单失败: ", e);
            return Response.error("Operation failed.");
        }
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

        // 2. 设置客户和联系人ID（必需）
        try {
            // 客户ID处理 (soldToParty)
            if (StringUtils.hasText(request.getBasicInfo().getSoldToParty())) {
                String soldToParty = request.getBasicInfo().getSoldToParty();
                Long customerId = parseIdWithPrefix(soldToParty, "CUST-");
                order.setCustomerId(customerId);
                log.debug("设置客户ID: {}", customerId);
            } else {
                throw new IllegalArgumentException("soldToParty不能为空");
            }

            // 联系人ID处理 (shipToParty)
            if (StringUtils.hasText(request.getBasicInfo().getShipToParty())) {
                String shipToParty = request.getBasicInfo().getShipToParty();
                Long contactId = parseIdWithPrefix(shipToParty, "SHIP-");
                order.setContactId(contactId);
                log.debug("设置联系人ID: {}", contactId);
            } else {
                throw new IllegalArgumentException("shipToParty不能为空");
            }
        } catch (Exception e) {
            log.error("客户或联系人ID解析失败: ", e);
            throw new IllegalArgumentException("客户或联系人ID格式不正确", e);
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
                double netValue = Double.parseDouble(request.getBasicInfo().getNetValue());
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
     * 插入订单项目
     * @param soId 订单ID
     * @param items 订单项目列表
     */
    private void insertOrderItems(Long soId, List<SalesOrderCreateRequest.Item> items) {
        log.info("开始插入订单项目，订单ID: {}, 项目数量: {}", soId, items.size());

        for (int i = 0; i < items.size(); i++) {
            SalesOrderCreateRequest.Item item = items.get(i);

            // 解析物料ID
            Long matId = parseMaterialId(item.getMaterial());

            // 解析数量
            int quantity = parseQuantity(item.getOrderQuantity());

            // 解析单价
            double netPrice = parseNetPrice(item.getNetValue());

            // 插入订单项目
            int result = salesOrderMapper.insertSalesOrderItem(
                    soId, i + 1, matId, 1000L, "0001",
                    quantity, item.getOrderQuantityUnit(), netPrice
            );

            if (result <= 0) {
                log.error("插入订单项目失败: 项目号 {}", i + 1);
                throw new RuntimeException("插入订单项目失败: 项目号 " + (i + 1));
            }

            log.debug("订单项目插入成功: 订单ID={}, 项目号={}, 物料ID={}, 数量={}, 单价={}",
                    soId, i + 1, matId, quantity, netPrice);
        }

        log.info("订单项目插入完成，订单ID: {}", soId);
    }

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
            
            // 2. 验证meta.id和basicInfo.so_id是否一致
            if (!StringUtils.hasText(request.getMeta().getId()) || 
                !request.getMeta().getId().equals(soId)) {
                log.warn("meta.id和请求路径ID不一致");
                return Response.error("meta.id和请求路径ID不一致");
            }
            
            if (!StringUtils.hasText(request.getBasicInfo().getSo_id()) || 
                !request.getBasicInfo().getSo_id().equals(soId)) {
                log.warn("basicInfo.so_id和请求路径ID不一致");
                return Response.error("basicInfo.so_id和请求路径ID不一致");
            }
            
            // 3. 构建销售订单对象
            SalesOrder order = buildSalesOrderFromRequest(request);
            order.setSoId(Long.valueOf(soId)); // 设置要更新的订单ID
            
            // 4. 更新订单头
            int result = salesOrderMapper.updateSalesOrder(order);
            if (result <= 0) {
                log.error("更新订单头失败: {}", soId);
                return Response.error("更新订单头失败");
            }
            
            // 5. 删除原有订单项目
            salesOrderMapper.deleteSalesOrderItemsBySoId(order.getSoId());
            
            // 6. 插入新的订单项目
            if (request.getItemOverview() != null && request.getItemOverview().getItems() != null) {
                insertOrderItems(order.getSoId(), request.getItemOverview().getItems());
            }
            
            log.info("销售订单修改成功: {}", soId);
            return Response.success("Sales Order updated successfully!");
            
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
    public Response<OutboundDeliveryDetailDTO> getOutboundDeliveryDetail(String deliveryId) {
        OutboundDeliveryDetailDTO detail = outboundDeliveryMapper.getOutboundDeliveryDetail(deliveryId);
        if (detail == null) {
            return Response.error("未找到交货单: " + deliveryId);
        }
        List<OutboundDeliveryItemDTO> items = outboundDeliveryMapper.getDeliveryItems(deliveryId);
        detail.setItems(items);
        return Response.success(detail);
    }

}
