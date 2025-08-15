package webserver.controller;

import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.*;
import webserver.service.OutboundDeliveryService;
import webserver.service.SalesOrderService;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/app/outbound-delivery")
public class OutboundDeliveryController {

    private final SalesOrderService salesOrderService;
    private final OutboundDeliveryService outboundDeliveryService;

    public OutboundDeliveryController(SalesOrderService salesOrderService, OutboundDeliveryService outboundDeliveryService) {
        this.salesOrderService = salesOrderService;
        this.outboundDeliveryService = outboundDeliveryService;
    }

    @PostMapping("/get-sales-orders")
    public SalesOrdersResponse getSalesOrders(@RequestBody SalesOrdersRequest request) {
        return salesOrderService.getSalesOrders(request);
    }

    @PostMapping("/create-from-orders")
    public Response<CreateOutboundDeliveryResponseData> createFromOrders(@RequestBody CreateOutboundDeliveryRequest request) {
        return outboundDeliveryService.createFromOrders(request);
    }

    @PostMapping("/get-deliveries-summary")
    public Response<?> getOutboundDeliverySummaries(@RequestBody GetOutboundDeliverySummaryRequest request) {
        return outboundDeliveryService.getOutboundDeliverySummaries(request);
    }

    @PostMapping("/get-detail")
    public Response<OutboundDeliveryDetailResponse> getDeliveryDetail(@RequestBody GetOutboundDeliveryDetailRequest request) {
        return outboundDeliveryService.getOutboundDeliveryDetail(request.getDeliveryId());
    }

    @PostMapping("/items-tab-query")
    public Response<ValidateItemsResponse.ValidateItemsData> validateItems(@RequestBody List<OutboundDeliveryItemDTO> requestItems) {
        Response<ValidateItemsResponse> serviceResponse = outboundDeliveryService.validateAndCompleteDeliveryItems(requestItems);
        if (serviceResponse.isSuccess()) {
            return Response.success(serviceResponse.getData().getData());
        } else {
            return Response.error(serviceResponse.getMessage());
        }
    }

    @PostMapping("/post-gis-by-id")
    public Response<?> postGIsById(@RequestBody PostGIsByIdRequest request) {
        return outboundDeliveryService.postGIsById(request);
    }

    @PostMapping("/post-gis")
    public Response<?> postGIs(@RequestBody List<PostGIsRequest> requests) {
        return outboundDeliveryService.postGIs(requests);
    }
}
