package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

public class AcessoNegadoException extends ErroException {

	private static final String CODIGO_PADRAO = "ACESSO_NEGADO";

	public AcessoNegadoException(String message) {
		super(HttpStatus.FORBIDDEN, CODIGO_PADRAO, message);
	}

	public AcessoNegadoException(String errorCode, String message) {
		super(HttpStatus.FORBIDDEN, errorCode, message);
	}
}