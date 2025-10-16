package triB.triB.room.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record RoomReadStateId(Long userId, Long roomId) implements Serializable {}