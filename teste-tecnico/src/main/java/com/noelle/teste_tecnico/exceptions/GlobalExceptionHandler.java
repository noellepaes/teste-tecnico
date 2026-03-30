package com.noelle.teste_tecnico.exceptions;


import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static Map<String, String> body(String message) {
		return Map.of("message", message != null ? message : "");
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<Map<String, String>> handlerMethodValidation(HandlerMethodValidationException ex) {
		List<String> parts = new ArrayList<>();
		for (ParameterValidationResult result : ex.getParameterValidationResults()) {
			if (result instanceof ParameterErrors errors) {
				errors.getFieldErrors().forEach(fe -> parts.add(fe.getField() + ": " + fe.getDefaultMessage()));
			} else {
				for (MessageSourceResolvable r : result.getResolvableErrors()) {
					if (r instanceof FieldError fe) {
						parts.add(fe.getField() + ": " + fe.getDefaultMessage());
					} else if (r.getDefaultMessage() != null) {
						parts.add(r.getDefaultMessage());
					}
				}
			}
		}
		ex.getCrossParameterValidationResults()
				.forEach(r -> parts.add(r.getDefaultMessage() != null ? r.getDefaultMessage() : r.toString()));
		return ResponseEntity.badRequest().body(body(String.join("; ", parts)));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
		String msg =
				ex.getBindingResult().getFieldErrors().stream()
						.map(err -> err.getField() + ": " + err.getDefaultMessage())
						.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(body(msg));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> notReadable(HttpMessageNotReadableException ex) {
		String hint = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
		return ResponseEntity.badRequest().body(body(hint != null ? hint : "JSON inválido"));
	}

	@ExceptionHandler(CouponValidationException.class)
	public ResponseEntity<Map<String, String>> couponValidation(CouponValidationException ex) {
		return ResponseEntity.badRequest().body(body(ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(body(ex.getMessage()));
	}

	@ExceptionHandler(CouponAlreadyDeletedException.class)
	public ResponseEntity<Map<String, String>> conflict(CouponAlreadyDeletedException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex.getMessage()));
	}

	@ExceptionHandler(CouponNotFoundException.class)
	public ResponseEntity<Map<String, String>> notFound(CouponNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
	}
}
