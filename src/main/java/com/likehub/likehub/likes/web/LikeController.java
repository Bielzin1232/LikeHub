package com.likehub.likehub.likes.web;

import com.likehub.likehub.auth.commons.api.ApiPaths;
import com.likehub.likehub.likes.LikeFacade;
import com.likehub.likehub.likes.LikeToggleRequest;
import com.likehub.likehub.likes.LikeToggleResponse;
import com.likehub.likehub.likes.TargetType;
import com.likehub.likehub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.Likes.BASE)
@RequiredArgsConstructor
public class LikeController {

    private final LikeFacade likeFacade;

    @PostMapping("/toggle")
    public ResponseEntity<LikeToggleResponse> toggle(
            @Valid @RequestBody LikeToggleRequest request,
            @AuthenticationPrincipal User user) {
        boolean liked = likeFacade.toggle(user.getPublicId(), request.postId(), TargetType.POST);
        return ResponseEntity.ok(new LikeToggleResponse(
                liked, likeFacade.obterTotalLikes(request.postId())));
    }
}
