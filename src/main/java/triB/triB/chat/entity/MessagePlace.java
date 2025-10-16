package triB.triB.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import triB.triB.room.entity.Room;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "message_places",
        indexes = {
                @Index(name = "idx_room_id", columnList = "room_id")
        }
)
public class MessagePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_place_id")
    private Long messagePlaceId;

    @JoinColumn(name = "message_id", nullable = false)
    @OneToOne
    private Message message;

    @JoinColumn(name = "room_id", nullable = false)
    @ManyToOne
    private Room room;

    @Column(name = "source", nullable = false)
    private Source source;

    @Column(name = "place_id", nullable = false)
    private String placeId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "place_tag", nullable = true)
    private PlaceTag placeTag;

    @Column(name = "tag_source", nullable = false)
    private TagSource tagSource;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
