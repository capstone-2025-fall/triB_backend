package triB.triB.room.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record UserRoomId(Long userId, Long roomId) implements Serializable {}
