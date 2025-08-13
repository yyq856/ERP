package webserver.pojo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SalesOrder {
    private Long soId;
    private Long quotationId;
    private Long customerId;        // 旧字段，保持兼容性
    private Long contactId;         // 旧字段，保持兼容性
    private Long soldTp;            // 新增：售达方客户ID
    private Long shipTp;            // 新增：送达方客户ID
    private LocalDate reqDeliveryDate;
    private LocalDate docDate;
    private String currency;
    private double netValue;
    private double taxValue;
    private double grossValue;
    private String incoterms;
    private String paymentTerms;
    private String status;
    private String customerReference;
    private LocalDate customerReferenceDate;
}
