package triB.triB.community.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import triB.triB.community.dto.PostSortType;
import triB.triB.community.dto.request.TripSharePostFilterRequest;
import triB.triB.community.entity.Post;
import triB.triB.community.entity.PostType;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Post> findTripSharePostsWithFilters(TripSharePostFilterRequest filter) {
        // JPQL을 사용한 동적 쿼리 구성
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT DISTINCT p FROM Post p ");
        jpql.append("LEFT JOIN FETCH p.trip t ");
        jpql.append("LEFT JOIN FETCH t.room r ");
        jpql.append("LEFT JOIN p.hashtags ph ");
        jpql.append("LEFT JOIN ph.hashtag h ");
        jpql.append("WHERE p.postType = :postType ");

        // 나라 필터
        if (filter.getCountry() != null && !filter.getCountry().isEmpty()) {
            jpql.append("AND t.destination LIKE :country ");
        }

        // 검색어 (제목 + 해시태그)
        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            jpql.append("AND (p.title LIKE :keyword OR h.tagName LIKE :keyword) ");
        }

        // 정렬
        PostSortType sortType = filter.getSortType();
        jpql.append("ORDER BY ");
        switch (sortType) {
            case LATEST:
                jpql.append("p.createdAt DESC");
                break;
            case OLDEST:
                jpql.append("p.createdAt ASC");
                break;
            case MOST_LIKED:
                jpql.append("p.likesCount DESC, p.postId DESC");
                break;
            case MOST_COMMENTED:
                jpql.append("p.commentsCount DESC, p.postId DESC");
                break;
            default:
                jpql.append("p.createdAt DESC");
        }

        // Query 생성 및 파라미터 설정
        TypedQuery<Post> query = entityManager.createQuery(jpql.toString(), Post.class);
        query.setParameter("postType", PostType.TRIP_SHARE);

        if (filter.getCountry() != null && !filter.getCountry().isEmpty()) {
            query.setParameter("country", "%" + filter.getCountry() + "%");
        }

        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            query.setParameter("keyword", "%" + filter.getKeyword() + "%");
        }

        // 페이징
        query.setFirstResult(filter.getPage() * filter.getSize());
        query.setMaxResults(filter.getSize());

        return query.getResultList();
    }
}
