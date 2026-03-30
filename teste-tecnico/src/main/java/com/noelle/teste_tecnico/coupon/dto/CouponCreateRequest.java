package com.noelle.teste_tecnico.coupon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Corpo para criação de cupom")
public record CouponCreateRequest(
		@NotBlank
		@Schema(
				description =
						"Código de entrada; caracteres especiais são removidos. Após a limpeza deve restar exatamente 6 caracteres alfanuméricos.",
				example = "ABC-123",
				requiredMode = Schema.RequiredMode.REQUIRED)
		String code,
		@NotBlank
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		String description,
		@NotNull
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0.5", description = "Mínimo 0,5 (validado no serviço)")
		BigDecimal discountValue,
		@NotNull
		@Schema(
				requiredMode = Schema.RequiredMode.REQUIRED,
				description = "ISO-8601 em UTC; deve ser no presente ou futuro",
				example = "2030-12-31T23:59:59.000Z")
		Instant expirationDate,
		@JsonProperty(defaultValue = "false")
		@Schema(defaultValue = "false")
		Boolean published) {

	public CouponCreateRequest {
		if (published == null) {
			published = false;
		}
	}
}
