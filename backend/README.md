# DSCatalog Backend

API REST para gerenciamento de catalogo de produtos, categorias e usuarios, desenvolvida com Spring Boot.

Este projeto faz parte da trilha Java Spring Expert (DevSuperior) e foi evoluido para servir como base de estudo e referencia para projetos futuros.

## Visao Geral

A aplicacao oferece:

- CRUD de produtos, categorias e usuarios
- Controle de acesso por papeis (`ROLE_ADMIN` e `ROLE_OPERATOR`)
- Autenticacao com OAuth2 Authorization Server e JWT
- Grant customizado `password`
- Recuperacao de senha com token temporario e envio por e-mail
- Ambientes `test`, `dev` e `prod` com configuracoes separadas

## Tecnologias

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Authorization Server
- Spring OAuth2 Resource Server
- Spring Validation
- Spring Mail
- PostgreSQL (dev/prod)
- H2 (test)
- Maven Wrapper
- Docker Compose (PostgreSQL + pgAdmin)

## Arquitetura (camadas)

- `resources`: endpoints REST
- `services`: regras de negocio
- `repositories`: acesso a dados com Spring Data JPA
- `entities`: mapeamento das tabelas
- `dto`: objetos de transferencia de dados
- `config`: seguranca, OAuth2 e configuracoes gerais

## Seguranca e Autenticacao

A aplicacao roda com dois papeis principais:

- `ROLE_ADMIN`
- `ROLE_OPERATOR`

O token JWT inclui claims customizadas:

- `authorities`
- `username`

### Como obter token de acesso

Endpoint padrao do Authorization Server:

- `POST /oauth2/token`

Use autenticacao de cliente (Basic Auth) com:

- `client_id`: valor de `security.client-id`
- `client_secret`: valor de `security.client-secret`

Exemplo com curl:

```bash
curl --request POST 'http://localhost:8080/oauth2/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --header 'Authorization: Basic bXljbGllbnRpZDpteWNsaWVudHNlY3JldA==' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=alex@gmail.com' \
  --data-urlencode 'password=123456'
```

Obs.: o valor do header `Authorization: Basic ...` corresponde a `base64(client_id:client_secret)`.

### Usar token nas rotas protegidas

```bash
curl --request GET 'http://localhost:8080/users/profile' \
  --header 'Authorization: Bearer SEU_ACCESS_TOKEN'
```

## Recuperacao de Senha

Fluxo:

1. Cliente envia e-mail em `POST /auth/recover-token`
2. API gera token com expiracao (minutos configuraveis)
3. API envia e-mail com link/token
4. Cliente envia nova senha em `PUT /auth/new-password`

## Endpoints Principais

### Auth

- `POST /auth/recover-token`
- `PUT /auth/new-password`

### Users

- `GET /users` (ADMIN)
- `GET /users/{id}` (ADMIN)
- `GET /users/profile` (ADMIN, OPERATOR)
- `POST /users` (publico)
- `PUT /users/{id}` (ADMIN)
- `DELETE /users/{id}` (ADMIN)

### Categories

- `GET /categories` (publico)
- `GET /categories/{id}` (publico)
- `POST /categories` (ADMIN, OPERATOR)
- `PUT /categories/{id}` (ADMIN, OPERATOR)
- `DELETE /categories/{id}` (ADMIN, OPERATOR)

### Products

- `GET /products?name=&categoryId=&page=0&size=12` (publico)
- `GET /products/{id}` (publico)
- `POST /products` (ADMIN, OPERATOR)
- `PUT /products/{id}` (ADMIN, OPERATOR)
- `DELETE /products/{id}` (ADMIN, OPERATOR)

## Colecao Postman

A pasta `postman` contem os arquivos para facilitar os testes manuais da API:

- `postman/dscatalog.postman_collection.json`: colecao com endpoints principais
- `postman/dscatalog-env.postman_environment.json`: variaveis de ambiente (base URL, token e credenciais)

### Como usar

1. Importe a collection `postman/dscatalog.postman_collection.json` no Postman.
2. Importe o environment `postman/dscatalog-env.postman_environment.json`.
3. Selecione o environment importado.
4. Ajuste as variaveis conforme o perfil em execucao (`test` ou `dev`), se necessario.
5. Execute primeiro a requisicao de token para preencher o `access_token` e depois teste as rotas protegidas.

## Configuracao por Ambiente

Arquivo principal: `src/main/resources/application.yaml`

### test (padrao)

- Banco H2 em memoria
- Console H2 habilitado em `/h2-console`
- `ddl-auto: create-drop`
- Carga inicial via `data.sql`

### dev

- PostgreSQL local
- `ddl-auto: update`
- SQL init configuravel por variavel (`SQL_INIT_MODE`)

### prod

- `DATABASE_URL` via variavel de ambiente
- `ddl-auto: none`
- logs SQL desabilitados

## Variaveis de Ambiente

### Gerais

- `PORT` (default: `8080`)
- `APP_PROFILE` (default: `test`)
- `CORS_ORIGINS` (default: `http://localhost:3000,http://localhost:5173`)

### OAuth2/JWT

- `CLIENT_ID` (default: `myclientid`)
- `CLIENT_SECRET` (default: `myclientsecret`)
- `JWT_DURATION` em segundos (default: `86400`)

### Banco (dev)

- `JPA_DDL_AUTO` (default: `update`)
- `SQL_INIT_MODE` (default: `never`)

### E-mail

- `APP_HOST`
- `APP_EMAIL`
- `APP_PASSWORD`
- `PASSWORD_RECOVER_TOKEN_MINUTES` (default: `30`)
- `PASSWORD_RECOVER_URI` (default: `http://localhost:5173/recover-password/`)

## Como Executar

### 1) Subir infra de banco (opcional para perfil dev)

```bash
docker compose up -d
```

Servicos disponiveis:

- PostgreSQL: `localhost:5433`
- pgAdmin: `http://localhost:5050`

### 2) Rodar API

Perfil `test` (padrao):

```bash
./mvnw spring-boot:run
```

Perfil `dev`:

```bash
APP_PROFILE=dev ./mvnw spring-boot:run
```

### 3) Build

```bash
./mvnw clean package
```

### 4) Testes

```bash
./mvnw test
```

## Dados Iniciais

No perfil `test`, o arquivo `data.sql` popula:

- usuarios
- papeis
- categorias
- produtos
- relacoes de permissao e categorias de produtos

## Observacoes Importantes

- O projeto exige Java 21.
- Nao commitar arquivos de ambiente local com segredo.
- O arquivo `.env-intellij` esta configurado para nao ir ao repositorio remoto.

## Melhorias Futuras

- Documentacao OpenAPI/Swagger
- Observabilidade com metricas customizadas
- Pipeline CI/CD
- Testes de integracao para fluxo OAuth2 e recuperacao de senha

## Licenca

Uso educacional e de portfolio.
