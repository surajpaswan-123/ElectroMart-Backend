package electromart.ElectroMart.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long cartId;

    private List<CartItemResponse> items;

    private Integer itemCount;

    private Double subtotal;

    private Double total;
}


