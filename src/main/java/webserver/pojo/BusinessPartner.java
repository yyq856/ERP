package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessPartner {
    private Long customerId;
    private String title;
    private String name;
    private String language;
    private String street;
    private String city;
    private String region;
    private String postalCode;
    private String country;
    private String companyCode;
    private String reconciliationAccount;
    private String sortKey;
    private String salesOrg;
    private Integer channel;
    private String division;
    private String currency;
    private String salesDistrict;
    private String priceGroup;
    private String customerGroup;
    private String deliveryPriority;
    private String shippingCondition;
    private Long deliveringPlant;
    private Integer maxPartDeliv;
    private String incoterms;
    private String incotermsLocation;
    private String paymentTerms;
    private String acctAssignment;
    private Integer outputTax;
}
