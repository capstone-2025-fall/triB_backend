package triB.triB.community.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "post_hashtags",
        indexes = {
                @Index(name = "idx_post_hashtags_hashtag_post", columnList = "hashtag_id, post_id")
        }
)
public class PostHashtag {

    @EmbeddedId
    private PostHashtagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", insertable = false, updatable = false)
    private Hashtag hashtag;
}
