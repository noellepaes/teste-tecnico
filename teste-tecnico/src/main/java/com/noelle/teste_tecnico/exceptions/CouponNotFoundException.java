package com.noelle.teste_tecnico.exceptions;

import java.util.UUID;

public class CouponNotFoundException extends RuntimeException {

	public CouponNotFoundException(UUID id) {
		super("Cupom não encontrado: " + id);
	}
}
