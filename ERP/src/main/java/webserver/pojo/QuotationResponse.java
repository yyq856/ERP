package webserver.pojo;

import lombok.Data;
import java.util.List;

/**
 * 报价单响应结构
 */
@Data
public class QuotationResponse {
    private boolean success;
    private String message;
    private Object data;

    /**
     * 成功响应
     * @param data 数据
     * @param message 消息
     * @return 响应对象
     */
    public static QuotationResponse success(Object data, String message) {
        QuotationResponse response = new QuotationResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 成功响应 - 默认消息
     * @param data 数据
     * @return 响应对象
     */
    public static QuotationResponse success(Object data) {
        return success(data, "操作成功");
    }

    /**
     * 错误响应
     * @param message 错误消息
     * @return 响应对象
     */
    public static QuotationResponse error(String message) {
        QuotationResponse response = new QuotationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    // 保留原有的内部类以保持向后兼容
    private QuotationData quotationData;

    @Data
    public static class QuotationData {
        private QuotationDetail quotationData;
    }

    @Data
    public static class QuotationDetail {
        private Meta meta;
        private BasicInfo basicInfo;
        private ItemOverview itemOverview;
    }

    @Data
    public static class Meta {
        private String id;
    }

    @Data
    public static class BasicInfo {
        private String quotation; // 报价单号字段
        private String soldToParty; // 售达方
        private String shipToParty; // 送达方
        private String customerReference; // 客户参考
        private String netValue; // 净值
        private String netValueUnit; // 净值单位
        private String customerReferenceDate; // 客户参考日期
    }

    @Data
    public static class ItemOverview {
        private String reqDelivDate; // 请求交货日期
        private List<Item> items; // 报价单项目列表
    }

    @Data
    public static class Item {
        private String item; // 项目号
        private String material; // 物料号
        private String orderQuantity; // 订单数量
        private String orderQuantityUnit; // 订单数量单位
        private String description; // 描述
        private String reqDelivDate; // 请求交货日期
        private String netValue; // 净值
        private String netValueUnit; // 净值单位
        private String taxValue; // 税值
        private String taxValueUnit; // 税值单位
        private String pricingDate; // 定价日期
        private String orderProbability; // 订单概率
        private List<?> pricingElements; // 定价元素列表
    }
}
