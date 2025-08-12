package webserver.pojo;

import lombok.Data;

@Data
public class OrderSummary {
    private String id;
    private String plannedCreationDate;
    private String shipToParty;
    private String grossWeight;
}