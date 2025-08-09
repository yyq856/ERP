package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.*;
import webserver.service.InquiryService;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/app")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    /**
     * 初始化询价单
     * @param request 初始化请求
     * @return 初始化结果
     */
    @PostMapping("/inquiry/initialize")
    public InquiryResponse initialize(@RequestBody InquiryInitializeRequest request) {
        log.info("询价单初始化请求: {}", request.getInquiryType());
        return inquiryService.initialize(request);
    }

    /**
     * 获取询价单详情
     * @param request 查询请求
     * @return 查询结果
     */
    @PostMapping("/inquiry/get")
    public InquiryResponse get(@RequestBody InquiryGetRequest request) {
        log.info("询价单查询请求: {}", request.getInquiryId());
        return inquiryService.get(request);
    }

    /**
     * 编辑询价单
     * @param request 编辑请求
     * @return 编辑结果
     */
    @PostMapping("/inquiry/edit")
    public InquiryResponse edit(@RequestBody InquiryEditRequest request) {
        log.info("询价单编辑请求");
        return inquiryService.edit(request);
    }

    /**
     * 物品批量查询
     * @param items 物品查询列表
     * @return 查询结果
     */
    @PostMapping("/inquiry/items-tab-query")
    public InquiryResponse itemsTabQuery(@RequestBody List<InquiryItemsTabQueryRequest.ItemQuery> items) {
        log.info("物品批量查询请求，项目数: {}", items != null ? items.size() : 0);
        return inquiryService.itemsTabQuery(items);
    }
}
