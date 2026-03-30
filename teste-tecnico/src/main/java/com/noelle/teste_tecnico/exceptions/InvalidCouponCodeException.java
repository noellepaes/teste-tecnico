package com.noelle.teste_tecnico.exceptions;

public class InvalidCouponCodeException extends CouponValidationException {

	public InvalidCouponCodeException(String message) {
		super(message);
	}
}
