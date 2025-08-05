package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpCreateRequest {
    private Customer customer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        private BpIdAndRoleSection bpIdAndRoleSection;
        private Name name;
        private SearchTerms searchTerms;
        private Address address;
        private CustomerInfo customer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BpIdAndRoleSection {
        private String customerId;
        private String bpRole;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Name {
        private String title;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchTerms {
        private String searchTerm;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String country;
        private String street;
        private String postalCode;
        private String city;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String type;
    }
}
