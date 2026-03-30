package com.noelle.teste_tecnico.coupon.domain.exception;

/** Erro de regra de negócio do cupom (resposta HTTP 400). */
public abstract class CouponDomainException extends RuntimeException {

	protected CouponDomainException(String message) {
		super(message);
	}
}
