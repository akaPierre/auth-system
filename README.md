# Auth System

Sistema completo de autenticação e autorização de usuários construído com **Java 21**, **Spring Boot 4.0.5** e **Spring Security 7**. Implementa boas práticas de segurança prontas para produção.

---

## Stack

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.5 | Framework base |
| Spring Security | 7.x | Autenticação e autorização |
| Spring Data JPA | 4.x | Persistência com Hibernate 7 |
| H2 Database | — | Banco em memória (desenvolvimento) |
| Thymeleaf | 4.x | Engine de templates |
| Lombok | — | Redução de boilerplate |
| Maven | 3.9+ | Build e gerenciamento de dependências |

---

## Funcionalidades

- ✅ Cadastro de usuário comum com validação de formulário
- ✅ Login com e-mail e senha
- ✅ Redirecionamento automático por role após login
- ✅ Página home exclusiva para usuários (`ROLE_USER`)
- ✅ Painel exclusivo para administradores (`ROLE_ADMIN`)
- ✅ Logout com invalidação de sessão e limpeza de cookie
- ✅ Admin padrão criado automaticamente na primeira execução

---

## Segurança Implementada

| Proteção | Mecanismo |
|---|---|
| Senhas | BCrypt com strength 12 (~400ms/hash) |
| CSRF | `HttpSessionCsrfTokenRepository` em todos os POSTs |
| SQL Injection | JPA com PreparedStatements parametrizados |
| XSS | Thymeleaf escaping + Content-Security-Policy header |
| Session Fixation | `changeSessionId()` ao autenticar |
| Sessões simultâneas | `maximumSessions(1)` por usuário |
| Cookie seguro | `HttpOnly=true`, `SameSite=Strict` |
| User enumeration | Mensagens de erro genéricas no login |
| Clickjacking | `X-Frame-Options: SAMEORIGIN` |
| MIME sniffing | `X-Content-Type-Options: nosniff` |

---

## Pré-requisitos

