package webserver.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.mapper.QuotationMapper;
import webserver.pojo.QuotationBasicInfo;
import webserver.pojo.QuotationData;
import webserver.pojo.QuotationItem;
import webserver.pojo.QuotationItemOverview;
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
}
