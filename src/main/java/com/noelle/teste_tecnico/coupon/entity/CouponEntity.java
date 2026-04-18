package com.noelle.teste_tecnico.coupon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.noelle.teste_tecnico.coupon.domain.valueObjects.CouponStatus;

@Entity
@Table(name = "coupons")
public class CouponEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 6)
	private String code;

	@Lob
	@Column(nullable = false)
	private String description;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal discountValue;

	@Column(nullable = false)
	private Instant expirationDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CouponStatus status;

	@Column(nullable = false)
	private boolean published;

	@Column(nullable = false)
	private boolean redeemed;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(BigDecimal discountValue) {
		this.discountValue = discountValue;
	}

	public Instant getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Instant expirationDate) {
		this.expirationDate = expirationDate;
	}

	public CouponStatus getStatus() {
		return status;
	}

	public void setStatus(CouponStatus status) {
		this.status = status;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isRedeemed() {
		return redeemed;
	}

	public void setRedeemed(boolean redeemed) {
		this.redeemed = redeemed;
	}
}
