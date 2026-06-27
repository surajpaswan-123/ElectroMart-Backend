package electromart.ElectroMart.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import electromart.ElectroMart.dto.CartResponse;
import electromart.ElectroMart.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Get authenticated user's cart.
     */
    @GetMapping
    public CartResponse getCart() {
        return cartService.getCartForAuthenticatedUser();
    }

    /**
     * Add product to authenticated user's cart.
     */
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ) {
        return cartService.addToCart(productId, quantity);
    }

    /**
     * Update quantity for a product in authenticated user's cart.
     */
    @PutMapping("/update")
    public CartResponse updateQuantity(
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ) {
        return cartService.updateQuantity(productId, quantity);
    }

    /**
     * Remove a product from authenticated user's cart.
     */
    @DeleteMapping("/remove/{productId}")
    public CartResponse removeItem(
            @PathVariable Long productId
    ) {
        return cartService.removeItem(productId);
    }

    /**
     * Clear authenticated user's cart.
     */
    @DeleteMapping("/clear")
    public CartResponse clearCart() {
        return cartService.clearCart();
    }
}
