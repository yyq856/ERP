package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报价单查询请求类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationGetRequest {
    private String quotationId;  // 报价单ID
}
