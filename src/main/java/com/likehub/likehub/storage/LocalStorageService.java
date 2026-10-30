package com.likehub.likehub.storage;

import com.likehub.likehub.shared.exceptions.ErroInternoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
@Profile("dev")
public class LocalStorageService implements StorageService {

    private final Path root;
    private final String publicBaseUrl;

    public LocalStorageService(
            @Value("${app.storage.local.root}") String root,
            @Value("${app.storage.local.public-base-url}") String publicBaseUrl) throws IOException {
        this.root = Paths.get(root).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl;
        Files.createDirectories(this.root);
    }

    @Override
    public StoredFile upload(MultipartFile file) {
        String ext = extractExtension(file.getOriginalFilename());
        String key = "posts/" + UUID.randomUUID() + ext;
        Path target = root.resolve(key).normalize();

        // defesa contra path traversal
        if (!target.startsWith(root)) {
            throw new ErroInternoException("Chave de armazenamento inválida.");
        }

        try {
            Files.createDirectories(target.getParent());
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Falha ao salvar arquivo", e);
        }

        String url = publicBaseUrl + "/" + key;
        return new StoredFile(url, key);
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(root.resolve(storageKey).normalize());
        } catch (IOException e) {
            log.warn("Falha ao deletar {}", storageKey, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        String ext = filename.substring(dot).toLowerCase();
        return switch (ext) {
            case ".jpg", ".jpeg", ".png", ".webp", ".gif" -> ext;
            default -> "";
        };
    }
}