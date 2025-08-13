package webserver.pojo;

import lombok.Data;

@Data
public class SalesOrdersRequest {
    private String shipToParty;         // 客户编号
    private String plannedCreationDate; // 计划创建日期，格式"yyyy-MM-dd"
    private String plannedGIDate; // 计划发货日期，格式"yyyy-MM-dd"
    private String shippingPoint; // 发货处
    private String relevantForTM;       // 业务字段，暂时不用
}