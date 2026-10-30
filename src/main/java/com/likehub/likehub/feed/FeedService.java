package com.likehub.likehub.feed;

import com.likehub.likehub.likes.LikeFacade;
import com.likehub.likehub.posts.Post;
import com.likehub.likehub.posts.PostRepository;
import com.likehub.likehub.posts.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private static final int CANDIDATE_LIMIT = 100;
    private static final int FEED_LIMIT = 20;
    private static final double RECENCY_WINDOW_HOURS = 168.0;

    private final PostRepository postRepository;
    private final LikeFacade likeFacade;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public List<PostResponse> personalized(UUID userId) {
        Map<Object, Object> preferences =
                redisTemplate.opsForHash().entries("user:" + userId + ":preferences");
        Instant now = Instant.now();

        return postRepository.findAll(PageRequest.of(0, CANDIDATE_LIMIT,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(post -> new ScoredPost(post, score(post, preferences, now)))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .limit(FEED_LIMIT)
                .map(ScoredPost::post)
                .map(PostResponse::from)
                .toList();
    }

    private double score(Post post, Map<Object, Object> preferences, Instant now) {
        double affinity = post.getHashtags().stream()
                .mapToDouble(tag -> preference(preferences, tag))
                .sum();
        double hoursOld = Math.max(0,
                Duration.between(post.getCreatedAt(), now).toMinutes() / 60.0);
        double recency = Math.max(0, 1 - hoursOld / RECENCY_WINDOW_HOURS) * 2;
        double popularity = Math.log1p(likeFacade.obterTotalLikes(post.getId()));
        return affinity * 3 + recency + popularity;
    }

    private double preference(Map<Object, Object> preferences, String tag) {
        Object value = preferences.get(tag);
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private record ScoredPost(Post post, double score) {}
}
