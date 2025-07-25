package webserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // 添加这个导入
import org.springframework.stereotype.Service;
import webserver.common.Response;
import webserver.mapper.SalesOrderMapper;
import webserver.pojo.SalesOrderSearchRequest;
import webserver.pojo.SalesOrderDetailDTO;
import webserver.service.SalesOrderService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {
    
    private final SalesOrderMapper salesOrderMapper;

    @Override
    public Response searchSalesOrders(SalesOrderSearchRequest request) {
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
                                log.warn("Invalid netValue format: " + netValueObj);  // 修改为log
                                formattedOrder.put("netValue", "0.00");
                            }
                        }

                        return formattedOrder;
                    })
                    .collect(Collectors.toList());

            return Response.success(formattedResults);
        } catch (Exception e) {
            log.error("Sales order search error: " + e.getMessage());  // 修改为log
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

}
