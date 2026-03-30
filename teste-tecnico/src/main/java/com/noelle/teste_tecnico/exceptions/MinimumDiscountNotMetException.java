package com.noelle.teste_tecnico.exceptions;

import java.math.BigDecimal;

public class MinimumDiscountNotMetException extends CouponValidationException {

	public MinimumDiscountNotMetException(BigDecimal value) {
		super("O valor de desconto deve ser no mínimo 0,5. Valor informado: " + value);
	}
}
