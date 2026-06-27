package electromart.ElectroMart.repository;

import electromart.ElectroMart.entity.Order;
import electromart.ElectroMart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
