package com.likehub.likehub.posts;

import com.likehub.likehub.images.ImageStatus;
import com.likehub.likehub.images.UploadedImage;
import com.likehub.likehub.images.UploadedImageRepository;
import com.likehub.likehub.posts.dto.CreatePostRequest;
import com.likehub.likehub.posts.dto.UpdatePostRequest;
import com.likehub.likehub.shared.exceptions.AcessoNegadoException;
import com.likehub.likehub.shared.exceptions.ConflitoException;
import com.likehub.likehub.shared.exceptions.RecursoNaoEncontradoException;
import com.likehub.likehub.shared.exceptions.RequisicaoInvalidaException;
import com.likehub.likehub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UploadedImageRepository uploadedImageRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public Post create(CreatePostRequest req, UUID senderId) {
        Post post = new Post();
        post.setSender(userRepository.findByPublicId(senderId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "USUARIO_NAO_ENCONTRADO",
                        "O usuário informado não existe.")));
        preencherPost(post, req.title(), req.content(), req.imageIds(), senderId);
        Post savedPost = postRepository.save(post);
        atualizarFeedHashtags(savedPost, List.of());
        return savedPost;
    }

    @Transactional
    public Post update(UUID postId, UpdatePostRequest req, UUID authenticatedUserId) {
        Post post = obterPostDoUsuario(postId, authenticatedUserId);
        List<String> hashtagsAnteriores = new ArrayList<>(post.getHashtags());
        preencherPost(post, req.title(), req.content(), req.imageIds(), authenticatedUserId);
        Post updatedPost = postRepository.save(post);
        atualizarFeedHashtags(updatedPost, hashtagsAnteriores);
        return updatedPost;
    }

    @Transactional
    public void delete(UUID postId, UUID authenticatedUserId) {
        Post post = obterPostDoUsuario(postId, authenticatedUserId);
        List<String> hashtags = new ArrayList<>(post.getHashtags());
        postRepository.delete(post);
        for (String tag : hashtags) {
            redisTemplate.opsForZSet().remove("hashtag:feed:" + tag, postId.toString());
        }
    }

    private void preencherPost(Post post, String title, String content, List<UUID> imageIds, UUID senderId) {
        post.setTitle(title);
        post.setContent(content);
        post.getImages().clear();
        List<UUID> ids = imageIds == null ? List.of() : imageIds;
        if (!ids.isEmpty()) {
            attachImages(post, ids, senderId);
        }
        post.setHashtags(extrairHashtagsDoTexto(content));
    }

    private Post obterPostDoUsuario(UUID postId, UUID authenticatedUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "POST_NAO_ENCONTRADO", "O post informado não existe."));
        if (!post.getSender().getPublicId().equals(authenticatedUserId)) {
            throw new AcessoNegadoException("POST_NAO_PERTENCE_AO_USUARIO",
                    "O post informado não pertence ao usuário.");
        }
        return post;
    }

    private void atualizarFeedHashtags(Post post, List<String> hashtagsAnteriores) {
        for (String tag : hashtagsAnteriores) {
            if (!post.getHashtags().contains(tag)) {
                redisTemplate.opsForZSet().remove("hashtag:feed:" + tag, post.getId().toString());
            }
        }
        long timestampScore = System.currentTimeMillis();
        for (String tag : post.getHashtags()) {
            redisTemplate.opsForZSet().add("hashtag:feed:" + tag,
                    post.getId().toString(), timestampScore);
        }
    }

    private List<String> extrairHashtagsDoTexto(String texto) {
        if (texto == null || texto.isEmpty()) return List.of();

        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase();
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }
        return tags;
    }


    private void attachImages(Post post, List<UUID> imageIds, UUID userId) {
        if (new HashSet<>(imageIds).size() != imageIds.size()) {
            throw new RequisicaoInvalidaException(
                    "IMAGENS_DUPLICADAS",
                    "IDs de imagem duplicados.");
        }

        List<UploadedImage> uploads = uploadedImageRepository.findAllById(imageIds);

        if (uploads.size() != imageIds.size()) {
            throw new RecursoNaoEncontradoException(
                    "IMAGEM_NAO_ENCONTRADA",
                    "Uma ou mais imagens não existem.");
        }

        Map<UUID, UploadedImage> byId = uploads.stream()
                .collect(Collectors.toMap(UploadedImage::getId, Function.identity()));

        for (int i = 0; i < imageIds.size(); i++) {
            UploadedImage up = byId.get(imageIds.get(i));

            if (!up.getOwner().getPublicId().equals(userId)) {
                throw new AcessoNegadoException(
                        "IMAGEM_NAO_PERTENCE_AO_USUARIO",
                        "A imagem informada não pertence ao usuário.");
            }
            if (up.getStatus() != ImageStatus.PENDING) {
                throw new ConflitoException(
                        "IMAGEM_JA_UTILIZADA",
                        "A imagem informada já foi utilizada.");
            }

            PostImage img = new PostImage();
            img.setUrl(up.getUrl());
            img.setPosition(i);
            post.addImage(img);

            up.setStatus(ImageStatus.ATTACHED);
        }
    }
}