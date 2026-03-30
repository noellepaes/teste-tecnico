package com.noelle.teste_tecnico.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noelle.teste_tecnico.coupon.CouponStatus;
import com.noelle.teste_tecnico.coupon.repository.CouponRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void postCoupon_returns201AndSanitizedCode() throws Exception {
		mockMvc.perform(
						post("/coupon")
								.contentType(MediaType.APPLICATION_JSON)
								.content(
										"""
										{
										  "code": "ABC-123",
										  "description": "Cupom teste",
										  "discountValue": 0.8,
										  "expirationDate": "2026-12-31T18:00:00.000Z"
										}
										"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("ABC123"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.published").value(false))
				.andExpect(jsonPath("$.redeemed").value(false));
	}

	@Test
	void postCoupon_withPublishedTrue() throws Exception {
		mockMvc.perform(
						post("/coupon")
								.contentType(MediaType.APPLICATION_JSON)
								.content(
										"""
										{
										  "code": "PUB123",
										  "description": "x",
										  "discountValue": 0.5,
										  "expirationDate": "2026-12-31T18:00:00.000Z",
										  "published": true
										}
										"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.published").value(true));
	}

	@Test
	void postCoupon_rejectsDiscountBelowMinimum() throws Exception {
		String errorBody =
				mockMvc.perform(
								post("/coupon")
										.contentType(MediaType.APPLICATION_JSON)
										.content(
												"""
												{
												  "code": "ABCDEF",
												  "description": "x",
												  "discountValue": 0.49,
												  "expirationDate": "2026-12-31T18:00:00.000Z"
												}
												"""))
						.andExpect(status().isBadRequest())
						.andReturn()
						.getResponse()
						.getContentAsString();
		assertThat(errorBody).contains("mínimo");
	}

	@Test
	void postCoupon_rejectsCodeTooShortAfterNormalize() throws Exception {
		mockMvc.perform(
						post("/coupon")
								.contentType(MediaType.APPLICATION_JSON)
								.content(
										"""
										{
										  "code": "a@-b@-1",
										  "description": "x",
										  "discountValue": 0.5,
										  "expirationDate": "2026-12-31T18:00:00.000Z"
										}
										"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postCoupon_rejectsCodeTooLongAfterNormalize() throws Exception {
		mockMvc.perform(
						post("/coupon")
								.contentType(MediaType.APPLICATION_JSON)
								.content(
										"""
										{
										  "code": "ABC-123-XY",
										  "description": "x",
										  "discountValue": 0.5,
										  "expirationDate": "2026-12-31T18:00:00.000Z"
										}
										"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postCoupon_rejectsPastExpiration() throws Exception {
		String past = Instant.now().minusSeconds(120).toString();
		mockMvc.perform(
						post("/coupon")
								.contentType(MediaType.APPLICATION_JSON)
								.content(
										"""
										{
										  "code": "ABCDEF",
										  "description": "x",
										  "discountValue": 0.5,
										  "expirationDate": "%s"
										}
										"""
												.formatted(past)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deleteCoupon_softDelete_andSecondDeleteConflict() throws Exception {
		String body =
				mockMvc.perform(
								post("/coupon")
										.contentType(MediaType.APPLICATION_JSON)
										.content(
												"""
												{
												  "code": "DEL001",
												  "description": "del",
												  "discountValue": 0.5,
												  "expirationDate": "2026-12-31T18:00:00.000Z"
												}
												"""))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();
		JsonNode root = objectMapper.readTree(body);
		UUID uuid = UUID.fromString(root.get("id").asText());

		mockMvc.perform(delete("/coupon/{id}", uuid)).andExpect(status().isNoContent());

		assertThat(couponRepository.findById(uuid)).isPresent();
		assertThat(couponRepository.findById(uuid).orElseThrow().getStatus())
				.isEqualTo(CouponStatus.DELETED);

		mockMvc.perform(delete("/coupon/{id}", uuid)).andExpect(status().isConflict());
	}

	@Test
	void deleteUnknown_returns404() throws Exception {
		mockMvc.perform(delete("/coupon/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
	}
}
