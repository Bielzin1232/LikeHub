package com.likehub.likehub.likes;

import com.likehub.likehub.posts.PostRepository;
import com.likehub.likehub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LikeFacade {

    private final LikeRecordRepository likeRecordRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public boolean toggle(UUID userPublicId, UUID postPublicId, TargetType targetType) {
        if (targetType != TargetType.POST) {
            throw new IllegalArgumentException("Apenas likes de posts são suportados no momento.");
        }

        var existing = likeRecordRepository.findByUserIdAndTargetIdAndTargetType(userPublicId, postPublicId, targetType);

        if (existing.isPresent()) {
            unlike(userPublicId, postPublicId, targetType);
            return false;
        }

        like(userPublicId, postPublicId, targetType);
        return true;
    }

    @Transactional
    public void like(UUID userPublicId, UUID postPublicId, TargetType targetType) {
        var post = postRepository.findByPublicId(postPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Post não encontrado."));

        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        LikeRecord like = new LikeRecord();
        like.setUser(user);
        like.setTargetId(postPublicId);
        like.setTargetType(targetType);
        likeRecordRepository.save(like);

        List<String> tagsDoPost = List.copyOf(post.getHashtags());

        executeAfterCommit(() -> {
            String chaveContador = "likes:count:" + postPublicId;
            String chaveUsuarios = "likes:users:" + postPublicId;
            String chavePrefRedis = "user:" + userPublicId + ":preferences";

            Long adicionouNoCache = redisTemplate.opsForSet().add(chaveUsuarios, userPublicId.toString());

            if (adicionouNoCache != null && adicionouNoCache > 0) {
                redisTemplate.opsForValue().increment(chaveContador);
                redisTemplate.expire(chaveContador, 24, TimeUnit.HOURS);

                for (String tag : tagsDoPost) {
                    redisTemplate.opsForHash().increment(chavePrefRedis, tag, 1);
                }
            }
        });
    }

    public List<String> getUsersWhoLiked(UUID targetId, int page) {
        String chaveUsuarios = "likes:users:" + targetId;

        Set<String> usuariosNoCache = redisTemplate.opsForSet().members(chaveUsuarios);

        if (usuariosNoCache != null && !usuariosNoCache.isEmpty()) {
            return usuariosNoCache.stream().toList();
        }

        return likeRecordRepository.findUsernamesByTargetId(
                targetId,
                org.springframework.data.domain.PageRequest.of(page, 20)
        ).getContent();
    }

    @Transactional
    public void unlike(UUID userPublicId, UUID postPublicId, TargetType targetType) {
        List<String> tagsDoPost = postRepository.findByPublicId(postPublicId)
                .map(com.likehub.likehub.posts.Post::getHashtags)
                .orElse(List.of());

        likeRecordRepository.deleteByUserIdAndTargetIdAndTargetType(userPublicId, postPublicId, targetType);

        executeAfterCommit(() -> {
            String chaveContador = "likes:count:" + postPublicId;
            String chaveUsuarios = "likes:users:" + postPublicId;
            String chavePrefRedis = "user:" + userPublicId + ":preferences";

            Long removeuDoCache = redisTemplate.opsForSet().remove(chaveUsuarios, userPublicId.toString());

            if (removeuDoCache != null && removeuDoCache > 0) {
                redisTemplate.opsForValue().decrement(chaveContador);

                for (String tag : tagsDoPost) {
                    redisTemplate.opsForHash().increment(chavePrefRedis, tag, -1);
                }
            }
        });
    }

    public Long getLikeCount(UUID postPublicId) {
        String chaveContador = "likes:count:" + postPublicId;
        String totalCache = redisTemplate.opsForValue().get(chaveContador);

        if (totalCache != null) {
            return Long.parseLong(totalCache);
        }

        Long totalBanco = likeRecordRepository.countByTargetIdAndTargetType(postPublicId, TargetType.POST);

        redisTemplate.opsForValue().set(chaveContador, totalBanco.toString(), 2, TimeUnit.HOURS);

        return totalBanco;
    }

    private void executeAfterCommit(Runnable acao) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acao.run();
                }
            });
        } else {
            acao.run();
        }
    }
}
