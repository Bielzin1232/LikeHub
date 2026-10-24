package com.likehub.likehub.shared.exceptions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converte qualquer exceção em ProblemDetail (RFC 9457).
 *
 * Estende ResponseEntityExceptionHandler para que erros do próprio Spring MVC
 * (405, 415, JSON malformado, parâmetro faltando, 404 de rota...) tenham o
 * status correto em vez de cair no handler genérico e virar 500.
 *
 * Não ative spring.mvc.problemdetails.enabled: conflita com esta classe.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String MENSAGEM_INTERNA = "Ocorreu um erro interno.";

	// ---------- Exceções de negócio (todas herdam de ErroException) ----------

	@ExceptionHandler(ErroException.class)
	ProblemDetail handleErro(ErroException ex, HttpServletRequest request) {
		HttpStatus status = ex.getStatus();
		if (status.is5xxServerError()) {
			log.error("Erro interno em {} {}", request.getMethod(), request.getRequestURI(), ex);
			return problem(status, MENSAGEM_INTERNA, ex.getErrorCode(), request);
		}
		log.debug("{} em {} {}: {}", ex.getErrorCode(), request.getMethod(),
				request.getRequestURI(), ex.getMessage());
		return problem(status, ex.getMessage(), ex.getErrorCode(), request);
	}

	// ---------- Spring Security (lançadas dentro do controller, ex.: @PreAuthorize) ----------

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleSecurityForbidden(AccessDeniedException ex, HttpServletRequest request) {
		return problem(HttpStatus.FORBIDDEN, "Acesso negado.", "ACESSO_NEGADO", request);
	}

	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail handleSecurityUnauthorized(AuthenticationException ex, HttpServletRequest request) {
		return problem(HttpStatus.UNAUTHORIZED, "Autenticação necessária.", "NAO_AUTENTICADO", request);
	}

	// ---------- Banco e validação de parâmetros ----------

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
		log.warn("Violação de integridade em {} {}: {}", request.getMethod(),
				request.getRequestURI(), ex.getMostSpecificCause().getMessage());
		return problem(HttpStatus.CONFLICT,
				"Os dados informados entram em conflito com um registro existente.",
				"CONFLITO_DE_DADOS", request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
		return problem(HttpStatus.BAD_REQUEST, "Parâmetros inválidos.", "PARAMETROS_INVALIDOS", request);
	}

	// ---------- Fallback: qualquer outra coisa é bug nosso ----------

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_INTERNA, "ERRO_INTERNO", request);
	}

	// ---------- Ajustes nos erros que a classe base do Spring já trata ----------

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		ProblemDetail pd = ex.getBody();
		pd.setDetail("Dados inválidos.");
		pd.setProperty("errorCode", "DADOS_INVALIDOS");
		pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						error -> String.valueOf(error.getDefaultMessage()),
						(a, b) -> a + "; " + b,
						LinkedHashMap::new)));
		return handleExceptionInternal(ex, pd, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status,
				"Corpo da requisição ausente ou malformado.");
		pd.setProperty("errorCode", "CORPO_INVALIDO");
		return handleExceptionInternal(ex, pd, headers, status, request);
	}

	/** Acrescenta timestamp e path em TODA resposta de erro da classe base. */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		if (body instanceof ProblemDetail pd) {
			pd.setProperty("timestamp", Instant.now());
			if (request instanceof ServletWebRequest servletRequest) {
				pd.setProperty("path", servletRequest.getRequest().getRequestURI());
			}
		}
		return super.handleExceptionInternal(ex, body, headers, statusCode, request);
	}

	// ---------- Montagem do ProblemDetail ----------

	private ProblemDetail problem(HttpStatus status, String detail, String errorCode,
	                              HttpServletRequest request) {
		String message = (detail == null || detail.isBlank()) ? status.getReasonPhrase() : detail;
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
		problem.setTitle(status.getReasonPhrase());
		problem.setProperty("errorCode", errorCode);
		problem.setProperty("timestamp", Instant.now());
		problem.setProperty("path", request.getRequestURI());
		return problem;
	}
}