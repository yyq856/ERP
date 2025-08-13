package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class QuotationResponseDTO1 {
    private QuotationDetailsResponseDTO quotation; // 外层包一层 quotation
}