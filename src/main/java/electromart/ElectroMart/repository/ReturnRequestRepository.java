package electromart.ElectroMart.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import electromart.ElectroMart.entity.ReturnRequest;
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest,Long> {
 Optional<ReturnRequest> findByOrderId(Long orderId);
 List<ReturnRequest> findAllByOrderUserIdOrderByRequestedAtDesc(Long userId);
}
