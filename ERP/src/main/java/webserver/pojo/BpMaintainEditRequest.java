package webserver.pojo;

import lombok.Data;

@Data
public class BpMaintainEditRequest {
    private BpIdAndRoleSection bpIdAndRoleSection;
    private NameSection name;
    private SearchTermsSection searchTerms;
    private AddressSection address;
    
    @Data
    public static class BpIdAndRoleSection {
        private String customerId;
        private String bpRole;
        private String type; // person/org/group
    }
    
    @Data
    public static class NameSection {
        private String title;
        private String name;
        private String firstName;  // person类型
        private String lastName;   // person类型
    }
    
    @Data
    public static class SearchTermsSection {
        private String searchTerm;
    }
    
    @Data
    public static class AddressSection {
        private String country;
        private String street;
        private String postalCode;
        private String city;
    }
}
