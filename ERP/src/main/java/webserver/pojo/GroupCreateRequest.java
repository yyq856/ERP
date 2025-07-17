package webserver.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {
    private Test test;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Test {
        private BpIdAndRoleSection bpIdAndRoleSection;
        private Name name;
        private SearchTerms searchTerms;
        private Address address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BpIdAndRoleSection {
        @JsonProperty("customer_id")
        private String customerId;
        @JsonProperty("bp_role")
        private String bpRole;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Name {
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
}
