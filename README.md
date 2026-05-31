# FindTeam

FindTeam is a Telegram Mini App for finding teammates for hackathons, startups, pet projects, internships, and IT collaborations.

## Backend Database Setup

The Spring Boot backend supports two database modes:

- Default mode: H2 in-memory database for quick local development and tests.
- `postgres` profile: PostgreSQL for local database compatibility testing.

### Run Backend With H2

H2 is used when no Spring profile is selected.

```bash
./mvnw spring-boot:run
```

This starts the backend on port `8080` and uses an in-memory H2 database:

```text
jdbc:h2:mem:findteam
```

Data is reset when the process stops.

### Run Local PostgreSQL

Start only the PostgreSQL container:

```bash
docker compose up -d postgres
```

Default local credentials:

```text
Database: findteam
Username: findteam
Password: findteam
Host: localhost
Port: 5432
```

Stop PostgreSQL:

```bash
docker compose down
```

Stop PostgreSQL and remove stored database data:

```bash
docker compose down -v
```

### Run Backend With PostgreSQL

Use the `postgres` Spring profile:

```bash
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

The profile reads these environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
TELEGRAM_BOT_TOKEN
```

Example:

```bash
DB_URL=jdbc:postgresql://localhost:5432/findteam \
DB_USERNAME=findteam \
DB_PASSWORD=findteam \
TELEGRAM_BOT_TOKEN=123456:telegram-bot-token \
SPRING_PROFILES_ACTIVE=postgres \
./mvnw spring-boot:run
```

If database variables are not set, the backend uses local defaults:

```text
DB_URL=jdbc:postgresql://localhost:5432/findteam
DB_USERNAME=findteam
DB_PASSWORD=findteam
```

### Schema Management

For local development, Hibernate uses:

```text
spring.jpa.hibernate.ddl-auto=update
```

This is convenient for MVP development. Production should use explicit migrations later, for example Flyway or Liquibase.

### Tests

Tests continue to use the default H2 setup:

```bash
./mvnw test
```

