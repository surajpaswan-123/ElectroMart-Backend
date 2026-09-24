package electromart.ElectroMart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import electromart.ElectroMart.dto.CouponApplyRequest;
import electromart.ElectroMart.dto.CouponRequest;
import electromart.ElectroMart.dto.CouponResponse;
import electromart.ElectroMart.entity.Coupon;
import electromart.ElectroMart.repository.CouponRepository;
import electromart.ElectroMart.service.CouponService;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired private CouponService couponService;
    @Autowired private CouponRepository couponRepository;

    @PostMapping("/apply")
    public CouponResponse apply(@RequestBody CouponApplyRequest request) {
        return couponService.apply(request);
    }

    @PostMapping
    public ResponseEntity<Coupon> create(@RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.create(request));
    }

    @GetMapping
    public List<Coupon> getAll() {
        return couponRepository.findAll();
    }
}
