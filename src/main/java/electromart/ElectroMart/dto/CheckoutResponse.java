package electromart.ElectroMart.dto;

import electromart.ElectroMart.entity.Order;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutResponse {
    private Order order;
    private String transactionId;
    private String paymentStatus;
    private Double subtotal;
    private Double discount;
    private Double shipping;
    private Double total;
}
