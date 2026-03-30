# teste-tecnico

API REST em **Spring Boot 4** para cadastro de cupons com **H2** (em memória), **JPA**, validação de entrada e documentação **OpenAPI (Swagger)**.

## O que foi implementado

_Use este bloco para descrever decisões técnicas, o que entregaste no teste e qualquer nota para quem for revisar o repositório._

**Resumo técnico (preenchido):**

- API REST com `POST /coupon` e `DELETE /coupon/{id}`.
- Persistência com **JPA** e **H2 em memória**; consola H2 ativa em desenvolvimento.
- **DTOs** de entrada/saída (`CouponCreateRequest`, `CouponResponse`) com validação Jakarta Validation e documentação **Swagger / OpenAPI 3**.
- Regras de negócio da criação e normalização do código no **`CouponService`**; exclusão lógica na mesma camada, atualizando o estado na entidade.
- Tratamento de erros centralizado em **`GlobalExceptionHandler`** (400 regra de negócio / validação, 404 não encontrado, 409 já excluído).
- **Testes:** unitários do serviço (Mockito), testes de integração HTTP (`CouponControllerIntegrationTest`), testes do handler de exceções e smoke da aplicação.
- **JaCoCo:** relatório em `target/site/jacoco`; na fase `verify`, *check* de **≥ 80% de linhas** no pacote `com.noelle.teste_tecnico.coupon.service` (regras de negócio da aplicação).
- **Docker** multi-stage (`Dockerfile`) e **`docker compose`** para subir a aplicação na porta 8080.

---

## Regras de negócio

### Create

- Um cupom pode ser cadastrado a qualquer momento. Campos obrigatórios:
  - `code`
  - `description`
  - `discountValue`
  - `expirationDate`
- O **código** é alfanumérico com **exatamente 6 caracteres** após o processamento:
  - Na criação podem existir **caracteres especiais**; a aplicação **remove** tudo o que não for letra ou dígito e exige que restem **6** caracteres (nem mais, nem menos). O valor guardado e devolvido na resposta é esse código normalizado.
- O **desconto** tem valor mínimo **0,5**; não há máximo definido na regra.
- A **data de expiração** não pode estar no **passado** (relativamente ao relógio da aplicação).
- O cupom pode ser criado já como **publicado** (`published: true`).

### Delete

- Um cupom pode ser eliminado a qualquer momento (via API).
- É feito **soft delete**: o registo permanece na base; altera-se o estado para refletir a exclusão lógica, sem apagar os dados do cadastro.
- **Não** é permitido eliminar de novo um cupom **já** eliminado (resposta de conflito).

---

## Cobertura de testes — regras de negócio (JaCoCo)

A meta explícita de **80%** no `pom.xml` aplica-se às **linhas** do pacote **`com.noelle.teste_tecnico.coupon.service`**, onde estão implementadas a normalização do código, validações de desconto e expiração e a orquestração da criação.



<img width="1182" height="354" alt="Image" src="https://github.com/user-attachments/assets/579f0b1f-1207-4ade-830a-d48225519e9c" />


## Expectativas (nível Pleno) — checklist

| Expectativa | Situação neste projeto |
|-------------|-------------------------|
| Testes cobrindo regras de negócio (**80%**) | **Sim:** JaCoCo *check* com mínimo de **80% de linhas** em `com.noelle.teste_tecnico.coupon.service`; testes diretos no serviço + fluxos HTTP que exercitam as mesmas regras. |
| Banco em memória **H2** | **Sim** (`application.properties` + dependência `h2`). |
| Regras em **objetos de domínio** | **Nesta versão as regras estão no `CouponService`**, não em records/classes de domínio separados. Se precisares de alinhar estritamente ao enunciado, podes extrair de novo um módulo de domínio (ex.: `CouponCode` + `Coupon`) e manter o serviço fino. |
| **Docker** e **Docker Compose** | **Sim** (`Dockerfile` + `docker-compose.yml`). |
| **Swagger** | **Sim** (SpringDoc — UI em `/swagger-ui.html`, OpenAPI em `/v3/api-docs`). |

---

## Requisitos

- Java **17**
- Maven (ou use o wrapper: `./mvnw` no Linux/macOS, `mvnw.cmd` no Windows)

## Como rodar localmente

Na pasta do projeto (onde está o `pom.xml`):

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Porta padrão: **8080**.

### Docker

```bash
docker compose up --build
```

## Endpoints da API

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/coupon` | Cria cupom (corpo JSON) — resposta **201** |
| `DELETE` | `/coupon/{id}` | Exclusão lógica (soft delete) — **204** |

Base URL local: `http://localhost:8080`

## URLs úteis (app em execução)

| O quê | URL |
|-------|-----|
| **Swagger UI** (testar a API) | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |
| **Consola H2** | http://localhost:8080/h2-console |

**Consola H2:** em *JDBC URL* use `jdbc:h2:mem:coupons;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`, utilizador `sa`, palavra-passe vazia. A base é em memória e partilhada com o processo da aplicação.

## Testes

```bash
./mvnw test
```

O Maven Surefire inclui por defeito classes `*Test` e `*Tests`. Ficheiros só com sufixo `*IT` não são executados sem configuração extra no `pom.xml` — os testes HTTP estão em `CouponControllerIntegrationTest`.

## Cobertura de testes (JaCoCo) — comandos

Relatório HTML + *check* no `verify` (mínimo 80% de linhas em `coupon.service`):

```bash
./mvnw clean verify
```

Só relatório HTML:

```bash
./mvnw clean test jacoco:report
```

Abrir: **`target/site/jacoco/index.html`** (a partir da pasta do módulo Maven).

**Windows:** na pasta correta,

```powershell
start .\target\site\jacoco\index.html
```

## Estrutura resumida

- `coupon` — entidade JPA, repositório, serviço, DTOs, mapper
- `web` — `CouponController`
- `exceptions` — `GlobalExceptionHandler` e exceções de negócio
- `docs` — imagens de documentação (ex.: captura JaCoCo)
