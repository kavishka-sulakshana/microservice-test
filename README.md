# Microservice Example

A complete event-driven microservices architecture built using Spring Boot, Kafka, PostgreSQL, Eureka, API Gateway, Keycloak, Prometheus, and Grafana.

## Architecture Overview

This system consists of:

* **Service Registry** - Eureka server for service discovery
* **API Gateway** - Entry point for client requests
* **Frontend** - React app that demonstrates user login and protected API calls
* **Order Service** - Handles order creation and publishes order events
* **Payment Service** - Consumes order events and processes payments
* **Kafka** - Event streaming between services
* **PostgreSQL** - Separate databases per service
* **Keycloak** - OIDC/OAuth2 provider for user login and service-to-service auth
* **Kafka UI** - Web interface to monitor Kafka
* **Prometheus** - Metrics collection
* **Grafana** - Visualization dashboards

## Tech Stack

* Java 21 + Spring Boot
* Spring Cloud Gateway + Eureka
* Spring Security OAuth2 Resource Server
* React + Vite
* Keycloak
* Apache Kafka
* PostgreSQL
* Docker & Docker Compose
* Prometheus + Grafana

## Getting Started

Start all services:

```bash
docker compose up -d --build
```

Open the React demo:

```text
http://localhost:5173
```

## Authentication

All `/api/**` routes are protected with Keycloak-issued JWT access tokens. The gateway validates the bearer token before routing, and the order/payment services validate the same bearer token again so direct service access is not implicitly trusted.

Local Keycloak:

* Admin console: `http://localhost:8091`
* Admin user: `admin`
* Admin password: `admin`
* Realm: `microservice`
* Public login client: `user-login` with Authorization Code + PKCE
* Service client: `service-client`
* Local service secret: `local-service-secret-change-me`

Service-to-service token example:

```bash
curl -X POST http://localhost:8091/realms/microservice/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=service-client" \
  -d "client_secret=local-service-secret-change-me"
```

Use the returned `access_token` when calling APIs:

```bash
curl http://localhost:8080/api/orders/{orderId} \
  -H "Authorization: Bearer <access_token>"
```

For user login, use Authorization Code + PKCE against `http://localhost:8091/realms/microservice` with client `user-login`. A local development user is imported as `demo` / `demo`.

The React frontend uses this flow and calls the gateway with the returned bearer token. It can create an order, fetch an order, and fetch the payment record for an order ID.

If Keycloak returns `Invalid parameter: redirect_uri`, update the `user-login` client in the Keycloak admin console so Valid redirect URIs include `http://localhost:5173/*` and Web origins include `http://localhost:5173`. Realm imports only apply when the realm is first created, so an existing `keycloak_db_data` volume will not automatically pick up later JSON changes.

## Event Flow

1. Client sends request to the API Gateway with a bearer token.
2. Gateway validates the token and routes to the Order Service.
3. Order Service validates the token, creates the order, and publishes an event to Kafka.
4. Payment Service consumes the order event.
5. Payment is processed and stored.

## Databases

Each service has its own isolated database:

* **Order DB**
  * DB: `order_db`
  * User: `order_user`
* **Payment DB**
  * DB: `payment_db`
  * User: `payment_user`
* **Keycloak DB**
  * DB: `keycloak`
  * User: `keycloak`

## Monitoring

* Prometheus collects metrics from services.
* Grafana provides dashboards and visualization.

## Key Features

* OAuth2/OIDC user login with Keycloak
* Service-to-service auth using client credentials
* JWT validation at gateway and service boundaries
* Event-driven communication with Kafka
* Service discovery with Eureka
* API Gateway routing
* Database per service pattern
* Observability with metrics and dashboards
* Fully containerized setup
* SAGA-style event flow

## Production Notes

The local compose file uses development credentials and `start-dev` for Keycloak. For production, rotate client secrets, remove demo users, enforce HTTPS, use externalized secrets, restrict actuator endpoints, and run Keycloak with production hostname and TLS settings.
