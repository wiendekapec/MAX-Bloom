package database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Сущность пользователя платформы.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_max_user_id", columnList = "max_user_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_user_id", nullable = false, unique = true, length = 64)
    private String maxUserId;

    @Column(name = "username", length = 128)
    private String username;

    @Column(name = "first_name", length = 128)
    private String firstName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "pdp_consent_given", nullable = false)
    @Builder.Default
    private Boolean pdpConsentGiven = false;

    @Column(name = "self_employed_confirmed", nullable = false)
    @Builder.Default
    private Boolean selfEmployedConfirmed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
