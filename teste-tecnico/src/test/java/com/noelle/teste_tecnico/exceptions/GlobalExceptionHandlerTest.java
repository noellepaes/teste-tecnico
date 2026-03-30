package com.noelle.teste_tecnico.exceptions;

import com.noelle.teste_tecnico.coupon.dto.CouponCreateRequest;
import com.noelle.teste_tecnico.web.CouponController;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void couponValidation_returnsMessage() {
		ResponseEntity<Map<String, String>> r =
				handler.couponValidation(new InvalidCouponCodeException("código inválido"));
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(r.getBody()).containsEntry("message", "código inválido");
	}

	@Test
	void couponValidation_minimumDiscount() {
		ResponseEntity<Map<String, String>> r =
				handler.couponValidation(new MinimumDiscountNotMetException(BigDecimal.ZERO));
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(r.getBody().get("message")).contains("0,5");
	}

	@Test
	void couponValidation_expirationInPast() {
		ResponseEntity<Map<String, String>> r = handler.couponValidation(new ExpirationInPastException());
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(r.getBody().get("message")).contains("passado");
	}

	@Test
	void notFound_returns404() {
		UUID id = UUID.randomUUID();
		ResponseEntity<Map<String, String>> r = handler.notFound(new CouponNotFoundException(id));
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(r.getBody().get("message")).contains(id.toString());
	}

	@Test
	void conflict_returns409() {
		ResponseEntity<Map<String, String>> r = handler.conflict(new CouponAlreadyDeletedException());
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(r.getBody()).containsEntry("message", "O cupom já foi excluído.");
	}

	@Test
	void illegalArgument_returns400() {
		ResponseEntity<Map<String, String>> r = handler.badRequest(new IllegalArgumentException("ops"));
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(r.getBody()).containsEntry("message", "ops");
	}

	@Test
	void methodArgumentNotValid_joinsFieldErrors() throws Exception {
		Method m = CouponController.class.getMethod("create", CouponCreateRequest.class);
		MethodParameter mp = new MethodParameter(m, 0);
		CouponCreateRequest target =
				new CouponCreateRequest(
						"",
						"x",
						new BigDecimal("1"),
						Instant.parse("2030-01-01T00:00:00Z"),
						false);
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, "couponCreateRequest");
		errors.addError(new FieldError("couponCreateRequest", "code", "", false, null, null, "obrigatorio"));
		MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, errors);
		ResponseEntity<Map<String, String>> r = handler.validation(ex);
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(r.getBody().get("message")).contains("code");
		assertThat(r.getBody().get("message")).contains("obrigatorio");
	}

	@Test
	void notReadable_usesCauseMessage() {
		HttpMessageNotReadableException ex =
				new HttpMessageNotReadableException("outer", new IllegalArgumentException("detalhe"), null);
		ResponseEntity<Map<String, String>> r = handler.notReadable(ex);
		assertThat(r.getBody().get("message")).isEqualTo("detalhe");
	}

	@Test
	void notReadable_fallsBackWhenNoCause() {
		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("só isto", null, null);
		ResponseEntity<Map<String, String>> r = handler.notReadable(ex);
		assertThat(r.getBody().get("message")).isEqualTo("só isto");
	}

	@Test
	void handlerMethodValidation_emptyResults() {
		HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
		when(ex.getParameterValidationResults()).thenReturn(List.of());
		when(ex.getCrossParameterValidationResults()).thenReturn(List.of());
		ResponseEntity<Map<String, String>> r = handler.handlerMethodValidation(ex);
		assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(r.getBody().get("message")).isEmpty();
	}
}
