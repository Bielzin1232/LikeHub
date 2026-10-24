package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

public class LimiteRequisicoesException extends ErroException {

	private static final String CODIGO_PADRAO = "LIMITE_REQUISICOES";

	public LimiteRequisicoesException(String message) {
		super(HttpStatus.TOO_MANY_REQUESTS, CODIGO_PADRAO, message);
	}

	public LimiteRequisicoesException(String errorCode, String message) {
		super(HttpStatus.TOO_MANY_REQUESTS, errorCode, message);
	}
}