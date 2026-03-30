package com.noelle.teste_tecnico.exceptions;

public class CouponAlreadyDeletedException extends RuntimeException {

	public CouponAlreadyDeletedException() {
		super("O cupom já foi excluído.");
	}
}
