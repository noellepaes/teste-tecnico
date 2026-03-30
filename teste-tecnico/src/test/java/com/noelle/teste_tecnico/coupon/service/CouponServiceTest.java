package com.noelle.teste_tecnico.coupon.service;

import com.noelle.teste_tecnico.coupon.CouponStatus;
import com.noelle.teste_tecnico.coupon.dto.CouponCreateRequest;
import com.noelle.teste_tecnico.coupon.entity.CouponEntity;
import com.noelle.teste_tecnico.coupon.mapper.CouponMapper;
import com.noelle.teste_tecnico.coupon.repository.CouponRepository;
import com.noelle.teste_tecnico.exceptions.CouponAlreadyDeletedException;
import com.noelle.teste_tecnico.exceptions.CouponNotFoundException;
import com.noelle.teste_tecnico.exceptions.ExpirationInPastException;
import com.noelle.teste_tecnico.exceptions.InvalidCouponCodeException;
import com.noelle.teste_tecnico.exceptions.MinimumDiscountNotMetException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

	private static final Clock CLOCK =
			Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);

	@Mock
	private CouponRepository couponRepository;

	private CouponMapper couponMapper;
	private CouponService couponService;

	@BeforeEach
	void setUp() {
		couponMapper = new CouponMapper();
		couponService = new CouponService(couponRepository, couponMapper, CLOCK);
	}

	@Test
	void normalizeCouponCode_removesSpecials() {
		assertThat(CouponService.normalizeCouponCode("ABC-123")).isEqualTo("ABC123");
		assertThat(CouponService.normalizeCouponCode("ab@12#xy")).isEqualTo("ab12xy");
	}

	@Test
	void normalizeCouponCode_rejectsTooShort() {
		assertThatThrownBy(() -> CouponService.normalizeCouponCode("a-b-1"))
				.isInstanceOf(InvalidCouponCodeException.class);
	}

	@Test
	void normalizeCouponCode_rejectsTooLong() {
		assertThatThrownBy(() -> CouponService.normalizeCouponCode("ABCDEFGH"))
				.isInstanceOf(InvalidCouponCodeException.class);
		assertThatThrownBy(() -> CouponService.normalizeCouponCode("ABC-123-XY"))
				.isInstanceOf(InvalidCouponCodeException.class);
	}

	@Test
	void normalizeCouponCode_rejectsBlank() {
		assertThatThrownBy(() -> CouponService.normalizeCouponCode("   "))
				.isInstanceOf(InvalidCouponCodeException.class);
	}

	@Test
	void create_persistsNormalizedCode() {
		var request =
				new CouponCreateRequest(
						"XY-12-34",
						"desc",
						new BigDecimal("0.5"),
						Instant.parse("2026-12-31T00:00:00Z"),
						false);
		when(couponRepository.save(any(CouponEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		var response = couponService.create(request);

		verify(couponRepository).save(any(CouponEntity.class));
		assertThat(response.code()).isEqualTo("XY1234");
		assertThat(response.status()).isEqualTo(CouponStatus.ACTIVE);
	}

	@Test
	void create_rejectsDiscountBelowMinimum() {
		var request =
				new CouponCreateRequest(
						"ABCDEF",
						"d",
						new BigDecimal("0.49"),
						Instant.parse("2026-12-31T00:00:00Z"),
						false);

		assertThatThrownBy(() -> couponService.create(request)).isInstanceOf(MinimumDiscountNotMetException.class);
	}

	@Test
	void create_acceptsMinimumDiscount() {
		var request =
				new CouponCreateRequest(
						"abcdef",
						"d",
						new BigDecimal("0.5"),
						Instant.parse("2026-12-31T00:00:00Z"),
						false);
		when(couponRepository.save(any(CouponEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		var response = couponService.create(request);

		assertThat(response.discountValue()).isEqualByComparingTo("0.5");
	}

	@Test
	void create_rejectsExpirationInPast() {
		var request =
				new CouponCreateRequest(
						"abcdef",
						"d",
						new BigDecimal("1"),
						Instant.parse("2026-06-01T11:59:59Z"),
						false);

		assertThatThrownBy(() -> couponService.create(request)).isInstanceOf(ExpirationInPastException.class);
	}

	@Test
	void delete_marksEntityDeleted() {
		UUID id = UUID.randomUUID();
		var entity = new CouponEntity();
		entity.setId(id);
		entity.setCode("ABCDEF");
		entity.setDescription("x");
		entity.setDiscountValue(new BigDecimal("1"));
		entity.setExpirationDate(Instant.parse("2027-01-01T00:00:00Z"));
		entity.setStatus(CouponStatus.ACTIVE);
		entity.setPublished(false);
		entity.setRedeemed(false);
		when(couponRepository.findById(id)).thenReturn(Optional.of(entity));
		when(couponRepository.save(any(CouponEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		couponService.delete(id);

		assertThat(entity.getStatus()).isEqualTo(CouponStatus.DELETED);
	}

	@Test
	void delete_unknownId_throws() {
		UUID id = UUID.randomUUID();
		when(couponRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> couponService.delete(id)).isInstanceOf(CouponNotFoundException.class);
	}

	@Test
	void delete_alreadyDeleted_throws() {
		UUID id = UUID.randomUUID();
		var entity = new CouponEntity();
		entity.setId(id);
		entity.setCode("ABCDEF");
		entity.setDescription("x");
		entity.setDiscountValue(new BigDecimal("1"));
		entity.setExpirationDate(Instant.parse("2027-01-01T00:00:00Z"));
		entity.setStatus(CouponStatus.DELETED);
		entity.setPublished(false);
		entity.setRedeemed(false);
		when(couponRepository.findById(id)).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> couponService.delete(id)).isInstanceOf(CouponAlreadyDeletedException.class);
	}
}
