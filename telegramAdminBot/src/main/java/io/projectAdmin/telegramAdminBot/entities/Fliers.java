package io.projectAdmin.telegramAdminBot.entities;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="fliersCounter")
public class Fliers {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String salon_name;
    int num_given;
    int num_activated;

}
