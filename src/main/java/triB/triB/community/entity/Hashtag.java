package triB.triB.community.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "hashtags",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "tag_name")
        },
        indexes = {
                @Index(name = "idx_hashtags_tag_name", columnList = "tag_name", unique = true)
        }
)
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long hashtagId;

    @Column(name = "tag_name", length = 50, nullable = false, unique = true)
    private String tagName;

    @Column(name = "tag_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TagType tagType;

    @Builder.Default
    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> posts = new ArrayList<>();
}
