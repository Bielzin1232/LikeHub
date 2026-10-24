package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

public class ConflitoException extends ErroException {

	private static final String CODIGO_PADRAO = "CONFLITO";

	public ConflitoException(String message) {
		super(HttpStatus.CONFLICT, CODIGO_PADRAO, message);
	}

	public ConflitoException(String errorCode, String message) {
		super(HttpStatus.CONFLICT, errorCode, message);
	}
}