package com.likehub.likehub.images;

import com.likehub.likehub.auth.commons.api.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.likehub.likehub.users.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    @PostMapping(path = ApiPaths.Images.upload, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User usuarioLogado) {
        UUID userId = usuarioLogado.getPublicId();
        UploadedImage img = imageService.upload(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ImageResponse(img.getId(), img.getUrl()));
    }

    public record ImageResponse(UUID id, String url) {}
}