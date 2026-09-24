package electromart.ElectroMart.controller;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import electromart.ElectroMart.entity.Address;
import electromart.ElectroMart.service.AddressService;
@RestController @RequestMapping("/api/addresses")
public class AddressController {
 @Autowired private AddressService service;
 @GetMapping public List<Address> list(){return service.list();}
 @PostMapping public Address add(@RequestBody Address address){return service.add(address);}
 @PutMapping("/{id}") public Address update(@PathVariable Long id,@RequestBody Address address){return service.update(id,address);}
 @DeleteMapping("/{id}") public void delete(@PathVariable Long id){service.delete(id);}
 @PatchMapping("/{id}/default") public Address makeDefault(@PathVariable Long id){return service.makeDefault(id);}
}