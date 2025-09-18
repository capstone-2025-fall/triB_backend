package triB.triB.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uq_users_username", columnNames = "username")

        }

)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Email
    @Column(name = "email", nullable = true, unique = true)
    private String email;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "nickname", nullable = true, unique = true)
    private String nickname;

    @Column(name = "username", nullable = true, unique = true)
    private String username;

    @Lob
    @Column(name = "photo_url", columnDefinition = "TEXT", nullable = true)
    private String photoUrl;

    @Column(name = "user_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.ACTIVE;

    @Column(name = "is_alarm", nullable = false)
    @Enumerated(EnumType.STRING)
    private IsAlarm isAlarm;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
