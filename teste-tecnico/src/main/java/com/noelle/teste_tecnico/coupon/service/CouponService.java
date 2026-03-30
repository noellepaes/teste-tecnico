package com.noelle.teste_tecnico.coupon.service;

import com.noelle.teste_tecnico.coupon.domain.Coupon;
import com.noelle.teste_tecnico.coupon.dto.CouponCreateRequest;
import com.noelle.teste_tecnico.coupon.dto.CouponResponse;
import com.noelle.teste_tecnico.coupon.entity.CouponEntity;
import com.noelle.teste_tecnico.coupon.mapper.CouponMapper;
import com.noelle.teste_tecnico.coupon.repository.CouponRepository;
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

	@Transactional
	public CouponResponse create(CouponCreateRequest request) {
		Coupon coupon =
				Coupon.createNew(
						request.code(),
						request.description(),
						request.discountValue(),
						request.expirationDate(),
						Boolean.TRUE.equals(request.published()),
						clock);
		CouponEntity saved = couponRepository.save(couponMapper.toEntity(coupon));
		return couponMapper.toDTO(saved);
	}

	@Transactional
	public void delete(UUID id) {
		CouponEntity entity = couponRepository.findById(id).orElseThrow(() -> new CouponNotFoundException(id));
		Coupon coupon =
				Coupon.restore(
						entity.getId(),
						entity.getCode(),
						entity.getDescription(),
						entity.getDiscountValue(),
						entity.getExpirationDate(),
						entity.getStatus(),
						entity.isPublished(),
						entity.isRedeemed());
		Coupon deleted = coupon.markDeleted();
		entity.setStatus(deleted.status());
		couponRepository.save(entity);
	}
}
