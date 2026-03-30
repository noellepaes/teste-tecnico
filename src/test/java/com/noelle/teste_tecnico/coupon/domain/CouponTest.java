package com.noelle.teste_tecnico.coupon.domain;

import com.noelle.teste_tecnico.coupon.domain.exception.ExpirationInPastException;
import com.noelle.teste_tecnico.coupon.domain.exception.MinimumDiscountNotMetException;
import com.noelle.teste_tecnico.exceptions.CouponAlreadyDeletedException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

	private static final Clock CLOCK =
			Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);

	@Test
	void createNew_normalizesCodeAndSetsActive() {
		Coupon c =
				Coupon.createNew(
						"ab@12#xy",
						"desc",
						new BigDecimal("0.5"),
						Instant.parse("2026-12-31T23:59:59Z"),
						true,
						CLOCK);
		assertThat(c.code()).isEqualTo("ab12xy");
		assertThat(c.status()).isEqualTo(CouponStatus.ACTIVE);
		assertThat(c.published()).isTrue();
		assertThat(c.redeemed()).isFalse();
	}

	@Test
	void createNew_rejectsDiscountBelowMinimum() {
		assertThatThrownBy(
						() ->
								Coupon.createNew(
										"abcdef",
										"d",
										new BigDecimal("0.49"),
										Instant.parse("2026-12-31T00:00:00Z"),
										false,
										CLOCK))
				.isInstanceOf(MinimumDiscountNotMetException.class);
	}

	@Test
	void createNew_acceptsMinimumDiscount() {
		Coupon c =
				Coupon.createNew(
						"abcdef",
						"d",
						new BigDecimal("0.5"),
						Instant.parse("2026-12-31T00:00:00Z"),
						false,
						CLOCK);
		assertThat(c.discountValue()).isEqualByComparingTo("0.5");
	}

	@Test
	void createNew_rejectsExpirationInPast() {
		assertThatThrownBy(
						() ->
								Coupon.createNew(
										"abcdef",
										"d",
										new BigDecimal("1"),
										Instant.parse("2026-06-01T11:59:59Z"),
										false,
										CLOCK))
				.isInstanceOf(ExpirationInPastException.class);
	}

	@Test
	void markDeleted_setsDeletedStatus() {
		Coupon c =
				Coupon.createNew(
						"abcdef",
						"d",
						new BigDecimal("1"),
						Instant.parse("2026-12-31T00:00:00Z"),
						false,
						CLOCK);
		Coupon deleted = c.markDeleted();
		assertThat(deleted.status()).isEqualTo(CouponStatus.DELETED);
		assertThat(deleted.id()).isEqualTo(c.id());
	}

	@Test
	void markDeleted_twiceThrows() {
		Coupon c =
				Coupon.createNew(
						"abcdef",
						"d",
						new BigDecimal("1"),
						Instant.parse("2026-12-31T00:00:00Z"),
						false,
						CLOCK);
		Coupon deleted = c.markDeleted();
		assertThatThrownBy(deleted::markDeleted).isInstanceOf(CouponAlreadyDeletedException.class);
	}

	@Test
	void restore_roundTrip() {
		UUID id = UUID.randomUUID();
		Coupon c =
				Coupon.restore(
						id,
						"ABC123",
						"x",
						new BigDecimal("1"),
						Instant.parse("2027-01-01T00:00:00Z"),
						CouponStatus.ACTIVE,
						false,
						false);
		assertThat(c.id()).isEqualTo(id);
		assertThat(c.code()).isEqualTo("ABC123");
	}
}
