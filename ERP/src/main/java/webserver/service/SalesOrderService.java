package webserver.service;

import webserver.common.Response;
import webserver.pojo.SalesOrderCreateRequest;
import webserver.pojo.SalesOrderSearchRequest;
import webserver.pojo.SalesOrderDetailDTO;

public interface SalesOrderService {
    Response searchSalesOrders(SalesOrderSearchRequest request);
    Response<SalesOrderDetailDTO> getSalesOrderDetails(String soId);
    Response createSalesOrder(SalesOrderCreateRequest request);
    Response updateSalesOrder(String soId, SalesOrderCreateRequest request);
}




