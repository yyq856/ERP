package webserver.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class Quotation {
    private Long quotationId; // 报价单ID
    private String soldToParty; // 售达方
    private String shipToParty; // 送达方
    private String customerReference; // 客户参考
    private Double netValue; // 净值
    private String currency; // 货币代码
    private LocalDate customerReferenceDate; // 客户参考日期
    private LocalDate reqDeliveryDate; // 请求交货日期
    private List<QuotationItem> items; // 报价单项目列表
}
