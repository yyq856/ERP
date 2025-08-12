package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliveryItemDTO {
    private String item;                 // item_no
    private String material;             // mat_id
    private String materialDescription; // mat_desc
    private float pickingQuantity;    // pick_quantity
    private String plant;                // plant_id
    private String plantName;            // plant_name
    private String storageLocation;      // storage_loc
    private String storageLocationDescription;      // storage_loc_name
}
