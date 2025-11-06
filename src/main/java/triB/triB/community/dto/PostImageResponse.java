package triB.triB.community.dto;

import lombok.Builder;
import lombok.Getter;
import triB.triB.community.entity.PostImage;

@Getter
@Builder
public class PostImageResponse {
    private Long imageId;
    private String imageUrl;
    private Integer displayOrder;

    public static PostImageResponse from(PostImage image) {
        return PostImageResponse.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
