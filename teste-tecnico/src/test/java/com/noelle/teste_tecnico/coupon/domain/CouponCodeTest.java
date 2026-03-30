package com.noelle.teste_tecnico.coupon.domain;

import com.noelle.teste_tecnico.coupon.domain.exception.InvalidCouponCodeException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponCodeTest {

	@Test
	void normalize_removesSpecialsAndKeepsSix() {
		assertThat(CouponCode.normalize("ABC-123")).isEqualTo("ABC123");
		assertThat(CouponCode.normalize("ab@12#xy")).isEqualTo("ab12xy");
	}

	@Test
	void normalize_rejectsTooLong() {
		assertThatThrownBy(() -> CouponCode.normalize("ABCDEFGH")).isInstanceOf(InvalidCouponCodeException.class);
		assertThatThrownBy(() -> CouponCode.normalize("ABC-123-XY")).isInstanceOf(InvalidCouponCodeException.class);
	}

	@Test
	void normalize_rejectsTooShort() {
		assertThatThrownBy(() -> CouponCode.normalize("a-b-1")).isInstanceOf(InvalidCouponCodeException.class);
	}

	@Test
	void normalize_rejectsBlank() {
		assertThatThrownBy(() -> CouponCode.normalize("   ")).isInstanceOf(InvalidCouponCodeException.class);
	}
}
