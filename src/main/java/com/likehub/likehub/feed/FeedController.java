package com.likehub.likehub.feed;

import com.likehub.likehub.auth.commons.api.ApiPaths;
import com.likehub.likehub.posts.dto.PostResponse;
import com.likehub.likehub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Feed.BASE)
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public List<PostResponse> personalized(@AuthenticationPrincipal User user) {
        return feedService.personalized(user.getPublicId());
    }
}
