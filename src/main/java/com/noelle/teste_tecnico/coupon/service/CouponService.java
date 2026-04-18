package com.noelle.teste_tecnico.coupon.service;

import com.noelle.teste_tecnico.coupon.dto.CouponCreateRequest;
import com.noelle.teste_tecnico.coupon.dto.CouponResponse;
import com.noelle.teste_tecnico.coupon.domain.valueObjects.Code;
import com.noelle.teste_tecnico.coupon.domain.valueObjects.CouponStatus;
import com.noelle.teste_tecnico.coupon.domain.valueObjects.DiscountValue;
import com.noelle.teste_tecnico.coupon.domain.valueObjects.ExpirationDate;
import com.noelle.teste_tecnico.coupon.entity.CouponEntity;
import com.noelle.teste_tecnico.coupon.mapper.CouponMapper;
import com.noelle.teste_tecnico.coupon.repository.CouponRepository;
import com.noelle.teste_tecnico.exceptions.CouponAlreadyDeletedException;
import com.noelle.teste_tecnico.exceptions.CouponNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class CouponService {

	private final CouponRepository couponRepository;
	private final CouponMapper couponMapper;
	private final Clock clock;

	public CouponService(CouponRepository couponRepository, CouponMapper couponMapper, Clock clock) {
		this.couponRepository = couponRepository;
		this.couponMapper = couponMapper;
		this.clock = clock;
	}

	@Transactional(rollbackFor = Exception.class)
	public CouponResponse create(CouponCreateRequest request) {
		Code code = new Code(request.code());
		DiscountValue discountValue = new DiscountValue(request.discountValue());
		ExpirationDate expirationDate = new ExpirationDate(request.expirationDate(), clock);

		CouponEntity entity =
				couponMapper.toEntity(
						UUID.randomUUID(),
						code.value(),
						request.description(),
						discountValue.value(),
						expirationDate.value(),
						CouponStatus.ACTIVE,
						Boolean.TRUE.equals(request.published()),
						false);

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
}
