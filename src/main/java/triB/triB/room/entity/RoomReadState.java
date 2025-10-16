package triB.triB.room.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import triB.triB.auth.entity.User;
import triB.triB.chat.entity.Message;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "room_read_states",
        indexes = {
        }
)
public class RoomReadState {

    @EmbeddedId
    private RoomReadStateId id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne
    @MapsId("userId")
    private User user;

    @JoinColumn(name = "room_id", nullable = false)
    @ManyToOne
    @MapsId("roomId")
    private Room room;

    @Column(name = "last_read_message_id", nullable = true)
    private Long lastReadMessageId;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
