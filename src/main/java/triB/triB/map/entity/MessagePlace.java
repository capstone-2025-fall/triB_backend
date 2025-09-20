package triB.triB.map.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "message_places",
        indexes = {
                @Index(name = "idx_room_created_tag", columnList = "room_id, created_at, place_tag"),
                @Index(name = "idx_room_tag", columnList = "room_id, place_tag"),
                @Index(name = "idx_room_msg", columnList = "room_id, message_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_message", columnNames = "message_id")
        }
)
public class MessagePlace {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_place_id")
    private Long messagePlaceId;
    
    @Column(name = "message_id", nullable = false)
    private Long messageId;
    
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    @Builder.Default
    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaceSource source = PlaceSource.MAP;
    
    @Column(name = "place_name", nullable = false)
    private String placeName;
    
    @Column(name = "google_place_id", nullable = false)
    private String googlePlaceId;
    
    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;
    
    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;
    
    @Column(name = "place_tag", nullable = true)
    @Enumerated(EnumType.STRING)
    private PlaceTag placeTag;
    
    @Builder.Default
    @Column(name = "tag_source", nullable = false)
    @Enumerated(EnumType.STRING)
    private TagSource tagSource = TagSource.USER;
    
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
