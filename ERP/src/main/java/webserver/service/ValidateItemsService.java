package webserver.service;

import webserver.pojo.ValidateItemsRequest;
import webserver.pojo.ValidateItemsResponse;

import java.util.List;

public interface ValidateItemsService {
    
    /**
     * 批量验证和计算物品的定价信息
     * @param request 物品验证请求列表
     * @return 验证结果和计算后的定价信息
     */
    ValidateItemsResponse validateAndCalculateItems(List<ValidateItemsRequest> request);
}