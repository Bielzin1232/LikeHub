package com.likehub.likehub.feed;

import com.likehub.likehub.api.ApiPaths;
import com.likehub.likehub.posts.dto.PostResponse;
import com.likehub.likehub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Feed.BASE)
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> personalized(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "page", defaultValue = "0") int page) {

        List<PostResponse> feed = feedService.personalized(user.getPublicId(), page);
        return ResponseEntity.ok(feed);
    }
}
