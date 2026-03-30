package com.noelle.teste_tecnico.exceptions;

public class ExpirationInPastException extends CouponValidationException {

	public ExpirationInPastException() {
		super("A data de expiração não pode ser no passado.");
	}
}
