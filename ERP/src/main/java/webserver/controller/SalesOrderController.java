package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.SalesOrderSearchRequest;
import webserver.service.SalesOrderService;

@Slf4j
@RestController
@RequestMapping("/api/so")
@CrossOrigin(origins = "*")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @PostMapping("/search")
    public Response searchSalesOrders(@RequestBody SalesOrderSearchRequest request) {
        log.info("Sales order search request: {}", request);
        return salesOrderService.searchSalesOrders(request);
    }
}
