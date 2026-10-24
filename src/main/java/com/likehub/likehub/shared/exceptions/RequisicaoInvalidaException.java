package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

public class RequisicaoInvalidaException extends ErroException {

	private static final String CODIGO_PADRAO = "REQUISICAO_INVALIDA";

	public RequisicaoInvalidaException(String message) {
		super(HttpStatus.BAD_REQUEST, CODIGO_PADRAO, message);
	}

	public RequisicaoInvalidaException(String errorCode, String message) {
		super(HttpStatus.BAD_REQUEST, errorCode, message);
	}
}