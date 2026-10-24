package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

/** A mensagem só vai para o log: o cliente sempre recebe um texto genérico. */
public class ErroInternoException extends ErroException {

	private static final String CODIGO_PADRAO = "ERRO_INTERNO";

	public ErroInternoException(String message) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, CODIGO_PADRAO, message);
	}

	public ErroInternoException(String message, Throwable cause) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, CODIGO_PADRAO, message, cause);
	}
}