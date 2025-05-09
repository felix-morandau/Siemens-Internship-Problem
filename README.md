# Siemens Internship Problem — Refactored & Tested

## Overview
This project implements a simple `Item` CRUD API with asynchronous item processing.

---

## Improvements

### 1. Controller Layer
- **Controllers**: Moved all business logic into `ItemService`.
- **Proper HTTP Status Codes**:  
  - `POST /api/items` → **201 Created**  
  - `DELETE /api/items/{id}` → **204 No Content**  
  - `GET /api/items/{id}` → **404 Not Found** when missing  
- **DTO Validation**:  
  - Added `@Valid @RequestBody` on create/update endpoints  
  - Global `@ControllerAdvice` to process errors and return user friendly error messages

### 2. Service Layer
- **CRUD Methods**:  
  - `findById(id)` with `ItemNotFoundException`  
  - `createItem(dto)`, `updateItem(id, dto)` mapping DTO→Entity  
  - `deleteById(id)` throws if not found
- **Asynchronous Processing** (`processItemsAsync`):  
  - Returns `CompletableFuture<List<Item>>` so callers can wait for completion  
  - Uses a single Spring-managed `taskExecutor` for all subtasks  
  - Coordinates tasks with `CompletableFuture.allOf(...)`  
  - Thread-safe shared resources via `Collections.synchronizedList` and `AtomicInteger`  
  - Exceptions propagate (are not subtile anymore)

### 3. Async Configuration
- Defined a `ThreadPoolTaskExecutor` bean (`taskExecutor`)  
  - Core: 10 threads, Max: 20 threads, Queue: 500  
- Custom `AsyncUncaughtExceptionHandler` to log uncaught async errors

### 4. Testing
- **Unit Tests** for `ItemService`:  
  - CRUD operations, exception paths, item processing
  - 100% coverage
- **Integration Tests** for `ItemController`:  
  - All endpoints, including validation error scenarios via `MockMvc`
  - 100% coverage
- **DTO Validation Tests**:  
  - Direct bean-validation on `CreateItemDTO` to ensure constraints work

---

## Getting Started

1. **Build & Run**  
   ```bash
   mvn clean install
   mvn spring-boot:run
