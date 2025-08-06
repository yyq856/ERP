package webserver.service;

import webserver.common.Response;
import webserver.pojo.*;

import java.util.List;

public interface OutboundDeliveryService {
    Response<CreateOutboundDeliveryResponseData> createFromOrders(CreateOutboundDeliveryRequest request);
    Response getOutboundDeliverySummaries(GetOutboundDeliverySummaryRequest request);
    Response<OutboundDeliveryDetailDTO> getOutboundDeliveryDetail(String deliveryId);
    Response<ValidateItemsResponse> validateAndCompleteDeliveryItems(List<OutboundDeliveryItemDTO> items);
    Response<?> postGIsById(PostGIsByIdRequest request);
    Response<?> postGIs(List<PostGIsRequest> requests);
}
