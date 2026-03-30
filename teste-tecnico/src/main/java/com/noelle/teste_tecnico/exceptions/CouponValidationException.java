package com.noelle.teste_tecnico.exceptions;

/** Regra de negócio do cupom inválida (resposta HTTP 400). */
public abstract class CouponValidationException extends RuntimeException {

	protected CouponValidationException(String message) {
		super(message);
	}
}
