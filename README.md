# 🚀 Microservice Example ( No Auth )

A complete **event-driven microservices architecture** built using **Spring Boot, Kafka, PostgreSQL, Eureka, API Gateway, and monitoring tools (Prometheus + Grafana)**.

This project demonstrates how to design, run, and monitor scalable microservices using Docker.

---

## HL Architecture Diagram
<img width="1448" height="872" alt="Untitled Diagram-Page-1" src="https://github.com/user-attachments/assets/67b1ed4a-4fa2-4d49-b1e4-7db0bee24ef7" />


## 🏗️ Architecture Overview

This system consists of:

* **Service Registry** – Eureka server for service discovery
* **API Gateway** – Entry point for all client requests
* **Order Service** – Handles order creation and publishing events
* **Payment Service** – Consumes order events and processes payments
* **Kafka** – Event streaming between services
* **PostgreSQL** – Separate databases per service
* **Kafka UI** – Web interface to monitor Kafka
* **Prometheus** – Metrics collection
* **Grafana** – Visualization dashboards

---

## 📦 Tech Stack

* Java 21 + Spring Boot
* Spring Cloud (Eureka, Gateway)
* Apache Kafka (KRaft mode)
* PostgreSQL
* Docker & Docker Compose
* Prometheus + Grafana

## 🚀 Getting Started

### Start all services

```bash
docker compose up -d --build
```

## 🔄 Event Flow

1. Client sends request → **API Gateway**
2. Routed to **Order Service**
3. Order Service → publishes event to **Kafka**
4. **Payment Service** consumes event
5. Payment is processed and stored

---

## 🗄️ Databases

Each service has its own isolated database:

* **Order DB**

  * DB: `order_db`
  * User: `order_user`

* **Payment DB**

  * DB: `payment_db`
  * User: `payment_user`

---

## 📊 Monitoring

* **Prometheus** collects metrics from services
* **Grafana** provides dashboards for visualization

---

## 💡 Key Features

* Event-driven communication (Kafka)
* Service discovery (Eureka)
* API Gateway routing
* Database per service pattern
* Observability (metrics + dashboards)
* Fully containerized setup
* SAGA pattern

---

## 📌 Future Improvements

* Add authentication (JWT / OAuth2)
* Circuit breaker (Resilience4j)
* Distributed tracing (Zipkin / Jaeger)
* CI/CD pipeline integration
* Kubernetes deployment
