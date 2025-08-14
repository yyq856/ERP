package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报价单初始化请求类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationInitializeRequest {
    private String quotationType;        // 报价单类型
    private String salesOrganization;    // 销售组织
    private String distributionChannel;  // 分销渠道
    private String division;             // 产品组
}
