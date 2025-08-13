package webserver.service;

import webserver.common.Response;
import webserver.pojo.ItemsTabQueryRequest;
import webserver.pojo.ValidateItemsResponse;

import java.util.List;

public interface ItemsService {
    Response<ValidateItemsResponse> itemsTabQuery(List<ItemsTabQueryRequest.ItemInput> items);
}

