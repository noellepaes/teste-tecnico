package com.noelle.teste_tecnico.coupon.domain.valueObjects;

import com.noelle.teste_tecnico.coupon.domain.exception.InvalidCouponCodeException;

public record Code(String value) {

	private static final int LENGTH = 6;

	public Code {
		if (value == null || value.isBlank()) {
			throw new InvalidCouponCodeException("Código obrigatório.");
		}
		if (!value.matches("^[a-zA-Z0-9]{%d}$".formatted(LENGTH))) {
			throw new InvalidCouponCodeException("Código deve ter exatamente 6 caracteres alfanuméricos.");
		}
	}
}
