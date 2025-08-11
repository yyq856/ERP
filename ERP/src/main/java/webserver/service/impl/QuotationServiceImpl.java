package webserver.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.mapper.QuotationMapper;
import webserver.pojo.*;
import webserver.service.QuotationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationMapper quotationMapper;

    @Override
    @Transactional
    public QuotationData createQuotationFromInquiry(String inquiryId) {
        // 1. 新建报价单，返回报价单id（字符串）
        String quotationId = quotationMapper.insertQuotationFromInquiry(inquiryId);

        // 2. 查询报价单基础信息
        QuotationBasicInfo basicInfo = quotationMapper.getQuotationBasicInfo(quotationId);

        // 3. 查询报价单汇总信息（除items）
        QuotationItemOverview itemOverview = quotationMapper.getQuotationItemOverview(quotationId);

        // 4. 查询报价单条目列表
        List<QuotationItem> items = quotationMapper.getQuotationItems(quotationId);
        itemOverview.setItems(items);

        // 5. 组装返回对象
        QuotationData data = new QuotationData();
        data.setBasicInfo(basicInfo);
        data.setItemOverview(itemOverview);

        return data;
    }

    @Override
    public QuotationDetailsResponse getQuotationDetails(String salesQuotationId) {
        QuotationDetailsResponse.BasicInfo basicInfo = quotationMapper.getQuotationBasicInfo1(salesQuotationId);
        if (basicInfo == null) {
            throw new RuntimeException("Quotation not found: " + salesQuotationId);
        }

        QuotationDetailsResponse.Meta meta = new QuotationDetailsResponse.Meta();
        meta.setId(salesQuotationId);

        // 查询报价单明细
        List<QuotationDetailsResponse.Item> items = quotationMapper.getQuotationItems1(salesQuotationId);

        // 组装itemOverview，部分字段暂时为空字符串
        QuotationDetailsResponse.ItemOverview overview = new QuotationDetailsResponse.ItemOverview();
        overview.setReqDelivDate("");           // 业务未定义，空
        overview.setExpectOralVal("");          // 业务未定义，空
        overview.setExpectOralValUnit("");      // 业务未定义，空
        overview.setItems(items);

        QuotationDetailsResponse response = new QuotationDetailsResponse();
        response.setMeta(meta);
        response.setBasicInfo(basicInfo);
        response.setItemOverview(overview);

        return response;
    }

    @Override
    @Transactional
    public QuotationDetailsResponse updateQuotation(QuotationDetailsResponse quotation) {
        // 1. 更新报价单主表基本信息
        quotationMapper.updateQuotationBasicInfo(quotation.getBasicInfo());

        // 2. 删除该报价单的所有条目
        quotationMapper.deleteQuotationItems(quotation.getMeta().getId());

        // 3. 批量插入新的报价条目
        List<QuotationDetailsResponse.Item> items = quotation.getItemOverview().getItems();
        if (items != null && !items.isEmpty()) {
            quotationMapper.insertQuotationItems(quotation.getMeta().getId(), items);
        }

        // 4. 返回最新数据（调用查询接口）
        return getQuotationDetails(quotation.getMeta().getId());
    }
}
