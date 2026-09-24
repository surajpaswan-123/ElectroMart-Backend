package electromart.ElectroMart.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import electromart.ElectroMart.entity.ReturnRequest;
import electromart.ElectroMart.service.ReturnRequestService;

@RestController
@RequestMapping("/api/returns")
public class ReturnRequestController {
 @Autowired private ReturnRequestService service;
 @PostMapping("/orders/{orderId}") public ReturnRequest request(@PathVariable Long orderId,@RequestBody java.util.Map<String,String> body){return service.request(orderId,body.get("reason"));}
 @GetMapping("/my") public List<ReturnRequest> my(){return service.my();}
 @GetMapping public List<ReturnRequest> all(){return service.all();}
 @PatchMapping("/{id}") public ReturnRequest update(@PathVariable Long id,@RequestBody java.util.Map<String,String> body){return service.update(id,body.get("status"));}
}
