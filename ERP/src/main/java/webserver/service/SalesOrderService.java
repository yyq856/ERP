package webserver.service;


import webserver.common.Response;
import webserver.pojo.SalesOrderDetailDTO;
import webserver.pojo.SalesOrderSearchRequest;

public interface SalesOrderService {
    Response searchSalesOrders(SalesOrderSearchRequest request);
    Response<SalesOrderDetailDTO> getSalesOrderDetails(String soId);

}



