package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.SalesOrderCreateRequest;
import webserver.pojo.SalesOrderDetailDTO;
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
    @GetMapping("/get/{so_id}")
    public Response<SalesOrderDetailDTO> getSalesOrderDetails(@PathVariable("so_id") String soId) {
        log.info("获取销售订单详情: {}", soId);
        return salesOrderService.getSalesOrderDetails(soId);
    }

    /**
     * 获取销售订单详情（POST方法，兼容前端现有实现）
     * @param soId 销售订单ID
     * @return 销售订单详情
     */
    @PostMapping("/get/{so_id}")
    public Response<SalesOrderDetailDTO> getSalesOrderDetailsPost(@PathVariable("so_id") String soId) {
        log.info("获取销售订单详情(POST): {}", soId);
        return salesOrderService.getSalesOrderDetails(soId);
    }

    @PostMapping("/create")
    public Response createSalesOrder(@RequestBody SalesOrderCreateRequest request) {
        return salesOrderService.createSalesOrder(request);
    }

    @PostMapping("/edit")
    public Response updateSalesOrder(@RequestBody SalesOrderCreateRequest request) {
        // 从请求中获取so_id
        String soId = request.getBasicInfo().getSo_id();
        if (!StringUtils.hasText(soId)) {
            return Response.error("so_id不能为空");
        }
        
        // 验证meta.id和basicInfo.so_id是否一致
        if (!request.getMeta().getId().equals(soId)) {
            return Response.error("meta.id和so_id不一致");
        }
        
        return salesOrderService.updateSalesOrder(soId, request);
    }
}
