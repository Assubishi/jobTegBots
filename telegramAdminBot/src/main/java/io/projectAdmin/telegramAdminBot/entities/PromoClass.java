package io.projectAdmin.telegramAdminBot.entities;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="promo_codes")
public class PromoClass {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int bill;
    String salon_name;
    String promCode;

}
