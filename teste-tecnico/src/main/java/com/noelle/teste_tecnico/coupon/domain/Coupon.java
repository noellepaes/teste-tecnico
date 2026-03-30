package com.noelle.teste_tecnico.coupon.domain;

import com.noelle.teste_tecnico.coupon.domain.exception.ExpirationInPastException;
import com.noelle.teste_tecnico.coupon.domain.exception.MinimumDiscountNotMetException;
import com.noelle.teste_tecnico.exceptions.CouponAlreadyDeletedException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Objeto de domínio do cupom — encapsula regras de criação e exclusão lógica.
 * DTOs só transportam dados da API; a validação de negócio fica aqui.
 */
public record Coupon(
		UUID id,
		String code,
		String description,
		BigDecimal discountValue,
		Instant expirationDate,
		CouponStatus status,
		boolean published,
		boolean redeemed) {

	private static final BigDecimal MIN_DISCOUNT = new BigDecimal("0.5");

	public static Coupon createNew(
			String rawCode,
			String description,
			BigDecimal discountValue,
			Instant expirationDate,
			boolean published,
			Clock clock) {
		String code = CouponCode.normalize(rawCode);
		validateDiscount(discountValue);
		validateExpiration(expirationDate, clock);
		return new Coupon(
				UUID.randomUUID(),
				code,
				description,
				discountValue,
				expirationDate,
				CouponStatus.ACTIVE,
				published,
				false);
	}

	/** Reidrata a partir da persistência (sem revalidar datas passadas de cupons já salvos). */
	public static Coupon restore(
			UUID id,
			String code,
			String description,
			BigDecimal discountValue,
			Instant expirationDate,
			CouponStatus status,
			boolean published,
			boolean redeemed) {
		return new Coupon(id, code, description, discountValue, expirationDate, status, published, redeemed);
	}

	private static void validateDiscount(BigDecimal discountValue) {
		if (discountValue == null || discountValue.compareTo(MIN_DISCOUNT) < 0) {
			throw new MinimumDiscountNotMetException(discountValue != null ? discountValue : BigDecimal.ZERO);
		}
	}

	private static void validateExpiration(Instant expirationDate, Clock clock) {
		Objects.requireNonNull(expirationDate, "expirationDate");
		if (expirationDate.isBefore(clock.instant())) {
			throw new ExpirationInPastException();
		}
	}

	public Coupon markDeleted() {
		if (status == CouponStatus.DELETED) {
			throw new CouponAlreadyDeletedException();
		}
		return new Coupon(id, code, description, discountValue, expirationDate, CouponStatus.DELETED, published, redeemed);
	}
}
