package triB.triB.community.dto;

import lombok.Builder;
import lombok.Getter;
import triB.triB.community.entity.Hashtag;
import triB.triB.community.entity.TagType;

@Getter
@Builder
public class HashtagResponse {
    private Long hashtagId;
    private String tagName;
    private TagType tagType;

    public static HashtagResponse from(Hashtag hashtag) {
        return HashtagResponse.builder()
                .hashtagId(hashtag.getHashtagId())
                .tagName(hashtag.getTagName())
                .tagType(hashtag.getTagType())
                .build();
    }
}
