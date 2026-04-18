package com.noelle.teste_tecnico.coupon.domain.valueObjects;

import com.noelle.teste_tecnico.coupon.domain.exception.ExpirationInPastException;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public record ExpirationDate(Instant value) {

	public ExpirationDate {
		Objects.requireNonNull(value, "expirationDate");
	}

	public ExpirationDate(Instant value, Clock clock) {
		this(value);
		if (value.isBefore(clock.instant())) {
			throw new ExpirationInPastException();
		}
	}
}
