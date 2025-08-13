package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliveryItemDTO {
    private String item;                 // item_no
    private String material;             // mat_id
    private String materialDescription = ""; // mat_desc 默认空
    private String deliveryQuantity = "0";  // 默认 "0"
    private String deliveryQuantityUnit = "EA"; // 默认 EA
    private String pickingQuantity = "0";    // 默认 "0"
    private String pickingQuantityUnit = "EA"; // 默认 EA
    private String pickingStatus = "Not Started"; // 默认
    private String confirmationStatus = "Not Confirmed"; // 默认
    private String salesOrder = "";       // 默认空
    private String itemType = "TAN";     // 默认
    private String originalDeliveryQuantity = "0 EA"; // 默认
    private String conversionRate = "1.000"; // 默认
    private String baseUnitDeliveryQuantity = "0 EA"; // 默认
    private String grossWeight = "0.0 KG"; // 默认
    private String netWeight = "0.0 KG";   // 默认
    private String volume = "0.0 M3";      // 默认
    private String plant = "1000";         // 默认
    private String storageLocation = "0001"; // 默认
    private String storageLocationDescription = ""; // 默认
    private String storageBin = "";        // 默认
    private String materialAvailability = ""; // 默认
}
