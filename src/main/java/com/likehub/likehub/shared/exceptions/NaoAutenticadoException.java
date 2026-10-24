package com.likehub.likehub.shared.exceptions;

import org.springframework.http.HttpStatus;

public class NaoAutenticadoException extends ErroException {

	private static final String CODIGO_PADRAO = "NAO_AUTENTICADO";

	public NaoAutenticadoException(String message) {
		super(HttpStatus.UNAUTHORIZED, CODIGO_PADRAO, message);
	}

	public NaoAutenticadoException(String errorCode, String message) {
		super(HttpStatus.UNAUTHORIZED, errorCode, message);
	}
}