package electromart.ElectroMart.service;

import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import electromart.ElectroMart.entity.*;
import electromart.ElectroMart.repository.*;

@Service
public class ReturnRequestService {
 @Autowired private ReturnRequestRepository repo;
 @Autowired private OrderRepository orders;
 @Autowired private UserRepository users;
 @Autowired private PaymentRepository payments;

 private User user(){Authentication a=SecurityContextHolder.getContext().getAuthentication(); if(a==null||!a.isAuthenticated()) throw new AccessDeniedException("Unauthorized"); return users.findByEmail(String.valueOf(a.getPrincipal())).orElseThrow(()->new RuntimeException("User not found")); }
 private boolean admin(User u){return u.getRole()!=null&&"ADMIN".equalsIgnoreCase(u.getRole());}

 @Transactional public ReturnRequest request(Long orderId,String reason){
  User u=user(); if(reason==null||reason.trim().length()<3) throw new IllegalArgumentException("Return reason is required");
  Order o=orders.findById(orderId).orElseThrow(()->new RuntimeException("Order not found"));
  if(!o.getUser().getId().equals(u.getId())) throw new AccessDeniedException("You cannot return this order");
  if(!"DELIVERED".equalsIgnoreCase(o.getOrderStatus())) throw new IllegalStateException("Only delivered orders can be returned");
  if(o.getOrderDate()==null||o.getOrderDate().plusDays(7).isBefore(LocalDateTime.now())) throw new IllegalStateException("Return window has expired");
  if(repo.findByOrderId(orderId).isPresent()) throw new IllegalStateException("Return request already exists");
  double amount=o.getTotalAmount()==null?0:o.getTotalAmount();
  return repo.save(ReturnRequest.builder().order(o).reason(reason.trim()).status("REQUESTED").refundStatus("PENDING").refundAmount(amount).requestedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
 }
 public List<ReturnRequest> my(){return repo.findAllByOrderUserIdOrderByRequestedAtDesc(user().getId());}
 public List<ReturnRequest> all(){if(!admin(user())) throw new AccessDeniedException("Admin access required"); return repo.findAll();}
 @Transactional public ReturnRequest update(Long id,String status){
  if(!admin(user())) throw new AccessDeniedException("Admin access required");
  String s=status==null?"":status.trim().toUpperCase();
  if(!List.of("APPROVED","REJECTED","REFUNDED").contains(s)) throw new IllegalArgumentException("Invalid return status");
  ReturnRequest r=repo.findById(id).orElseThrow(()->new RuntimeException("Return request not found"));
  r.setStatus(s); r.setUpdatedAt(LocalDateTime.now());
  if("APPROVED".equals(s)){r.setRefundStatus("PROCESSING"); r.getOrder().setOrderStatus("RETURN_APPROVED");}
  if("REJECTED".equals(s)){r.setRefundStatus("NOT_APPLICABLE");}
  if("REFUNDED".equals(s)){r.setRefundStatus("REFUNDED"); r.getOrder().setOrderStatus("REFUNDED");}
  return repo.save(r);
 }
}
