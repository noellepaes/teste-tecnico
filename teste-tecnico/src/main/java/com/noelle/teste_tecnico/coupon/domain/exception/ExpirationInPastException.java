package com.noelle.teste_tecnico.coupon.domain.exception;

public class ExpirationInPastException extends CouponDomainException {

	public ExpirationInPastException() {
		super("A data de expiração não pode ser no passado.");
	}
}
