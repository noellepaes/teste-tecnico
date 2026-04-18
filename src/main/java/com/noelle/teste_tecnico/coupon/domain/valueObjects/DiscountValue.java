package com.noelle.teste_tecnico.coupon.domain.valueObjects;

import com.noelle.teste_tecnico.coupon.domain.exception.MinimumDiscountNotMetException;

import java.math.BigDecimal;

public record DiscountValue(BigDecimal value) {

	private static final BigDecimal MIN = new BigDecimal("0.5");

	public DiscountValue {
		if (value == null || value.compareTo(MIN) < 0) {
			throw new MinimumDiscountNotMetException(value != null ? value : BigDecimal.ZERO);
		}
	}
}
