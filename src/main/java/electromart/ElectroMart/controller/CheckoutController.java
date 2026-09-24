package electromart.ElectroMart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import electromart.ElectroMart.dto.CheckoutRequest;
import electromart.ElectroMart.dto.CheckoutResponse;
import electromart.ElectroMart.service.CheckoutService;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(@RequestBody CheckoutRequest request) {
        return checkoutService.checkout(request);
    }
}
