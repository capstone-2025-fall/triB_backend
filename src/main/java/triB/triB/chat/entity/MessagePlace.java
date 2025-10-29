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

    @Column(name = "place_tag", nullable = true)
    private PlaceTag placeTag;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
