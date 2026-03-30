package com.noelle.teste_tecnico.coupon.domain;

import com.noelle.teste_tecnico.coupon.domain.exception.InvalidCouponCodeException;

/**
 * Código do cupom no domínio: remove não alfanuméricos; o resultado deve ter exatamente 6 caracteres.
 */
public final class CouponCode {

	public static final int LENGTH = 6;

	private static final String MSG_EXACT_LENGTH =
			"Após remover caracteres especiais, o código deve ter exatamente 6 caracteres alfanuméricos.";

	private CouponCode() {
	}

	public static String normalize(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new InvalidCouponCodeException("Código obrigatório.");
		}
		String cleaned =
				raw.codePoints()
						.filter(Character::isLetterOrDigit)
						.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
						.toString();
		if (cleaned.length() != LENGTH) {
			throw new InvalidCouponCodeException(MSG_EXACT_LENGTH);
		}
		return cleaned;
	}
}
