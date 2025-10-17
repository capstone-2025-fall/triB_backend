package triB.triB.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostHashtagId implements Serializable {

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "hashtag_id")
    private Long hashtagId;
}
