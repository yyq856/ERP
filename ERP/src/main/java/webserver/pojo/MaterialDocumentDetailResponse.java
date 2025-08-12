package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocumentDetailResponse {
    private boolean success;
    private String message;
    private MaterialDocumentResponseData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialDocumentResponseData {
        private MaterialDocumentDetail materialDocumentDetail;  // 物料凭证详细信息
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialDocumentDetail {
        private String materialDocument;      // 物料凭证号码
        private String plant;                 // 工厂名称
        private String postingDate;           // 过账日期 (ISO 8601格式)
        private String documentDate;          // 凭证日期 (ISO 8601格式)
        private String materialDocumentYear;  // 物料凭证年份
        private List<MaterialDocumentItemDetail> items;         // 物料凭证项目列表
        private List<ProcessFlowDetail> processFlow;            // 业务流程数据
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialDocumentItemDetail {
        private String item;                  // 项目编号
        private String material;              // 物料号码
        private String orderQuantity;         // 订单数量
        private String orderQuantityUnit;     // 订单数量单位
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessFlowDetail {
        private String dlvId;                 // 交货单ID (可选)
        private String materialDocument;      // 物料凭证ID
        private String billId;                // 会计凭证ID (可选)
    }
}
