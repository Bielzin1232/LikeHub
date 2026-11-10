package com.likehub.likehub.comments;

import com.likehub.likehub.api.ApiPaths;
import com.likehub.likehub.comments.dto.*;
import com.likehub.likehub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping(ApiPaths.Comments.BY_POST)
    public ResponseEntity<java.util.List<CommentResponse>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(commentService.getComments(postId, page, size));
    }

    @GetMapping(ApiPaths.Comments.REPLIES)
    public ResponseEntity<java.util.List<CommentResponse>> getReplies(
            @PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    @PostMapping(ApiPaths.Comments.CREATE)
    public ResponseEntity<CreateCommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User usuarioLogado) {
        CreateCommentResponse createdComment = commentService.createComment(request, usuarioLogado);
        return ResponseEntity.ok(createdComment);
    }

    @PutMapping(ApiPaths.Comments.UPDATE)
    public ResponseEntity<CommentEditResponse> updateComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal User usuarioLogado) {
        CommentEditResponse updatedComment = commentService.updateComment(
                postId, commentId, request, usuarioLogado.getPublicId());
        return ResponseEntity.ok(updatedComment);
    }

    @PutMapping(ApiPaths.Comments.CREATE_REPLY)
    public ResponseEntity<CreateCommentResponse> createCommentReply(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @RequestBody String text,
            @AuthenticationPrincipal User usuarioLogado) {
        CreateCommentResponse createdReply = commentService.createCommentReply(
                postId, commentId, text, usuarioLogado);
        return ResponseEntity.ok(createdReply);
    }

    @DeleteMapping(ApiPaths.Comments.DELETE)
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User usuarioLogado) {
        commentService.deleteComment(postId, commentId, usuarioLogado.getPublicId());
        return ResponseEntity.noContent().build();
    }

}
