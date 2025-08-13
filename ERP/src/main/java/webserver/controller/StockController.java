package webserver.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.MaterialInfoRequest;
import webserver.pojo.MaterialInfoResponse;
import webserver.pojo.SearchStockRequest;
import webserver.pojo.SearchStockResponseData;
import webserver.service.StockService;

import java.util.Map;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping("/materialInfo")
    public Response<MaterialInfoResponse> getMaterialInfo(@RequestBody MaterialInfoRequest request) {
        MaterialInfoResponse info = stockService.getMaterialInfo(request.getId());
        return Response.success(info);
    }

    @PostMapping("/searchStock")
    public Response<SearchStockResponseData> searchStock(@RequestBody SearchStockRequest request) {
        return stockService.searchStock(request);
    }
}
