package triB.triB.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "oauth_accounts",
        uniqueConstraints = {
               @UniqueConstraint(
                       name = "uq_oauth_provider_user",
                       columnNames = {"provider", "provider_user_id"}
               )
        }
)
public class OauthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_account_id")
    private Long oauthAccountId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_oauth_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "access_token_encrypted", nullable = true)
    private String accessTokenEncrypted;

    @Column(name = "refresh_token_encrypted", nullable = true)
    private String refreshTokenEncrypted;

    @Column(name = "token_expires_at", nullable = true)
    private LocalDateTime tokenExpiresAt;

    @Column(name = "scope", columnDefinition = "TEXT", nullable = true)
    private String scope;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
