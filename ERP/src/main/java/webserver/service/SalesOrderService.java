package webserver.service;

import webserver.common.Response;
import webserver.pojo.*;

import java.util.List;


public interface SalesOrderService {
    Response<?> searchSalesOrders(SalesOrderSearchRequest request);
    Response<SalesOrderDetailDTO> getSalesOrderDetails(String soId);
    Response<?> createSalesOrder(SalesOrderCreateRequest request);
    Response<?> updateSalesOrder(String soId, SalesOrderCreateRequest request);
    SalesOrdersResponse getSalesOrders(SalesOrdersRequest request);

    /**
     * 物品批量查询
     * @param items 物品查询列表
     * @return 响应结果
     */
    SalesOrderResponse itemsTabQuery(List<SalesOrderItemsTabQueryRequest.ItemQuery> items);
}