package electromart.ElectroMart.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import electromart.ElectroMart.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
}
