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
    Response<OutboundDeliveryDetailDTO> getOutboundDeliveryDetail(String deliveryId);
}