package triB.triB.community.repository;

import triB.triB.community.dto.request.TripSharePostFilterRequest;
import triB.triB.community.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findTripSharePostsWithFilters(TripSharePostFilterRequest filter);
}