- [Java 21](https://adoptium.net/) instalado e configurado no `PATH`
- [Git](https://git-scm.com/) instalado

Verifique a instalação:

```powershell
java -version
# java version "21.x.x"
```

---

## Como Rodar Localmente

### 1. Clone o repositório

```powershell
git clone https://github.com/akaPierre/auth-system.git
cd auth-system
```

### 2. Execute a aplicação

```powershell
.\mvnw.cmd spring-boot:run
```

### 3. Acesse no browser

```
http://localhost:8080
```

> A aplicação cria automaticamente o banco H2 em `./data/authdb.mv.db` e o **admin padrão** na primeira execução.

---

## Credenciais Padrão

> ⚠️ **Altere imediatamente antes de ir para produção.**

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@sistema.com` | `Admin@1234` |
| Usuário comum | Cadastre via `/register` | — |

---

## Rotas Disponíveis

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/` | Público | Redireciona para `/login` |
| `GET` | `/login` | Público | Página de login |
| `POST` | `/login` | Público | Processa autenticação |
| `GET` | `/register` | Público | Formulário de cadastro |
| `POST` | `/register` | Público | Processa cadastro |
| `POST` | `/logout` | Autenticado | Encerra sessão |
| `GET` | `/home` | `ROLE_USER` | Home do usuário comum |
| `GET` | `/admin/home` | `ROLE_ADMIN` | Painel do administrador |
| `GET` | `/h2-console/**` | Público (dev) | Console do banco H2 |

---

## Console H2 (Desenvolvimento)

```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/authdb
Usuário:  sa
Senha:    (vazio)
```

> Desabilitar em produção: `spring.h2.console.enabled=false`

---

## Estrutura do Projeto

```
src/
└── main/
    ├── java/com/seuprojeto/authsystem/
    │   ├── config/
    │   │   ├── SecurityConfig.java       ← Configuração central do Spring Security
    │   │   └── DataInitializer.java      ← Seed do admin padrão
    │   ├── controller/
    │   │   ├── AuthController.java       ← Login e cadastro
    │   │   └── HomeController.java       ← Páginas home por role
    │   ├── dto/
    │   │   └── UserRegistrationDto.java  ← DTO do formulário de cadastro
    │   ├── model/
    │   │   ├── User.java                 ← Entidade JPA
    │   │   └── Role.java                 ← Enum de roles
    │   ├── repository/
    │   │   └── UserRepository.java       ← Acesso a dados
    │   └── service/
    │       ├── UserService.java          ← Interface de negócio
    │       ├── UserServiceImpl.java      ← Implementação com BCrypt
    │       └── CustomUserDetailsService  ← Bridge com Spring Security
    └── resources/
        ├── application.properties
        ├── static/css/style.css
        └── templates/
            ├── auth/
            │   ├── login.html
            │   └── register.html
            └── home/
                ├── user-home.html
                └── admin-home.html
```

---

## Executar Testes

```powershell
# Todos os testes
.\mvnw.cmd test

# Resultado esperado
# Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

### Cobertura dos Testes Automatizados

| Categoria | Testes | O que valida |
|---|---|---|
| Contexto | 1 | Aplicação sobe sem erros |
| Páginas públicas | 2 | `/login` e `/register` retornam 200 sem autenticação |
| Proteção de rotas | 2 | `/home` e `/admin/home` redirecionam sem autenticação |
| Login válido | 2 | Usuário e admin autenticam com credenciais corretas |
| Login inválido | 2 | Senha errada e e-mail inexistente retornam `unauthenticated` |
| Controle de acesso | 4 | Roles corretas acessam; roles erradas recebem 403 |
| Cadastro | 4 | Dados válidos, e-mail duplicado, senhas divergentes, campos vazios |
| Logout | 1 | Sessão destruída e redirecionamento correto |
| CSRF | 1 | POST sem token retorna 403 |
| Segurança de senha | 1 | Hash BCrypt `$2a$12$` no banco, nunca texto puro |

---

## Migração para Produção (PostgreSQL)

### 1. Adicione a dependência no `pom.xml`

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Substitua o bloco de banco no `application.properties`

```properties
spring.datasource.url=${DB_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.h2.console.enabled=false
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.servlet.session.cookie.secure=true
```

### 3. Configure as variáveis de ambiente no servidor

```powershell
$env:DB_URL      = "jdbc:postgresql://localhost:5432/authdb"
$env:DB_USER     = "postgres"
$env:DB_PASSWORD = "SuaSenhaSegura"
$env:ADMIN_EMAIL    = "admin@suaempresa.com"
$env:ADMIN_PASSWORD = "SenhaAdminForte#2026"
```

---

## Checklist de Otimizações Futuras

### 🔴 Alta Prioridade (Segurança)

- [ ] **HTTPS obrigatório** — Configurar `server.ssl.*` ou proxy reverso (Nginx/Caddy)
- [ ] **Rate limiting no login** — Adicionar `Bucket4j` para bloquear brute force (ex: máx. 5 tentativas/minuto por IP)
- [ ] **Troca de senha obrigatória no 1º login do admin** — Detectar credencial padrão e forçar troca
- [ ] **Variáveis de ambiente para credenciais do admin** — Substituir constantes em `DataInitializer` por `@Value`
- [ ] **Desabilitar H2 Console em produção** — `spring.h2.console.enabled=false` no profile de prod

### 🟡 Média Prioridade (Evolução)

- [ ] **Migrar para JWT** — Estrutura já preparada (`AuthenticationManager` exposto); substituir sessões por tokens stateless
- [ ] **Auditoria de login** — Criar tabela `login_events` (IP, timestamp, sucesso/falha) para detectar anomalias
- [ ] **Profiles Spring** (`application-dev.properties`, `application-prod.properties`) — Separar configurações por ambiente
- [ ] **Flyway ou Liquibase** — Versionamento de schema do banco em vez de `ddl-auto`
- [ ] **Paginação de usuários** no painel admin — `Pageable` no `UserRepository`
- [ ] **Perfil do usuário** — Endpoints para alterar nome e senha (com confirmação da senha atual)
- [ ] **Testes de cobertura** — Adicionar JaCoCo com meta mínima de 80% nas camadas `service` e `controller`

### 🟢 Baixa Prioridade (UX e Conveniência)

- [ ] **Remember Me** — `rememberMe()` no `SecurityConfig` com token persistido no banco
- [ ] **Recuperação de senha por e-mail** — Fluxo de reset com token temporário (Spring Mail)
- [ ] **OAuth2 / Login social** — Google ou GitHub via `spring-boot-starter-oauth2-client`
- [ ] **Internacionalização** — `messages.properties` para centralizar mensagens de validação e erro
- [ ] **Docker** — `Dockerfile` + `docker-compose.yml` com PostgreSQL para ambiente reproduzível
- [ ] **CI/CD** — GitHub Actions rodando `mvn test` em todo Pull Request

---

## Decisões de Design

### Por que H2 em arquivo e não in-memory?
H2 file (`jdbc:h2:file:`) persiste os dados entre reinicializações durante o desenvolvimento, simulando melhor o comportamento de produção. In-memory (`jdbc:h2:mem:`) perde os dados a cada restart.

### Por que BCrypt com strength 12?
Strength 12 gera ~400ms por operação de hash — aceitável para o usuário no login, mas exponencialmente mais custoso para ataques de força bruta comparado ao default 10.

### Por que `UserDetailsService` separado do `UserService`?
Responsabilidade única: `CustomUserDetailsService` cuida exclusivamente de autenticação. `UserServiceImpl` cuida de regras de negócio. Essa separação facilita a futura migração para JWT sem reescrever a lógica de negócio.

### Por que DTO e não a entidade `User` diretamente no formulário?
Usar a entidade `User` em formulários expõe o campo `role` a manipulação externa (mass assignment). O DTO define exatamente quais campos o usuário pode preencher.

### Por que logout via POST e não GET?
Um link `<a href="/logout">` é um GET — qualquer site externo pode forçar o logout do usuário. O POST com token CSRF garante que apenas o próprio sistema pode disparar o logout.

---

## Licença

MIT — veja o arquivo `LICENSE` para detalhes.