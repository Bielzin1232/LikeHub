package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends ErroException {

	private static final String CODIGO_PADRAO = "RECURSO_NAO_ENCONTRADO";

	public RecursoNaoEncontradoException(String message) {
		super(HttpStatus.NOT_FOUND, CODIGO_PADRAO, message);
	}

	public RecursoNaoEncontradoException(String errorCode, String message) {
		super(HttpStatus.NOT_FOUND, errorCode, message);
	}
}