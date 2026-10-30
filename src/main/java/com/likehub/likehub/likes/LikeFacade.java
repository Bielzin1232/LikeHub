package com.likehub.likehub.likes;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LikeFacade {

    private final LikeRecordRepository likeRecordRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final com.likehub.likehub.posts.PostRepository postRepository;

    @Transactional
    public boolean toggle(UUID userId, UUID postId, TargetType targetType) {
        if (targetType != TargetType.POST) {
            throw new IllegalArgumentException("Apenas likes de posts são suportados.");
        }
        var existing = likeRecordRepository.findByUserIdAndPostIdAndTargetType(userId, postId, targetType);
        if (existing != null) {
            descurtir(userId, postId, targetType);
            return false;
        }
        curtir(userId, postId, targetType);
        return true;
    }

    @Transactional
    public void curtir(UUID userId, UUID postId, TargetType targetType) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post não encontrado."));
        String chaveContador = "likes:count:" + postId;
        String chaveUsuarios = "likes:users:" + postId;
        String chavePrefRedis = "user:" + userId + ":preferences";

        LikeRecord like = new LikeRecord();
        like.setUserId(userId);
        like.setPostId(postId);
        like.setTargetType(targetType);
        likeRecordRepository.save(like);

        Long adicionouNoCache = redisTemplate.opsForSet().add(chaveUsuarios, userId.toString());

        if (adicionouNoCache != null && adicionouNoCache > 0) {
            redisTemplate.opsForValue().increment(chaveContador);
            redisTemplate.expire(chaveContador, 24, TimeUnit.HOURS);

            List<String> tagsDoPost = post.getHashtags();
            for (String tag : tagsDoPost) {
                redisTemplate.opsForHash().increment(chavePrefRedis, tag, 1);
            }
        }
    }

    @Transactional
    public void descurtir(UUID userId, UUID postId) {
        descurtir(userId, postId, TargetType.POST);
    }

    @Transactional
    public void descurtir(UUID userId, UUID postId, TargetType targetType) {
        String chaveContador = "likes:count:" + postId;
        String chaveUsuarios = "likes:users:" + postId;
        String chavePrefRedis = "user:" + userId + ":preferences";

        likeRecordRepository.deleteByUserIdAndPostIdAndTargetType(userId, postId, targetType);

        Long removeuDoCache = redisTemplate.opsForSet().remove(chaveUsuarios, userId.toString());

        if (removeuDoCache != null && removeuDoCache > 0) {
            redisTemplate.opsForValue().decrement(chaveContador);

            List<String> tagsDoPost = postRepository.findById(postId)
                    .map(com.likehub.likehub.posts.Post::getHashtags)
                    .orElse(List.of());
            for (String tag : tagsDoPost) {
                redisTemplate.opsForHash().increment(chavePrefRedis, tag, -1);
            }
        }
    }

    public Long obterTotalLikes(UUID postId) {
        String chaveContador = "likes:count:" + postId;
        String totalCache = redisTemplate.opsForValue().get(chaveContador);

        if (totalCache != null) {
            return Long.parseLong(totalCache);
        }

        Long totalBanco = likeRecordRepository.countByPostIdAndTargetType(postId, TargetType.POST);

        redisTemplate.opsForValue().set(chaveContador, totalBanco.toString(), 2, TimeUnit.HOURS);

        return totalBanco;
    }
}
