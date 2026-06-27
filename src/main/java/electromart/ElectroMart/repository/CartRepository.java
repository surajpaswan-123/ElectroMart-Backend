package electromart.ElectroMart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import electromart.ElectroMart.entity.Cart;
import electromart.ElectroMart.entity.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}


