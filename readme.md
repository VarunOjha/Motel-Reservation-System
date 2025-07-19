# Motel Management System APIs (Practice Project)

Welcome to the **Motel Management System** – a purely educational project developed to practice and implement system design concepts, with no commercial intent.

This project is inspired by learnings from the book *System Design Interview* and AI-driven insights from *Vibe Coding*. The system mimics a platform similar to Airbnb, tailored specifically for motels.

---

## 🚀 Project Overview

This is a hypothetical system designed to simulate a backend service for motel operations. The system supports:

1. Onboarding motel chains and adding room inventory.
2. Searching for motels by location.
3. Viewing motel room availability and pricing.
4. Booking motel rooms.
5. Processing payments for reservations.
6. Handling various motel-specific operational scenarios.

---

## 🛠️ Tech Stack

- **Java + Spring Boot**: Backend service development.
- **PostgreSQL**: Primary database for motel and reservation data.
- **Gradle**: Build and dependency management.
- **Docker**: Containerization of application and database.
- **Swagger/OpenAPI**: API documentation and testing.
- **Neon**: Serverless PostgreSQL for on-demand data handling.
- **Kubernetes** *(future scope)*: Service scaling and orchestration.

---

## 📦 Scope

### ✅ Motel Management APIs
- CRUD operations for:
  - Motel Chains
  - Motel Locations
  - Room Categories
  - Rooms

### ✅ Room Inventory APIs
- Check:
  - Room rates
  - Room availability (`startDate` to `endDate`)

### ✅ Room Reservation APIs
- Book rooms for a specific duration (`startDate` to `endDate`)

### ✅ Payments & Ledger
- Add conceptual:
  - Payment APIs
  - Ledger system for tracking transactions

### ✅ Additional Features
- Use of **Go routines** *(planned integration)* for concurrency modeling
- **Kubernetes setup** for service orchestration and scaling *(exercise scope)*
- Implement **shadow traffic** to simulate API usage
- Add **API authentication** using multiple strategies
- Create **test cases** for coverage and reliability

---

## 🐳 Docker Setup

- Dockerized Spring Boot + PostgreSQL development environment
- Includes a pre-configured PostgreSQL container for local use

---

## 📌 Project Goals

This project is a sandbox to explore:
- System design principles
- API design patterns
- Scalable architecture with microservices
- Integration of advanced features like shadow traffic and serverless databases

---

## 📚 Learning Sources

- *System Design Interview* (Book)
- *AI-assisted learning through Vibe Coding*

---

## ❗ Disclaimer

This is a **practice project only**. It is not meant for production or commercial deployment. All work is intended to reinforce personal learning in backend systems and architecture.

---

## 📬 Contributions & Feedback

While this project is educational, feedback or suggestions to enhance learning and code quality are always welcome.

