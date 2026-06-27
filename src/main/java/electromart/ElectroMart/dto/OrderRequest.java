package electromart.ElectroMart.dto;

import lombok.Data;

@Data
public class OrderRequest {

    private String customerName;

    private String email;

    private String phone;

    private String address;

    private Double totalAmount;

    private String paymentMethod;
}