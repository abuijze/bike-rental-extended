# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Java/Maven Commands
- **Build entire project**: `mvn package` or `./mvnw package`
- **Run tests**: `mvn test` or `./mvnw test`
- **Native compilation**: `mvn package -Pnative` or `./mvnw package -Pnative`
- **Run individual Spring Boot applications**:
  - Rental service: Run `RentalApplication` class in `rental/src/main/java/io/axoniq/demo/bikerental/rental/RentalApplication.java`
  - Payment service: Run `PaymentApplication` class in `payment/src/main/java/io/axoniq/demo/bikerental/payment/PaymentApplication.java`

### Frontend Commands
- **Install dependencies**: `npm install` (in frontend/ directory)
- **Development server**: `npm run dev` (starts Nuxt dev server on port 3000)
- **Build for production**: `npm run build`
- **Generate static site**: `npm run generate`
- **Preview production build**: `npm run preview`

### Microservices Setup
- **Create microservices from monolith**: `./create-microservices.sh` (copies components to separate microservice modules)

## High-Level Architecture

This is an Axon Framework-based event-driven microservices demo implementing a bike rental system using CQRS and Event Sourcing patterns.

### Core Architecture Components

**Event-Driven Design**: Built on Axon Framework 4.11.0 with Spring Boot 3.3.4, using event sourcing and CQRS patterns throughout.

**Bounded Contexts**:
- **Rental Context**: Manages bike lifecycle, reservations, and returns
- **Payment Context**: Handles payment processing and confirmation
- **Core API**: Shared command/event definitions in Kotlin

### Key Patterns and Concepts

**Aggregates**: 
- `Bike` aggregate (rental/command/Bike.java) manages bike state with event sourcing
- `Payment` aggregate handles payment lifecycle

**Sagas**: 
- `PaymentSaga` orchestrates the rental-payment workflow with deadline management and retry logic

**Event Processing**: 
- Pooled streaming event processors for scalability
- Separate processors for PaymentSaga and query projections

**Architecture Flexibility**:
- Monolith structure in `rental/` and `payment/` modules
- Microservices structure in `microservices/` directory
- Use `create-microservices.sh` to extract components into separate deployable services

### Dependencies and External Services

**Required Infrastructure**:
- **Axon Server**: Event store and message routing (port 8024/8124)
  - Docker: `docker run -d --name axonserver -p 8024:8024 -p 8124:8124 -e AXONIQ_AXONSERVER_DEVMODE_ENABLED=true -e AXONIQ_AXONSERVER_STANDALONE=true axoniq/axonserver`
- **H2 Database**: Embedded for projections (rental_db.mv.db, payment_db.mv.db)

**Application Ports**:
- Rental service: 8081 (serves backend API and UI on port 8080 in production)
- Payment service: 8082
- Frontend dev server: 3000 (Nuxt development mode)

### Frontend Technology Stack
- **Vue 3** with **Nuxt 3** framework
- **Vuetify 3** as UI component library
- **Pinia** for state management
- **TypeScript** support
- Build tool: **Vite** (via Nuxt)
- Connects to backend REST APIs for bike management
- Real-time updates via **Server-Sent Events** (EventSource)

### Native Compilation Support
GraalVM native compilation is supported via the `native` Maven profile with Axon Spring AOT extensions.