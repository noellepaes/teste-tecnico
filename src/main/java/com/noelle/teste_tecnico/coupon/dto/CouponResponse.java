package com.noelle.teste_tecnico.coupon.dto;

import com.noelle.teste_tecnico.coupon.domain.CouponStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Cupom persistido")
public record CouponResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		UUID id,
		@Schema(
				requiredMode = Schema.RequiredMode.REQUIRED,
				minLength = 6,
				maxLength = 6,
				description = "6 caracteres alfanuméricos (especiais já removidos na criação)")
		String code,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		String description,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		BigDecimal discountValue,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Instant expirationDate,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"ACTIVE", "INACTIVE", "DELETED"})
		CouponStatus status,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		boolean published,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		boolean redeemed) {
}
