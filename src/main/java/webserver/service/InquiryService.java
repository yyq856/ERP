package webserver.service;

import webserver.pojo.*;

import java.util.List;

public interface InquiryService {
    
    /**
     * 初始化询价单
     * @param request 初始化请求
     * @return 响应结果
     */
    InquiryResponse initialize(InquiryInitializeRequest request);
    
    /**
     * 获取询价单详情
     * @param request 查询请求
     * @return 响应结果
     */
    InquiryResponse get(InquiryGetRequest request);
    
    /**
     * 编辑询价单
     * @param request 编辑请求
     * @return 响应结果
     */
    InquiryResponse edit(InquiryEditRequest request);
    
    /**
     * 物品批量查询
     * @param items 物品查询列表
     * @return 响应结果
     */
    InquiryResponse itemsTabQuery(List<InquiryItemsTabQueryRequest.ItemQuery> items);
}
