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
    private String firstName;      // 新增：person类型名字
    private String lastName;       // 新增：person类型姓氏
    private String bpType;         // 新增：业务伙伴类型 person/org/group
    private String searchTerm;     // 新增：搜索词
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
