package com.noelle.teste_tecnico.coupon.mapper;

import com.noelle.teste_tecnico.coupon.domain.Coupon;
import com.noelle.teste_tecnico.coupon.dto.CouponResponse;
import com.noelle.teste_tecnico.coupon.entity.CouponEntity;

import org.springframework.stereotype.Component;

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

	public CouponEntity toEntity(Coupon coupon) {
		if (coupon == null) {
			return null;
		}
		CouponEntity entity = new CouponEntity();
		entity.setId(coupon.id());
		entity.setCode(coupon.code());
		entity.setDescription(coupon.description());
		entity.setDiscountValue(coupon.discountValue());
		entity.setExpirationDate(coupon.expirationDate());
		entity.setStatus(coupon.status());
		entity.setPublished(coupon.published());
		entity.setRedeemed(coupon.redeemed());
		return entity;
	}
}
