package webserver.pojo;

import lombok.Data;


@Data
public class SalesOrderCreateResponse {
    private boolean success;
    private String message;
    private Data data;

    // Getters and Setters
    @lombok.Data
    public static class Data {
        private String so_id;
        // Getters and Setters
    }
}
