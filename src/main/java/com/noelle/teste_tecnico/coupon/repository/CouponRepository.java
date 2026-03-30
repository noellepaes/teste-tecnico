package com.noelle.teste_tecnico.coupon.repository;

import com.noelle.teste_tecnico.coupon.entity.CouponEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {
}
