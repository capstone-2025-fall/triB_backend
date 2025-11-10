package triB.triB.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BookmarkResponse {
    private Long bookmarkId;
    private String content;
}
