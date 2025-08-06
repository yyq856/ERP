package webserver.pojo;

import lombok.Data;

@Data
public class SalesOrdersRequest {
    private String shipToParty;         // 客户编号
    private String plannedCreationDate; // 计划创建日期（字符串格式）
    private String relevantForTM;       // 相关字段，可根据业务定义
}
