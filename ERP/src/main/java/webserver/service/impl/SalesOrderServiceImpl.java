// 修改后的代码
package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;  // 添加这个导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.common.Response;
import webserver.mapper.SalesOrderMapper;
import webserver.pojo.SalesOrderSearchRequest;
import webserver.service.SalesOrderService;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j  // 添加这个注解
public class SalesOrderServiceImpl implements SalesOrderService {

    @Autowired
    private SalesOrderMapper salesOrderMapper;

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
}
