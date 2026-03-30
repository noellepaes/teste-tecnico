package com.noelle.teste_tecnico.coupon.service;

import com.noelle.teste_tecnico.coupon.CouponStatus;
import com.noelle.teste_tecnico.coupon.dto.CouponCreateRequest;
import com.noelle.teste_tecnico.coupon.dto.CouponResponse;
import com.noelle.teste_tecnico.coupon.entity.CouponEntity;
import com.noelle.teste_tecnico.coupon.mapper.CouponMapper;
import com.noelle.teste_tecnico.coupon.repository.CouponRepository;
import com.noelle.teste_tecnico.exceptions.CouponAlreadyDeletedException;
import com.noelle.teste_tecnico.exceptions.CouponNotFoundException;
import com.noelle.teste_tecnico.exceptions.ExpirationInPastException;
import com.noelle.teste_tecnico.exceptions.InvalidCouponCodeException;
import com.noelle.teste_tecnico.exceptions.MinimumDiscountNotMetException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class CouponService {

	private static final int CODE_LENGTH = 6;
	private static final BigDecimal MIN_DISCOUNT = new BigDecimal("0.5");
	private static final String MSG_CODE_EXACT =
			"Após remover caracteres especiais, o código deve ter exatamente 6 caracteres alfanuméricos.";

	private final CouponRepository couponRepository;
	private final CouponMapper couponMapper;
	private final Clock clock;

	public CouponService(CouponRepository couponRepository, CouponMapper couponMapper, Clock clock) {
		this.couponRepository = couponRepository;
		this.couponMapper = couponMapper;
		this.clock = clock;
	}

	@Transactional
	public CouponResponse create(CouponCreateRequest request) {
		String code = normalizeCouponCode(request.code());
		validateDiscount(request.discountValue());
		validateExpiration(request.expirationDate());
		boolean published = Boolean.TRUE.equals(request.published());
		CouponEntity entity =
				couponMapper.toNewEntity(
						UUID.randomUUID(),
						code,
						request.description(),
						request.discountValue(),
						request.expirationDate(),
						published);
		CouponEntity saved = couponRepository.save(entity);
		return couponMapper.toDTO(saved);
	}

	@Transactional
	public void delete(UUID id) {
		CouponEntity entity = couponRepository.findById(id).orElseThrow(() -> new CouponNotFoundException(id));
		if (entity.getStatus() == CouponStatus.DELETED) {
			throw new CouponAlreadyDeletedException();
		}
		entity.setStatus(CouponStatus.DELETED);
		couponRepository.save(entity);
	}

	static String normalizeCouponCode(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new InvalidCouponCodeException("Código obrigatório.");
		}
		String cleaned =
				raw.codePoints()
						.filter(Character::isLetterOrDigit)
						.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
						.toString();
		if (cleaned.length() != CODE_LENGTH) {
			throw new InvalidCouponCodeException(MSG_CODE_EXACT);
		}
		return cleaned;
	}

	private void validateDiscount(BigDecimal discountValue) {
		if (discountValue == null || discountValue.compareTo(MIN_DISCOUNT) < 0) {
			throw new MinimumDiscountNotMetException(discountValue != null ? discountValue : BigDecimal.ZERO);
		}
	}

	private void validateExpiration(Instant expirationDate) {
		Objects.requireNonNull(expirationDate, "expirationDate");
		if (expirationDate.isBefore(clock.instant())) {
			throw new ExpirationInPastException();
		}
	}
}
