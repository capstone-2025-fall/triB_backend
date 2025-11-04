package triB.triB.community.entity;

import jakarta.persistence.*;
import lombok.*;
import triB.triB.auth.entity.User;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "post_likes",
        indexes = {
                @Index(name = "idx_post_likes_user_post", columnList = "user_id, post_id")
        }
)
public class PostLike {

    @EmbeddedId
    private PostLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
