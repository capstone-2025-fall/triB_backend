package triB.triB.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.community.dto.PostSortType;
import triB.triB.community.dto.request.TripSharePostCreateRequest;
import triB.triB.community.dto.response.PostDetailsResponse;
import triB.triB.community.dto.response.PostSummaryResponse;
import triB.triB.community.entity.*;
import triB.triB.community.repository.*;
import triB.triB.global.exception.CustomException;
import triB.triB.global.exception.ErrorCode;
import triB.triB.global.infra.AwsS3Client;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.repository.TripRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final HashtagRepository hashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final UserRoomRepository userRoomRepository;
    private final AwsS3Client awsS3Client;

    @Transactional
    public PostDetailsResponse createTripSharePost(Long userId,
                                                   TripSharePostCreateRequest request,
                                                   List<MultipartFile> images) {
        // 1. 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. Trip 검증 및 조회
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        // 3. 사용자가 해당 여행의 참여자인지 검증
        validateUserInTrip(userId, trip);

        // 4. Post 엔티티 생성
        Post post = Post.builder()
                .userId(userId)
                .user(user)
                .postType(PostType.TRIP_SHARE)
                .tripId(request.getTripId())
                .trip(trip)
                .title(request.getTitle())
                .content(request.getContent())
                .likesCount(0)
                .commentsCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        // 5. 이미지 업로드 및 저장
        List<PostImage> postImages = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = awsS3Client.uploadFile(images.get(i));
                PostImage postImage = PostImage.builder()
                        .postId(savedPost.getPostId())
                        .post(savedPost)
                        .imageUrl(imageUrl)
                        .displayOrder(i)
                        .build();
                postImages.add(postImageRepository.save(postImage));
            }
        }

        // 6. 해시태그는 일단 빈 리스트 (PR #9에서 AI 생성 추가 예정)
        List<Hashtag> hashtags = new ArrayList<>();

        // 7. Response 생성
        return PostDetailsResponse.from(savedPost, user, trip, postImages, hashtags, false);
    }

    public PostDetailsResponse getPostDetails(Long postId, Long currentUserId) {
        // 1. Post 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 2. 작성자 조회
        User author = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 3. Trip 조회 (TRIP_SHARE인 경우만)
        Trip trip = null;
        if (post.getPostType() == PostType.TRIP_SHARE && post.getTripId() != null) {
            trip = tripRepository.findById(post.getTripId()).orElse(null);
        }

        // 4. 이미지 조회
        List<PostImage> images = postImageRepository.findByPostIdOrderByDisplayOrderAsc(postId);

        // 5. 해시태그 조회
        List<PostHashtag> postHashtags = postHashtagRepository.findByIdPostId(postId);
        List<Hashtag> hashtags = postHashtags.stream()
                .map(ph -> hashtagRepository.findById(ph.getId().getHashtagId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 6. 현재 사용자의 좋아요 여부 확인
        boolean isLikedByMe = currentUserId != null &&
                postLikeRepository.existsByIdPostIdAndIdUserId(postId, currentUserId);

        return PostDetailsResponse.from(post, author, trip, images, hashtags, isLikedByMe);
    }

    public List<PostSummaryResponse> getTripSharePosts(PostSortType sortType) {
        // 기본 정렬로 조회 (다음 PR에서 복잡한 필터링 추가)
        List<Post> posts = postRepository.findByPostTypeOrderByCreatedAtDesc(PostType.TRIP_SHARE);

        return posts.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    private PostSummaryResponse mapToSummary(Post post) {
        User author = userRepository.findById(post.getUserId()).orElse(null);
        Trip trip = post.getTripId() != null ?
                tripRepository.findById(post.getTripId()).orElse(null) : null;

        // 첫 번째 이미지 URL 조회
        String coverImageUrl = postImageRepository.findByPostIdOrderByDisplayOrderAsc(post.getPostId())
                .stream()
                .findFirst()
                .map(PostImage::getImageUrl)
                .orElse(null);

        // 해시태그 조회
        List<Hashtag> hashtags = postHashtagRepository.findByIdPostId(post.getPostId())
                .stream()
                .map(ph -> hashtagRepository.findById(ph.getId().getHashtagId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PostSummaryResponse.from(post, author, trip, coverImageUrl, hashtags);
    }

    private void validateUserInTrip(Long userId, Trip trip) {
        // UserRoom을 통해 사용자가 해당 여행의 참여자인지 확인
        boolean isParticipant = userRoomRepository.existsByUser_UserIdAndRoom_RoomId(
                userId, trip.getRoomId());

        if (!isParticipant) {
            throw new CustomException(ErrorCode.USER_NOT_IN_TRIP);
        }
    }
}
