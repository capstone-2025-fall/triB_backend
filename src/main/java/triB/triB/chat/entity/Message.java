package triB.triB.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import triB.triB.room.entity.Room;
import triB.triB.auth.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_room_id", columnList = "room_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_msg_type_content", columnList = "message_type, message_content")
        }
)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_status")
    @Builder.Default
    private MessageStatus messageStatus = MessageStatus.ACTIVE;

    @Column(name = "content", columnDefinition = "TEXT", nullable = true)
    private String content;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
