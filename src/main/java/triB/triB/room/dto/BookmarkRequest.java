package triB.triB.room.dto;

import lombok.Getter;

@Getter
public class BookmarkRequest {
    private Long roomId;
    private Long bookmarkId;
    private String content;
}
