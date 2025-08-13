package webserver.service;

import webserver.common.Response;
import webserver.pojo.MaterialInfoResponse;
import webserver.pojo.SearchStockRequest;
import webserver.pojo.SearchStockResponseData;

import java.util.List;

public interface StockService {
    MaterialInfoResponse getMaterialInfo(String materialId);
    Response<SearchStockResponseData> searchStock(SearchStockRequest request);
}
