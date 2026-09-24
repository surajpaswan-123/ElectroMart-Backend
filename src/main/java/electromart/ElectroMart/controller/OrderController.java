package electromart.ElectroMart.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import electromart.ElectroMart.entity.Order;
import electromart.ElectroMart.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(order));
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/my")
    public List<Order> getMyOrders() {
        return orderService.getMyOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PatchMapping("/{id}/tracking")
    public Order updateTracking(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String courier = body.get("courierName") == null ? null : String.valueOf(body.get("courierName"));
        String tracking = body.get("trackingNumber") == null ? null : String.valueOf(body.get("trackingNumber"));
        Integer days = body.get("deliveryDays") == null ? null : Integer.valueOf(String.valueOf(body.get("deliveryDays")));
        return orderService.updateTracking(id, courier, tracking, days);
    }

    @PatchMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderService.updateStatus(id, body.get("status"));
    }
}
