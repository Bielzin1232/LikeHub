# LikeHub

Backend de uma rede social para publicação de posts, interação por likes, comentários, respostas, notificações e feed personalizado.

O projeto é uma aplicação monolítica modular construída com Spring Boot, PostgreSQL, Redis, Kafka e Flyway. O Redis é utilizado para dados de acesso rápido, como contadores de likes, preferências do feed e cache paginado de comentários.

## Stack

- Java 25
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Redis 7
- Apache Kafka 4
- Flyway
- Spring Security com JWT
- Spring Modulith
- Maven Wrapper
- Docker Compose

## Requisitos

- JDK 25
- Docker e Docker Compose
- Git

Confira as versões instaladas:

```bash
java -version
docker --version
docker compose version
```

## Executando localmente

### 1. Subir os serviços

```bash
docker compose up -d postgres redis kafka
```

Verifique o estado dos containers:

```bash
docker compose ps
```

Os serviços ficam disponíveis em:

| Serviço | Endereço local |
|---|---|
| API | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |
| Kafka | `localhost:29092` |

### 2. Iniciar a aplicação

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Ao iniciar, o Flyway executa as migrations em `src/main/resources/db/migration`. O Hibernate usa `ddl-auto=validate`, portanto o banco precisa estar com o schema compatível com as entidades.

### 3. Verificar a aplicação

```bash
curl http://localhost:8080/actuator/health
```

As rotas da aplicação exigem autenticação, mas o actuator expõe o health check configurado para diagnóstico.

## Banco de dados e migrations

As migrations devem seguir o padrão do Flyway:

```text
V1__migration.sql
V2__add_session_version.sql
V3__complete_domain_schema.sql
```

Para recriar o banco local de desenvolvimento, removendo os dados persistidos:

```bash
docker compose down -v
docker compose up -d postgres redis kafka
```

> O comando `down -v` apaga os volumes do PostgreSQL, Redis e Kafka. Não use em ambientes que contenham dados importantes.

Se uma instalação antiga já tiver um histórico Flyway incompatível, faça backup antes de qualquer reparo. Em desenvolvimento, a opção mais simples é recriar o volume; em produção, use `flyway repair` somente após conferir a migration aplicada e o checksum registrado.

### Erro de checksum do Flyway

Se a aplicação falhar com mensagens como:

```text
Migration checksum mismatch for migration version 2
Detected resolved migration not applied to database: 1
```

o banco existente foi criado com uma versão anterior das migrations. Para um banco local sem dados importantes, recrie os volumes:

```bash
docker compose down -v
docker compose up -d postgres redis kafka
./mvnw spring-boot:run
```

Se os dados precisarem ser preservados, não use `down -v` nem altere a tabela `flyway_schema_history` manualmente. Faça backup do PostgreSQL, compare as migrations aplicadas e execute `flyway repair` somente após confirmar que o schema atual corresponde aos arquivos versionados.

### Porta 8080 ocupada

Se o erro for `Port 8080 was already in use`, encerre a instância anterior ou use outra porta:

```bash
SERVER_PORT=8081 ./mvnw spring-boot:run
```

## Redis e cache

A configuração padrão usa:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=10m
spring.data.redis.timeout=2s
```

O `RedisConfig` configura `RedisTemplate<String, String>` com serializadores de string para evitar serialização implícita de entidades JPA.

### Cache de comentários

Os comentários são armazenados usando cache-aside:

```text
comments:post:{postId}:page:{page}:size:{size}
```

O fluxo é:

1. consultar o Redis;
2. retornar o valor se houver cache;
3. consultar o PostgreSQL em caso de miss;
4. salvar o DTO no Redis com TTL;
5. invalidar as páginas do post após criar, editar, responder ou excluir comentário.

As entidades `Comment` não são colocadas diretamente no Redis. O cache usa DTOs para evitar problemas com relacionamentos JPA lazy e referências circulares.

### Chaves principais

| Chave | Uso |
|---|---|
| `comments:post:{postId}:page:{page}:size:{size}` | Comentários paginados |
| `likes:count:{postId}` | Contador de likes |
| `likes:users:{postId}` | Usuários que curtiram |
| `user:{userId}:preferences` | Preferências usadas pelo feed |

## Variáveis de ambiente

Os valores abaixo são os defaults para desenvolvimento local:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/likehub
SPRING_DATASOURCE_USERNAME=likehub
SPRING_DATASOURCE_PASSWORD=likehub
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:29092
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_FLYWAY_ENABLED=true
SPRING_CACHE_REDIS_TTL=10m
SPRING_DATA_REDIS_TIMEOUT=2s
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000
JWT_SECRET=defina-um-segredo-forte-fora-do-repositorio
JWT_EXPIRATION_MILLIS=86400000
```

Para iniciar com valores diferentes:

