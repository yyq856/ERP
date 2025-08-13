package webserver.pojo;

import lombok.Data;

@Data
public class OrderSummary {
    private String id;
    private String plannedCreationDate;
    private String plannedGIDate; // 计划发货日期，格式"yyyy-MM-dd"
    private String shippingPoint; // 发货处
    private String shipToParty;
    private String grossWeight;
}