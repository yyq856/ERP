package webserver.service;

import webserver.pojo.MaterialInfoResponse;
import webserver.pojo.SearchStockResponseData;

import java.util.List;

public interface StockService {
    List<String> getStockStages();
    List<String> getStockLevels();
    MaterialInfoResponse getMaterialInfo(String materialId);
    SearchStockResponseData searchStock(String materialId);
}
