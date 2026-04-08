package com.noelle.teste_tecnico.coupon.mapper;

import com.noelle.teste_tecnico.coupon.domain.CouponStatus;
import com.noelle.teste_tecnico.coupon.dto.CouponResponse;
import com.noelle.teste_tecnico.coupon.entity.CouponEntity;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Converte entre entidade JPA, DTO de saída e objeto de domínio (sem MapStruct). */
@Component
public class CouponMapper {

	public CouponResponse toDTO(CouponEntity entity) {
		if (entity == null) {
			return null;
		}
		return new CouponResponse(
				entity.getId(),
				entity.getCode(),
				entity.getDescription(),
				entity.getDiscountValue(),
				entity.getExpirationDate(),
				entity.getStatus(),
				entity.isPublished(),
				entity.isRedeemed());
	}

	public CouponEntity toEntity(
			UUID id,
			String code,
			String description,
			BigDecimal discountValue,
			Instant expirationDate,
			CouponStatus status,
			boolean published,
			boolean redeemed) {
		CouponEntity entity = new CouponEntity();
		entity.setId(id);
		entity.setCode(code);
		entity.setDescription(description);
		entity.setDiscountValue(discountValue);
		entity.setExpirationDate(expirationDate);
		entity.setStatus(status);
		entity.setPublished(published);
		entity.setRedeemed(redeemed);
		return entity;
	}

}
