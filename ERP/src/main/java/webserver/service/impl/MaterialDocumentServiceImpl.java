package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.MaterialDocumentMapper;
import webserver.pojo.*;
import webserver.service.MaterialDocumentService;
import webserver.util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MaterialDocumentServiceImpl implements MaterialDocumentService {

    @Autowired
    private MaterialDocumentMapper materialDocumentMapper;

    @Override
    public MaterialDocumentSearchResponse searchMaterialDocuments(MaterialDocumentSearchRequest request) {
        try {
            log.info("搜索物料凭证，原始条件: {}", request);
            
            // 验证和转换日期格式
            MaterialDocumentSearchRequest processedRequest = processSearchRequest(request);
            log.info("处理后的搜索条件: {}", processedRequest);
            
            // 执行搜索
            List<MaterialDocumentSearchResponse.MaterialDocumentSummary> summaries =
                materialDocumentMapper.searchMaterialDocuments(processedRequest);
            
            log.info("搜索到 {} 条物料凭证记录", summaries != null ? summaries.size() : 0);
            
            if (summaries != null && !summaries.isEmpty()) {
                log.debug("搜索结果详情: {}", summaries);
            } else {
                log.warn("搜索结果为空，检查数据库数据或查询条件");
            }
            
            return new MaterialDocumentSearchResponse(true, "搜索成功", summaries != null ? summaries : new ArrayList<>());
            
        } catch (Exception e) {
            log.error("搜索物料凭证失败: {}", e.getMessage(), e);
            return new MaterialDocumentSearchResponse(false, "搜索失败: " + e.getMessage(), new ArrayList<>());
        }
    }

    @Override
    public MaterialDocumentDetailResponse getMaterialDocumentDetail(Long materialDocumentId) {
        try {
            log.info("查询物料凭证详情，ID: {}", materialDocumentId);
            
            // 验证参数
            if (materialDocumentId == null) {
                return new MaterialDocumentDetailResponse(false, "物料凭证ID不能为空", null);
            }
            
            // 查询物料凭证基础信息
            MaterialDocument materialDocument = materialDocumentMapper.getMaterialDocumentById(materialDocumentId);
            if (materialDocument == null) {
                return new MaterialDocumentDetailResponse(false, "物料凭证不存在", null);
            }
            
            // 查询物料凭证项目
            List<MaterialDocumentItem> items = materialDocumentMapper.getMaterialDocumentItems(materialDocumentId);
            
            // 查询业务流程关联信息
            MaterialDocumentProcess processFlow = materialDocumentMapper.getMaterialDocumentProcess(materialDocumentId);
            
            // 构建响应数据（扁平 data 结构）
            MaterialDocumentDetailResponse.MaterialDocumentDetail detail = buildDetailResponse(
                materialDocument, items, processFlow);

            log.info("物料凭证详情查询成功，ID: {}", materialDocumentId);
            return new MaterialDocumentDetailResponse(true, "查询成功", detail);
            
        } catch (Exception e) {
            log.error("查询物料凭证详情失败: {}", e.getMessage(), e);
            return new MaterialDocumentDetailResponse(false, "查询失败: " + e.getMessage(), null);
        }
    }

    @Override
    public MaterialDocumentDetailResponse getMaterialDocumentDetail(String materialDocument) {
        try {
            if (!StringUtils.hasText(materialDocument)) {
                return new MaterialDocumentDetailResponse(false, "物料凭证标识不能为空", null);
            }

            // 如果是纯数字，直接当作主键ID查询
            Long id = null;
            try {
                id = Long.parseLong(materialDocument);
            } catch (NumberFormatException ignore) {
                // 非数字：按业务号查询其对应的ID
                id = materialDocumentMapper.findIdByDocumentCode(materialDocument);
            }

            if (id == null) {
                return new MaterialDocumentDetailResponse(false, "未找到对应的物料凭证", null);
            }

            return getMaterialDocumentDetail(id);
        } catch (Exception e) {
            log.error("按标识查询物料凭证详情失败: {}", e.getMessage(), e);
            return new MaterialDocumentDetailResponse(false, "查询失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 处理搜索请求，转换日期格式
     */
    private MaterialDocumentSearchRequest processSearchRequest(MaterialDocumentSearchRequest request) {
        if (request == null) {
            return new MaterialDocumentSearchRequest();
        }
        
        MaterialDocumentSearchRequest processed = new MaterialDocumentSearchRequest();
        processed.setMaterialDocument(request.getMaterialDocument());
        processed.setPlant(request.getPlant());
        processed.setMaterialDocumentYear(request.getMaterialDocumentYear());
        processed.setMaterial(request.getMaterial());
        
        // 处理过账日期
        if (StringUtils.hasText(request.getPostingDate())) {
            try {
                LocalDate postingDate = DateUtil.parseDate(request.getPostingDate());
                processed.setPostingDate(postingDate.toString()); // 转换为 yyyy-MM-dd 格式
            } catch (Exception e) {
                log.warn("过账日期格式不正确，将被忽略: {}", request.getPostingDate());
                processed.setPostingDate(null);
            }
        }
        
        // 处理凭证日期
        if (StringUtils.hasText(request.getDocumentDate())) {
            try {
                LocalDate documentDate = DateUtil.parseDate(request.getDocumentDate());
                processed.setDocumentDate(documentDate.toString()); // 转换为 yyyy-MM-dd 格式
            } catch (Exception e) {
                log.warn("凭证日期格式不正确，将被忽略: {}", request.getDocumentDate());
                processed.setDocumentDate(null);
            }
        }
        
        return processed;
    }
    
    /**
     * 构建详情响应数据
     */
    private MaterialDocumentDetailResponse.MaterialDocumentDetail buildDetailResponse(
            MaterialDocument materialDocument,
            List<MaterialDocumentItem> items,
            MaterialDocumentProcess processFlow) {
        // 构建物料凭证详细信息
        MaterialDocumentDetailResponse.MaterialDocumentDetail materialDocumentDetail =
            new MaterialDocumentDetailResponse.MaterialDocumentDetail();
        
        // 设置基础信息
        materialDocumentDetail.setMaterialDocument(materialDocument.getMaterialDocument());
        materialDocumentDetail.setPlant(materialDocument.getPlantName() != null ? materialDocument.getPlantName() : "");
        materialDocumentDetail.setMaterialDocumentYear(materialDocument.getMaterialDocumentYear());
        
        // 格式化日期为ISO 8601格式
        if (materialDocument.getPostingDate() != null) {
            materialDocumentDetail.setPostingDate(materialDocument.getPostingDate().toString());
        }
        if (materialDocument.getDocumentDate() != null) {
            materialDocumentDetail.setDocumentDate(materialDocument.getDocumentDate().toString());
        }
        
        // 转换物料凭证项目
        List<MaterialDocumentDetailResponse.MaterialDocumentItemDetail> itemDetails =
            items.stream().map(this::convertToItemDetail).collect(Collectors.toList());
        materialDocumentDetail.setItems(itemDetails);
        
        // 转换业务流程信息
        List<MaterialDocumentDetailResponse.ProcessFlowDetail> processFlowDetails =
            convertToProcessFlowDetails(materialDocument, processFlow);
        materialDocumentDetail.setProcessFlow(processFlowDetails);
        
    return materialDocumentDetail;
    }
    
    /**
     * 转换物料凭证项目
     */
    private MaterialDocumentDetailResponse.MaterialDocumentItemDetail convertToItemDetail(MaterialDocumentItem item) {
        MaterialDocumentDetailResponse.MaterialDocumentItemDetail detail = 
            new MaterialDocumentDetailResponse.MaterialDocumentItemDetail();
        
        detail.setItem(item.getItemNo() != null ? item.getItemNo().toString() : "");
        detail.setMaterial(item.getMatId() != null ? item.getMatId().toString() : "");
        detail.setOrderQuantity(item.getQuantity() != null ? item.getQuantity().toString() : "0");
        detail.setOrderQuantityUnit(item.getUnit() != null ? item.getUnit() : "");
        
        return detail;
    }
    
    /**
     * 转换业务流程信息
     */
    private List<MaterialDocumentDetailResponse.ProcessFlowDetail> convertToProcessFlowDetails(
            MaterialDocument materialDocument, MaterialDocumentProcess processFlow) {
        
        List<MaterialDocumentDetailResponse.ProcessFlowDetail> processFlowDetails = new ArrayList<>();
        
        // 基础物料凭证信息
        MaterialDocumentDetailResponse.ProcessFlowDetail materialDocDetail = 
            new MaterialDocumentDetailResponse.ProcessFlowDetail();
        materialDocDetail.setMaterialDocument(materialDocument.getMaterialDocumentId().toString());
        materialDocDetail.setDlvId(null);
        materialDocDetail.setBillId(null);
        processFlowDetails.add(materialDocDetail);
        
        // 如果有关联的业务流程信息
        if (processFlow != null) {
            if (processFlow.getDlvId() != null) {
                MaterialDocumentDetailResponse.ProcessFlowDetail dlvDetail = 
                    new MaterialDocumentDetailResponse.ProcessFlowDetail();
                dlvDetail.setDlvId(processFlow.getDlvId().toString());
                dlvDetail.setMaterialDocument(materialDocument.getMaterialDocumentId().toString());
                dlvDetail.setBillId(null);
                processFlowDetails.add(dlvDetail);
            }
            
            if (processFlow.getBillId() != null) {
                MaterialDocumentDetailResponse.ProcessFlowDetail billDetail = 
                    new MaterialDocumentDetailResponse.ProcessFlowDetail();
                billDetail.setDlvId(null);
                billDetail.setMaterialDocument(materialDocument.getMaterialDocumentId().toString());
                billDetail.setBillId(processFlow.getBillId().toString());
                processFlowDetails.add(billDetail);
            }
        }
        
        return processFlowDetails;
    }
}