package com.likehub.likehub.storage;

import com.likehub.likehub.shared.exceptions.ErroInternoException;

public class StorageException extends ErroInternoException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}