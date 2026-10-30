package com.likehub.likehub.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartFile;


public interface StorageService {
    StoredFile upload(MultipartFile file);

    void delete(String storageKey);

    record StoredFile(String url, String storageKey) {}
}