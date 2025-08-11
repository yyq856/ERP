package webserver.pojo;

import lombok.Data;

@Data
public class SalesOrdersRequest {
    private String shipToParty;         // 客户编号
    private String plannedCreationDate; // 计划创建日期，格式"yyyy-MM-dd"
    private String relevantForTM;       // 业务字段，暂时不用
}