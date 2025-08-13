package webserver.service;

import webserver.pojo.ItemValidationRequest;
import webserver.pojo.ItemValidationResponse;
import webserver.pojo.ItemsTabQueryRequest;
import webserver.pojo.ItemsTabQueryResponse;
import webserver.pojo.ValidateItemsRequest;
import webserver.pojo.ValidateItemsResponse;

import java.util.List;

public interface ValidateItemsService {
    
    /**
     * 物品验证服务接口 - 按照接口规范实现
     * @param request 物品验证请求列表
     * @return 验证结果和计算后的定价信息
     */
    ItemValidationResponse validateItems(List<ItemValidationRequest> request);
    
    /**
     * 批量验证和计算物品的定价信息（原有方法，保持兼容）
     * @param request 物品验证请求列表
     * @return 验证结果和计算后的定价信息
     */
    ValidateItemsResponse validateAndCalculateItems(List<ValidateItemsRequest> request);
    
    /**
     * 处理物品条件查询请求
     * @param request 物品条件查询请求列表
     * @param applicationType 应用类型（inquiry/quotation/so/billing）
     * @return 查询结果和计算后的定价信息
     */
    ItemsTabQueryResponse processItemsTabQuery(List<ItemsTabQueryRequest> request, String applicationType);
}