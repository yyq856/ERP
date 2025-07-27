package webserver.pojo;

import lombok.Data;

import java.time.LocalDate;


@Data
public class SalesOrder {
    private Long soId;
    private Long quotationId;
    private Long customerId;
    private Long contactId;
    private LocalDate reqDeliveryDate;
    private String currency;
    private Double netValue;
    private Double taxValue;
    private Double grossValue;
    private String incoterms;
    private String paymentTerms;
    // Getters and Setters
}
