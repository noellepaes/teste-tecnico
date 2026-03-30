package com.noelle.teste_tecnico.web;

import com.noelle.teste_tecnico.coupon.dto.CouponCreateRequest;
import com.noelle.teste_tecnico.coupon.dto.CouponResponse;
import com.noelle.teste_tecnico.coupon.service.CouponService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/coupon")
@Tag(name = "Coupon", description = "Cadastro e exclusão lógica de cupons")
public class CouponController {

	private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

	private final CouponService couponService;

	public CouponController(CouponService couponService) {
		this.couponService = couponService;
	}

	@PostMapping(consumes = JSON, produces = JSON)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Criar cupom",
			description =
					"""
					**Resposta real:** use o bloco *Server response* após Executar — ele mostra o status e o corpo desta chamada.

					**Lista Responses:** descreve todos os cenários possíveis; não é o resultado da última execução.
					Com **400** nenhum cupom é gravado no banco. Com **201** o cupom foi persistido.
					""",
			responses = {
				@ApiResponse(
						responseCode = "400",
						description =
								"Erro de validação, JSON inválido ou regra de negócio — **nenhum cupom é criado**. Corpo: `{\"message\":\"...\"}`",
						content =
								@Content(
										mediaType = JSON,
										schema = @Schema(type = "object", description = "Objeto com uma propriedade message (string)"),
										examples = {
											@ExampleObject(
													name = "regraNegocio",
													summary = "Data no passado",
													value = "{\"message\":\"A data de expiração não pode ser no passado.\"}"),
											@ExampleObject(
													name = "codigoTamanho",
													summary = "Código com tamanho inválido após limpar especiais",
													value =
															"{\"message\":\"Após remover caracteres especiais, o código deve ter exatamente 6 caracteres alfanuméricos.\"}"),
											@ExampleObject(
													name = "validacao",
													summary = "Bean Validation",
													value = "{\"message\":\"code: não deve estar em branco\"}")
										})),
				@ApiResponse(
						responseCode = "201",
						description = "**Somente em sucesso** — cupom persistido e retornado no corpo",
						content =
								@Content(
										mediaType = JSON,
										schema = @Schema(implementation = CouponResponse.class))),
			})
	public CouponResponse create(@Valid @RequestBody CouponCreateRequest request) {
		return couponService.create(request);
	}

	@DeleteMapping(value = "/{id}", produces = JSON)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
			summary = "Excluir cupom (soft delete)",
			responses = {
				@ApiResponse(responseCode = "204", description = "Excluído com sucesso (sem corpo)"),
				@ApiResponse(
						responseCode = "404",
						description = "Cupom não encontrado",
						content =
								@Content(
										mediaType = JSON,
										schema = @Schema(type = "object"),
										examples =
												@ExampleObject(value = "{\"message\":\"Cupom não encontrado: …\"}"))),
				@ApiResponse(
						responseCode = "409",
						description = "Cupom já estava excluído",
						content =
								@Content(
										mediaType = JSON,
										schema = @Schema(type = "object"),
										examples =
												@ExampleObject(value = "{\"message\":\"O cupom já foi excluído.\"}"))),
			})
	public void delete(@PathVariable UUID id) {
		couponService.delete(id);
	}
}
