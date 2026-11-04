package com.likehub.likehub.likes;

import com.likehub.likehub.auth.commons.api.ApiPaths;
import com.likehub.likehub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeFacade likeFacade;

    @PostMapping(ApiPaths.Likes.TOGGLE)
    public ResponseEntity<Boolean> toggle(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestParam UUID targetId,
            @RequestParam(defaultValue = "POST") TargetType targetType) {

        boolean resultado = likeFacade.toggle(authenticatedUser.getPublicId(), targetId, targetType);
        return ResponseEntity.ok(resultado);
    }


    @GetMapping(ApiPaths.Likes.COUNT)
    public ResponseEntity<Long> getCount(@PathVariable UUID targetId) {
        Long totalLikes = likeFacade.getLikeCount(targetId);
        return ResponseEntity.ok(totalLikes);
    }


    @GetMapping(ApiPaths.Likes.USERS)
    public ResponseEntity<java.util.List<String>> getUsersWhoLiked(
            @PathVariable UUID targetId,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        java.util.List<String> usuarios = likeFacade.getUsersWhoLiked(targetId, page);
        return ResponseEntity.ok(usuarios);
    }
}
