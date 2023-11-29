package ad.lotfiz.assignment.customerhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@Table(
        name = "Customers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"firstName", "lastName"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;
    private String lastName;
    private Integer age;
    private String address;
    private String email;
    @NonNull
    private OffsetDateTime created;
    @NonNull
    private OffsetDateTime updated;

}
