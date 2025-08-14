package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.*;
import webserver.service.SalesOrderService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/so")
@CrossOrigin(origins = "*")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @PostMapping("/search")
    public Response<?> searchSalesOrders(@RequestBody SalesOrderSearchRequest request) {
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
    public Response<?> createSalesOrder(@RequestBody SalesOrderCreateRequest request) {
        return salesOrderService.createSalesOrder(request);
    }

    @PostMapping("/edit")
    public Response<?> updateSalesOrder(@RequestBody SalesOrderCreateRequest request) {
        // 从请求中获取so_id
        String soId = request.getBasicInfo().getSo_id();
        if (!StringUtils.hasText(soId)) {
            return Response.error("so_id不能为空");
        }



        return salesOrderService.updateSalesOrder(soId, request);
    }

    /**
     * 物品批量查询
     * @param items 物品查询列表
     * @return 响应结果
     */
    @PostMapping("/items-tab-query")
    public SalesOrderResponse itemsTabQuery(@RequestBody List<SalesOrderItemsTabQueryRequest.ItemQuery> items) {
        log.info("物品批量查询请求，项目数: {}", items != null ? items.size() : 0);
        return salesOrderService.itemsTabQuery(items);
    }
}
