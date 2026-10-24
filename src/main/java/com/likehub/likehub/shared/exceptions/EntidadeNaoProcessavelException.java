package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

public class EntidadeNaoProcessavelException extends ErroException {

	private static final String CODIGO_PADRAO = "ENTIDADE_NAO_PROCESSAVEL";

	public EntidadeNaoProcessavelException(String message) {
		super(HttpStatus.UNPROCESSABLE_ENTITY, CODIGO_PADRAO, message);
	}

	public EntidadeNaoProcessavelException(String errorCode, String message) {
		super(HttpStatus.UNPROCESSABLE_ENTITY, errorCode, message);
	}
}