package electromart.ElectroMart.service;
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
public class AddressService {
 @Autowired private AddressRepository repo;
 @Autowired private UserRepository users;
 private User user(){Authentication a=SecurityContextHolder.getContext().getAuthentication(); if(a==null||!a.isAuthenticated()) throw new AccessDeniedException("Unauthorized"); return users.findByEmail(String.valueOf(a.getPrincipal())).orElseThrow(()->new RuntimeException("User not found"));}
 public List<Address> list(){return repo.findByUserOrderByIsDefaultDescIdDesc(user());}
 @Transactional public Address add(Address input){
  validate(input); User u=user(); Address a=copy(input,new Address()); a.setUser(u);
  if(Boolean.TRUE.equals(a.getIsDefault())||repo.findByUserOrderByIsDefaultDescIdDesc(u).isEmpty()) clearDefault(u);
  if(repo.findByUserOrderByIsDefaultDescIdDesc(u).isEmpty()) a.setIsDefault(true);
  return repo.save(a);
 }
 @Transactional public Address update(Long id,Address input){
  validate(input); User u=user(); Address a=repo.findByIdAndUserId(id,u.getId()).orElseThrow(()->new RuntimeException("Address not found"));
  copy(input,a); if(Boolean.TRUE.equals(a.getIsDefault())) clearDefaultExcept(u,a.getId()); return repo.save(a);
 }
 @Transactional public void delete(Long id){User u=user(); Address a=repo.findByIdAndUserId(id,u.getId()).orElseThrow(()->new RuntimeException("Address not found")); repo.delete(a); if(Boolean.TRUE.equals(a.getIsDefault())){List<Address> left=repo.findByUserOrderByIsDefaultDescIdDesc(u); if(!left.isEmpty()){left.get(0).setIsDefault(true);repo.save(left.get(0));}}}
 @Transactional public Address makeDefault(Long id){User u=user(); Address a=repo.findByIdAndUserId(id,u.getId()).orElseThrow(()->new RuntimeException("Address not found")); clearDefault(u); a.setIsDefault(true); return repo.save(a);}
 private Address copy(Address s,Address d){d.setFullName(s.getFullName());d.setPhone(s.getPhone());d.setAddressLine1(s.getAddressLine1());d.setAddressLine2(s.getAddressLine2());d.setCity(s.getCity());d.setState(s.getState());d.setPostalCode(s.getPostalCode());d.setLandmark(s.getLandmark());d.setIsDefault(Boolean.TRUE.equals(s.getIsDefault()));return d;}
 private void validate(Address a){if(a==null||blank(a.getFullName())||blank(a.getPhone())||blank(a.getAddressLine1())||blank(a.getCity())||blank(a.getState())||blank(a.getPostalCode()))throw new IllegalArgumentException("Required address fields are missing");}
 private boolean blank(String s){return s==null||s.trim().isEmpty();}
 private void clearDefault(User u){List<Address> xs=repo.findByUserOrderByIsDefaultDescIdDesc(u);xs.forEach(x->x.setIsDefault(false));repo.saveAll(xs);}
 private void clearDefaultExcept(User u,Long id){List<Address> xs=repo.findByUserOrderByIsDefaultDescIdDesc(u);xs.stream().filter(x->!x.getId().equals(id)).forEach(x->x.setIsDefault(false));repo.saveAll(xs);}
}