package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base de todas as exceções de negócio. Carrega o status HTTP e um código
 * estável (ex.: "USUARIO_NAO_ENCONTRADO") que o frontend pode usar em vez de
 * interpretar a mensagem.
 */
public class ErroException extends RuntimeException {

	private final HttpStatus status;
	private final String errorCode;

	public ErroException(HttpStatus status, String errorCode, String message) {
		super(message);
		this.status = status;
		this.errorCode = errorCode;
	}

	public ErroException(HttpStatus status, String errorCode, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
		this.errorCode = errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getErrorCode() {
		return errorCode;
	}
}