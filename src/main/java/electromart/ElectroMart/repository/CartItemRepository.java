package electromart.ElectroMart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import electromart.ElectroMart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}