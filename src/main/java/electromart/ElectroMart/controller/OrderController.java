package electromart.ElectroMart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import electromart.ElectroMart.entity.Order;
import electromart.ElectroMart.service.OrderService;

@RestController

@RequestMapping("/api/orders")

public class OrderController {

    @Autowired
    private OrderService orderService;

    // Place Order
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Order order) {
        try {
            int size = (order != null && order.getOrderItems() != null) ? order.getOrderItems().size() : -1;
            System.out.println("[OrderController] POST /api/orders received");
            System.out.println("[OrderController] request body: customerName=" + (order == null ? null : order.getCustomerName()));
            System.out.println("[OrderController] orderItems size=" + size);
            if (order != null && order.getOrderItems() != null) {
                for (int i = 0; i < order.getOrderItems().size(); i++) {
                    var oi = order.getOrderItems().get(i);
                    Long productId = (oi != null && oi.getProduct() != null) ? oi.getProduct().getId() : null;
                    System.out.println("[OrderController] orderItem[" + i + "] productId=" + productId + ", qty=" + (oi == null ? null : oi.getQuantity()) + ", price=" + (oi == null ? null : oi.getPrice()));
                }
            }

            Order saved = orderService.placeOrder(order);
            System.out.println("[OrderController] order saved successfully. id=" + (saved == null ? null : saved.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception ex) {
            System.out.println("[OrderController] placeOrder failed: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of(
                            "message", ex.getMessage(),
                            "error", ex.getClass().getSimpleName()));
        }
    }



    // Get All Orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Get Order By Id
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/my")
public List<Order> getMyOrders() {
    return orderService.getMyOrders();
}
}