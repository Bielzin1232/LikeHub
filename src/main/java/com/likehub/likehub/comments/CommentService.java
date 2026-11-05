package com.likehub.likehub.comments;


import com.likehub.likehub.comments.dto.CommentEditResponse;
import com.likehub.likehub.comments.dto.CommentResponse;
import com.likehub.likehub.comments.dto.CreateCommentRequest;
import com.likehub.likehub.comments.dto.CreateCommentResponse;
import com.likehub.likehub.comments.dto.UpdateCommentRequest;
import com.likehub.likehub.shared.exceptions.AcessoNegadoException;
import com.likehub.likehub.shared.exceptions.RecursoNaoEncontradoException;
import com.likehub.likehub.posts.Post;
import com.likehub.likehub.posts.PostRepository;
import com.likehub.likehub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;


    private static final String COMMENTS_KEY = "comments:post:";
    private static final Duration COMMENTS_TTL = Duration.ofMinutes(5);


    @Transactional
    public CreateCommentResponse createComment(CreateCommentRequest request, User authenticatedUser) {
        Post post = postRepository.findByPublicId(request.postPublicId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "POST_NAO_ENCONTRADO", "O post informado não existe."));

        Comment comment = new Comment();
        comment.setText(request.text());
        comment.setPost(post);
        comment.setUser(authenticatedUser);
        Comment savedComment = commentRepository.save(comment);
        executeAfterCommit(() -> invalidateCommentsCache(request.postPublicId()));

        return new CreateCommentResponse("Comentário criado com sucesso", savedComment.getId());
    }

    @Transactional
    public CommentEditResponse updateComment(UUID postId, UUID commentId, UpdateCommentRequest request, UUID userId) {
        Comment comment = commentRepository.findByPost_PublicIdAndId(postId, commentId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "COMENTARIO_NAO_ENCONTRADO", "O comentário informado não existe neste post."));

        if (!comment.getUser().getPublicId().equals(userId)) {
            throw new AcessoNegadoException(
                    "COMENTARIO_NAO_PERTENCE_AO_USUARIO",
                    "O comentário informado não pertence ao usuário.");
        }

        comment.setText(request.text());
        executeAfterCommit(() -> invalidateCommentsCache(postId));
        return new CommentEditResponse("Comentário atualizado com sucesso");
    }

    @Transactional
    public CreateCommentResponse createCommentReply(UUID postId, UUID parentCommentId, String text, User authenticatedUser) {
        Comment targetComment = commentRepository.findByPost_PublicIdAndId(postId, parentCommentId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "COMENTARIO_NAO_ENCONTRADO",
                        "O comentário informado não existe neste post."
                ));

        Comment rootComment = targetComment.getParent() != null ? targetComment.getParent() : targetComment;

        Comment reply = new Comment();
        reply.setPost(rootComment.getPost());
        reply.setParent(rootComment);
        reply.setText(text);
        reply.setUser(authenticatedUser);

        Comment savedReply = commentRepository.save(reply);
        executeAfterCommit(() -> invalidateCommentsCache(postId));

        return new CreateCommentResponse("Resposta criada com sucesso", savedReply.getId());
    }

    @Transactional
    public CommentEditResponse updateCommentReply(UUID postId, UUID commentId, UpdateCommentRequest request, UUID userId) {
        Comment comment = commentRepository.findByPost_PublicIdAndId(postId, commentId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "COMENTARIO_NAO_ENCONTRADO", "O comentário informado não existe neste post."));

        if (!comment.getUser().getPublicId().equals(userId)) {
            throw new AcessoNegadoException(
                    "COMENTARIO_NAO_PERTENCE_AO_USUARIO",
                    "O comentário informado não pertence ao usuário.");
        }

        comment.setText(request.text());
        commentRepository.save(comment);
        executeAfterCommit(() -> invalidateCommentsCache(postId));
        return new CommentEditResponse("Comentário atualizado com sucesso");
    }

    @Transactional
    public void deleteComment(UUID postId, UUID commentId, UUID userId) {
        Comment comment = commentRepository.findByPost_PublicIdAndId(postId, commentId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "COMENTARIO_NAO_ENCONTRADO", "O comentário informado não existe neste post."));

        if (!comment.getUser().getPublicId().equals(userId)) {
            throw new AcessoNegadoException(
                    "COMENTARIO_NAO_PERTENCE_AO_USUARIO",
                    "O comentário informado não pertence ao usuário.");
        }

        commentRepository.delete(comment);
        executeAfterCommit(() -> invalidateCommentsCache(postId));
    }


    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID postPublicId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("A página deve ser >= 0 e o tamanho deve estar entre 1 e 100.");
        }

        String key = COMMENTS_KEY + postPublicId + ":page:" + page + ":size:" + size;

        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return fromJson(cached);
        }

        List<CommentResponse> response = commentRepository.findByPost_PublicIdAndParentIsNullOrderByCreatedAtDesc(
                postPublicId,
                org.springframework.data.domain.PageRequest.of(page, size)
        ).getContent()
                .stream()
                .map(CommentResponse::from)
                .toList();

        redisTemplate.opsForValue().set(key, toJson(response), COMMENTS_TTL);

        return response;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new RecursoNaoEncontradoException(
                    "COMENTARIO_NAO_ENCONTRADO", "O comentário informado não existe.");
        }

        return commentRepository.findByParent_IdOrderByCreatedAtAsc(commentId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    private String toJson(List<CommentResponse> comments) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(comments);
            output.flush();
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível serializar os comentários para o cache.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private List<CommentResponse> fromJson(String json) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(json);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Não foi possível ler os comentários do cache.", exception);
        }

        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (List<CommentResponse>) input.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException exception) {
            throw new IllegalStateException("Não foi possível ler os comentários do cache.", exception);
        }
    }

    private void invalidateCommentsCache(UUID postPublicId) {
        Set<String> keys = redisTemplate.keys(
                "comments:post:" + postPublicId + ":*"
        );

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void executeAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

}
