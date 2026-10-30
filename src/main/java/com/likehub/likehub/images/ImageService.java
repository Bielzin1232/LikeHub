package com.likehub.likehub.images;

import com.likehub.likehub.storage.StorageService;
import com.likehub.likehub.shared.exceptions.RecursoNaoEncontradoException;
import com.likehub.likehub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final StorageService storage;
    private final UploadedImageRepository repo;
    private final UserRepository userRepository;

    @Transactional
    public UploadedImage upload(MultipartFile file, UUID ownerId) {
        ImageValidator.validate(file);

        StorageService.StoredFile stored = storage.upload(file);

        UploadedImage img = new UploadedImage();
        img.setOwner(userRepository.findByPublicId(ownerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "USUARIO_NAO_ENCONTRADO",
                        "O usuário informado não existe.")));
        img.setUrl(stored.url());
        img.setStorageKey(stored.storageKey());
        img.setStatus(ImageStatus.PENDING);

        try {
            return repo.save(img);
        } catch (RuntimeException e) {
            storage.delete(stored.storageKey());
            throw e;
        }
    }
}