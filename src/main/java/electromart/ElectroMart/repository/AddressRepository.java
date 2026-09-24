package electromart.ElectroMart.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import electromart.ElectroMart.entity.Address;
public interface AddressRepository extends JpaRepository<Address,Long> {
 List<Address> findByUserOrderByIsDefaultDescIdDesc(electromart.ElectroMart.entity.User user);
 Optional<Address> findByIdAndUserId(Long id,Long userId);
}