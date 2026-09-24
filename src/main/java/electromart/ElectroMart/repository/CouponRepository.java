package electromart.ElectroMart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import electromart.ElectroMart.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
}
