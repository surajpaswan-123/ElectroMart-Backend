package electromart.ElectroMart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import electromart.ElectroMart.dto.WishlistResponse;
import electromart.ElectroMart.service.WishlistService;


@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping("/add")
    public WishlistResponse addToWishlist(
            @RequestParam Long productId) {

        return wishlistService.addToWishlist(productId);
    }

    @PostMapping("/toggle")
    public boolean toggleWishlist(@RequestParam Long productId) {
        return wishlistService.toggleWishlist(productId);
    }

    @GetMapping("/check/{productId}")
    public boolean isWishlisted(@PathVariable Long productId) {
        return wishlistService.isWishlisted(productId);
    }

    @GetMapping
    public List<WishlistResponse> getWishlist() {

        return wishlistService.getWishlist();
    }


    @DeleteMapping("/remove/{productId}")
    public String removeWishlist(
            @PathVariable Long productId) {

        wishlistService.removeWishlist(productId);

        return "Wishlist Item Removed";
    }
}

