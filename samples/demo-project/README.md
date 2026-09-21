# Demo Store API Service

Welcome to Demo Store API service documentation.

## Prerequisites
- Java 11
- Maven 3.8+

## Configuration
- server.port=8080

## Endpoints

### 1. User Management
- **POST /api/users**: Register new user.
  - Accepts parameters: name, email

### 2. Product Catalog
- **GET /api/products**: Fetch all active store products.

### 3. Database Schema
- Table `users`: contains columns `name`, `email`