```bash
SPRING_DATA_REDIS_HOST=redis \
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/likehub \
./mvnw spring-boot:run
```

Nunca versionar secrets reais. Em produção, forneça `JWT_SECRET` e credenciais do banco pelo ambiente de execução ou por um gerenciador de secrets.

## Autenticação

O fluxo usa JWT e refresh token:

1. registrar usuário;
2. fazer login;
3. enviar o access token no header:

```http
Authorization: Bearer <access-token>
```

4. renovar o token usando o refresh token quando necessário.

No login, o campo `identifier` deve conter o email. O `username` é um nome
público e pode ser repetido, portanto não serve como identificador exclusivo
de uma conta.

As rotas de autenticação são públicas. As leituras (`GET`) de posts e
comentários também são públicas; as operações de criação, edição e exclusão
exigem usuário autenticado.

## Endpoints disponíveis

A API usa o prefixo `/api/v1`.

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Criar usuário |
| `POST` | `/api/v1/auth/login` | Fazer login |
| `POST` | `/api/v1/auth/refresh` | Renovar tokens |
| `POST` | `/api/v1/auth/logout` | Encerrar sessão atual |
| `POST` | `/api/v1/auth/logout-all` | Encerrar todas as sessões |
| `POST` | `/api/v1/auth/password/change` | Alterar senha |

### Posts

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/posts` | Criar post |
| `GET` | `/api/v1/posts/{postId}` | Buscar post |
| `PUT` | `/api/v1/posts/{postId}` | Atualizar post |
| `DELETE` | `/api/v1/posts/{postId}` | Excluir post |

### Feed

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/feed?page=0` | Buscar feed personalizado |

### Comentários

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/posts/{postId}/comments?page=0&size=20` | Listar comentários raiz |
| `GET` | `/api/v1/comments/{commentId}/replies` | Listar respostas |
| `POST` | `/api/v1/comments/create` | Criar comentário |
| `PUT` | `/api/v1/posts/{postId}/comments/{commentId}/update` | Editar comentário |
| `PUT` | `/api/v1/posts/{postId}/comments/{commentId}/reply` | Responder comentário |
| `DELETE` | `/api/v1/posts/{postId}/comments/{commentId}` | Excluir comentário |

O tamanho da página de comentários deve estar entre 1 e 100.

### Likes

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/likes/toggle?targetId={id}&targetType=POST` | Curtir ou remover like |
| `GET` | `/api/v1/likes/count/{targetId}` | Consultar quantidade de likes |
| `GET` | `/api/v1/likes/{targetId}/users?page=0` | Listar usuários que curtiram |

### Notificações

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/notifications?page=0` | Listar notificações |
| `GET` | `/api/v1/notifications/unread-count` | Consultar não lidas |
| `POST` | `/api/v1/notifications/read-all` | Marcar todas como lidas |
| `PATCH` | `/api/v1/notifications/{notificationId}/read` | Marcar uma como lida |

### Imagens

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/images/upload` | Fazer upload multipart |

## Exemplos rápidos

### Criar usuário

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "user@example.com",
    "username": "Nome Sobrenome",
    "displayName": "Nome de exibição",
    "password": "senha-forte"
  }'
```

### Consultar comentários

```bash
curl "http://localhost:8080/api/v1/posts/{postId}/comments?page=0&size=20" \
  -H "Authorization: Bearer {access-token}"
```

### Verificar o Redis

```bash
docker compose exec redis redis-cli ping
docker compose exec redis redis-cli keys 'comments:post:*'
```

## Testes e validação

Compilar:

```bash
./mvnw clean compile
```

Executar os testes:

```bash
./mvnw test
```

Validar o Docker Compose:

```bash
docker compose config --quiet
```

## Estrutura do projeto

```text
src/main/java/com/likehub/likehub/
├── auth/            # login, registro, JWT e refresh tokens
├── comments/        # comentários, respostas e cache Redis
├── feed/            # feed personalizado
├── images/          # upload e imagens
├── likes/           # likes e contadores
├── notifications/   # notificações
├── posts/           # posts
├── users/           # usuários e preferências
├── api/             # constantes de rotas
└── shared/          # segurança, configurações e exceções

src/main/resources/
├── application.properties
└── db/migration/    # migrations do Flyway
```

## Endpoints planejados

As constantes de rota já sugerem algumas funcionalidades que ainda não possuem implementação completa no controller/service:

- perfil e busca de usuários;
- seguir e deixar de seguir usuários;
- listagem de posts por usuário;
- bookmarks/salvos;
- gerenciamento completo de respostas e usuários.

Esses endpoints devem ser implementados junto com seus DTOs, regras de autorização e testes antes de serem considerados parte da API estável.

## Licença

Este projeto ainda não declara uma licença de distribuição no `pom.xml`.
