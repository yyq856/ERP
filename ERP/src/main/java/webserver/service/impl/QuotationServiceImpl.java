package webserver.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.mapper.QuotationMapper;
import webserver.pojo.*;
import webserver.common.Response;
import webserver.service.QuotationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationMapper quotationMapper;

    @Override
    @Transactional
    public Response<QuotationResponseDTO1> createQuotationFromInquiry(CreateQuotationFromInquiryRequest request) {
        try {
            // 1. 获取 inquiry 信息
            var inquiry = quotationMapper.selectInquiryById(request.getInquiryId());
            var inquiryItems = quotationMapper.selectInquiryItemsById(request.getInquiryId());

            // 2. 插入 quotation 主表
            quotationMapper.insertQuotationFromInquiry(inquiry);

            // 获取新 quotation_id
            Long quotationId = quotationMapper.getLastInsertId();

            // 3. 插入 quotation item 表
            quotationMapper.insertQuotationItemsFromInquiry(quotationId, inquiryItems);

            // 4. 组装返回 DTO
            QuotationDetailsResponseDTO resp = buildResponseDTO(quotationId, inquiry, inquiryItems);
            QuotationResponseDTO1 response = new QuotationResponseDTO1();
            response.setQuotationData(resp);
            return Response.success(response);

        } catch (Exception e) {
            return Response.error("Quotation creation failed, please try again later.");
        }
    }

    private QuotationDetailsResponseDTO buildResponseDTO(Long quotationId, InquiryDTO inquiry, List<InquiryItemDTO> inquiryItems) {
        QuotationDetailsResponseDTO response = new QuotationDetailsResponseDTO();

        // meta
        QuotationDetailsResponseDTO.Meta meta = new QuotationDetailsResponseDTO.Meta();
        meta.setId(String.valueOf(quotationId));
        response.setMeta(meta);

        // basicInfo
        QuotationDetailsResponseDTO.BasicInfo basicInfo = new QuotationDetailsResponseDTO.BasicInfo();
        basicInfo.setQuotation(String.valueOf(quotationId));
        basicInfo.setSoldToParty(String.valueOf(inquiry.getSoldTp()));
        basicInfo.setShipToParty(String.valueOf(inquiry.getShipTp()));
        basicInfo.setCustomerReference(inquiry.getCustRef());
        basicInfo.setCustomerReferenceDate(inquiry.getCustomerReferenceDate().toString());
        basicInfo.setNetValue(inquiry.getNetValue());
        basicInfo.setNetValueUnit("USD"); // 可根据需求调整
        response.setBasicInfo(basicInfo);

        // itemOverview
        QuotationDetailsResponseDTO.ItemOverview itemOverview = new QuotationDetailsResponseDTO.ItemOverview();
        itemOverview.setValidFrom(inquiry.getValidFromDate().toString());
        itemOverview.setValidTo(inquiry.getValidToDate().toString());
        itemOverview.setReqDelivDate(inquiry.getValidFromDate().toString());
        itemOverview.setExpectedOralVal("0");        // 示例，可根据需求改
        itemOverview.setExpectedOralValUnit("USD");  // 示例

        List<QuotationItemDTO> items = new ArrayList<>();
        /*
         */
        for (InquiryItemDTO inItem : inquiryItems) {
            QuotationItemDTO dto = new QuotationItemDTO();
            dto.setItem(String.valueOf(inItem.getItemNo()));
            dto.setMaterial(String.valueOf(inItem.getMatId()));
            dto.setOrderQuantity(String.valueOf(inItem.getQuantity()));
            dto.setOrderQuantityUnit(inItem.getSu());
            dto.setDescription("");
            dto.setSu(inItem.getSu());
            dto.setAltItm(0); // 示例
            items.add(dto);
        }
        itemOverview.setItems(items);

        response.setItemOverview(itemOverview);

        return response;
    }

    @Override
    public QuotationResponseDTO1 getQuotationDetails(String quotationId) {
        QuotationDTO quotation = quotationMapper.selectQuotationById(quotationId);
        if (quotation == null) {
            throw new RuntimeException("Quotation not found");
        }

        List<QuotationItemEntity> quotationItems =
                quotationMapper.selectQuotationItemsByQuotationId(quotationId);

        QuotationDetailsResponseDTO details = buildResponseDTO1(quotation, quotationItems);

        // 外层包装
        QuotationResponseDTO1 response = new QuotationResponseDTO1();
        response.setQuotationData(details);

        return response;
    }

    private QuotationDetailsResponseDTO buildResponseDTO1(QuotationDTO quotation, List<QuotationItemEntity> items) {
        QuotationDetailsResponseDTO response = new QuotationDetailsResponseDTO();

        // meta
        QuotationDetailsResponseDTO.Meta meta = new QuotationDetailsResponseDTO.Meta();
        meta.setId(String.valueOf(quotation.getQuotationId()));
        response.setMeta(meta);

        // basicInfo
        QuotationDetailsResponseDTO.BasicInfo basicInfo = new QuotationDetailsResponseDTO.BasicInfo();
        if (quotation.getReferenceInquiryId() != null) {
            basicInfo.setInquiry(String.valueOf(quotation.getReferenceInquiryId()));
        }
        basicInfo.setQuotation(String.valueOf(quotation.getQuotationId()));
        basicInfo.setSoldToParty(String.valueOf(quotation.getSoldTp()));
        basicInfo.setShipToParty(String.valueOf(quotation.getShipTp()));
        basicInfo.setCustomerReference(quotation.getCustRef() != null ? quotation.getCustRef() : "");
        basicInfo.setNetValue(quotation.getNetValue() != null ? quotation.getNetValue() : 0);
        basicInfo.setNetValueUnit(quotation.getCurrency() != null ? quotation.getCurrency() : "USD");
        if (quotation.getCustomerReferenceDate() != null) {
            basicInfo.setCustomerReferenceDate(
                    new SimpleDateFormat("yyyy-MM-dd").format(quotation.getCustomerReferenceDate())
            );
        }
        response.setBasicInfo(basicInfo);

        // itemOverview
        QuotationDetailsResponseDTO.ItemOverview itemOverview = new QuotationDetailsResponseDTO.ItemOverview();
        if (quotation.getValidFromDate() != null) {
            itemOverview.setValidFrom(new SimpleDateFormat("yyyy-MM-dd").format(quotation.getValidFromDate()));
        }
        if (quotation.getValidToDate() != null) {
            itemOverview.setValidTo(new SimpleDateFormat("yyyy-MM-dd").format(quotation.getValidToDate()));
        }
        itemOverview.setReqDelivDate("");
        itemOverview.setExpectedOralVal("");
        itemOverview.setExpectedOralValUnit("");

        List<QuotationItemDTO> itemDTOs = new ArrayList<>();
        for (QuotationItemEntity item : items) {
            QuotationItemDTO dto = new QuotationItemDTO();
            dto.setItem(String.valueOf(item.getItemNo()));
            dto.setMaterial(String.valueOf(item.getMatId()));
            dto.setOrderQuantity(String.valueOf(item.getQuantity()));
            dto.setOrderQuantityUnit(item.getSu());
            dto.setDescription("");
            dto.setSu(item.getSu() != null && !item.getSu().isEmpty() ? item.getSu() : "0");
            dto.setAltItm(0);
            itemDTOs.add(dto);
        }
        itemOverview.setItems(itemDTOs);

        response.setItemOverview(itemOverview);

        return response;
    }

    @Override
    public QuotationResponseDTO1 updateQuotation(QuotationResponseDTO1 request) {
        QuotationDetailsResponseDTO quotation = request.getQuotationData();

        if (quotation.getMeta() == null || quotation.getMeta().getId() == null) {
            throw new RuntimeException("Quotation ID cannot be null");
        }

        Long quotationId = Long.parseLong(quotation.getMeta().getId());

        // 更新主表
        quotationMapper.updateQuotation(quotationId, quotation.getBasicInfo(), quotation.getItemOverview());

        // 更新明细
        List<QuotationItemDTO> items = quotation.getItemOverview().getItems();
        if (items != null && !items.isEmpty()) {
            for (QuotationItemDTO item : items) {
                if(item.getItem()!=null && !item.getItem().equals("")) quotationMapper.updateQuotationItem(quotationId, item);
            }
        }

        // 返回最新数据（仍然保持外层包装）
        QuotationResponseDTO1 response = new QuotationResponseDTO1();
        response.setQuotationData(quotation);

        return response;
    }


    @Override
    public QuotationSearchResponseDTO searchQuotations(QuotationSearchRequestDTO request) {
        List<Map<String, Object>> results = quotationMapper.searchQuotations(request);

        QuotationSearchResponseDTO response = new QuotationSearchResponseDTO();
        QuotationSearchResponseDTO.QuotationStruct struct = new QuotationSearchResponseDTO.QuotationStruct();

        List<QuotationSearchResponseDTO.QuotationNode> currentValue = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Map<String, Object> row : results) {
            QuotationSearchResponseDTO.QuotationNode node = new QuotationSearchResponseDTO.QuotationNode();
            node.setSalesQuotation(String.valueOf(row.get("salesQuotation")));
            node.setSoldToParty(String.valueOf(row.get("soldToParty")));
            node.setCustomerReference(String.valueOf(row.get("customerReference")));
            node.setOverallStatus(String.valueOf(row.get("overallStatus")));
            Object exp = row.get("latestExpiration");
            node.setLatestExpiration(exp != null ? sdf.format(exp) : "");
            currentValue.add(node);
        }

        struct.setCurrentValue(currentValue);
        struct.setConfig(new Object());
        struct.setEditable(false);

        // children 配置固定
        List<QuotationSearchResponseDTO.QuotationChildNode> children = new ArrayList<>();
        children.add(createChildNode("number", "leaf", "salesQuotation", false, null));
        children.add(createChildNode("string", "leaf", "soldToParty", true, null));
        children.add(createChildNode("string", "leaf", "customerReference", false, null));
        children.add(createChildNode("selection", "leaf", "overallStatus", true,
                Map.of("options", new String[]{"New","Open","In Process","Completed"})));
        children.add(createChildNode("date", "leaf", "latestExpiration", false, null));
        struct.setChildren(children);

        response.setQuotationStruct(struct);
        return response;
    }

    private QuotationSearchResponseDTO.QuotationChildNode createChildNode(String varType, String nodeType, String name,
                                                                          boolean isEditable, Object config) {
        QuotationSearchResponseDTO.QuotationChildNode child = new QuotationSearchResponseDTO.QuotationChildNode();
        child.setVarType(varType);
        child.setNodeType(nodeType);
        child.setName(name);
        child.setEditable(isEditable);
        child.setConfig(config);
        return child;
    }
}
