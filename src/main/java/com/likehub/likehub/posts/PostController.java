package com.likehub.likehub.posts;

import com.likehub.likehub.auth.commons.api.ApiPaths;
import com.likehub.likehub.posts.dto.CreatePostRequest;
import com.likehub.likehub.posts.dto.PostResponse;
import com.likehub.likehub.posts.dto.UpdatePostRequest;
import com.likehub.likehub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.Posts.BASE)
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> create(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        Post post = postService.create(request, authenticatedUser.getPublicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @PutMapping(ApiPaths.Posts.BY_ID)
    public ResponseEntity<PostResponse> update(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        Post post = postService.update(postId, request, authenticatedUser.getPublicId());
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @DeleteMapping(ApiPaths.Posts.BY_ID)
    public ResponseEntity<Void> delete(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User authenticatedUser) {
        postService.delete(postId, authenticatedUser.getPublicId());
        return ResponseEntity.noContent().build();
    }
}
