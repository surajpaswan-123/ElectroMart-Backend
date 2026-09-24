package electromart.ElectroMart.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(nullable=false) private String fullName;
 @Column(nullable=false) private String phone;
 @Column(nullable=false,length=500) private String addressLine1;
 private String addressLine2;
 @Column(nullable=false) private String city;
 @Column(nullable=false) private String state;
 @Column(nullable=false) private String postalCode;
 private String landmark;
 @Column(nullable=false) private Boolean isDefault=false;
}