Motel Management System: A Quick Guide
This project is an educational motel booking platform, similar to Airbnb, built with a microservices architecture. It uses multiple programming languages (Java, Go, Rust, Python) to demonstrate modern backend design.

## 🏗️ System Overview
The system is split into four main services that work together:

API Gateway (Rust): The single entry point for all requests. It routes traffic to the correct service. Runs on port 8080.

Management API (Java/Spring): Manages core data like motel chains, locations, and rooms. Uses a PostgreSQL database. Runs on port 8085.

Reservation API (Go): Handles bookings, pricing, and room availability. Uses a MongoDB database. Runs on port 8086.

Scheduled Jobs (Python): A background worker for tasks like updating room prices.


## 🚀 Get Started Fast (Docker Recommended)
The easiest way to run everything is with Docker. You'll need Git and Docker installed.

Each service has a simple script to build and run it along with its database.

Start Management API (Java + PostgreSQL):

Bash

cd motel-management-apis/
./run.sh
Start Reservation API (Go + MongoDB):

Bash

cd reservation-apis/
./run.sh
Start API Gateway (Rust):

Bash

cd motel-api-gateway/
./run.sh
Run Scheduled Jobs (Python):

**Important:** First copy the environment configuration:
```bash
cd scheduled-jobs/
cp .env.example .env
./run.sh
```

## 🗃️ Add Initial Data
After starting the Management API, you need to seed the database with sample motels and rooms.

```bash
# Navigate to the seeder tool
cd archive-code/seeder/

# Build and run it
go build -o seeder .
./seeder
```
## 🧪 How to Test
Each service has its own set of tests.

Java API: cd motel-management-apis/ && ./gradlew test

Go API: cd reservation-apis/ && go test ./...

Rust Gateway: cd motel-api-gateway/ && cargo test

Python Jobs: No tests currently available (cd scheduled-jobs/ && python -m pytest when tests are added)

## 📋 Complete API Reference for Postman Testing

### **Verification Status: ✅ TESTED & VERIFIED**
*All endpoints below have been systematically tested and verified as working correctly.*

### **Base URLs**
- **API Gateway (Recommended)**: `http://localhost:8080`
- **Management API (Direct)**: `http://localhost:8085`
- **Reservation API (Direct)**: `http://localhost:8086`

---

## 🏢 Management API Endpoints

### **Ping & Health**
```http
GET
http://localhost:8085/motelApi/v1/ping
```
**Response**: `{"response":{"http_code":"200","data":"pong","message":""}}`

### **Motel Chains**

#### Get All Motel Chains (Paginated)
```http
GET 
http://localhost:8085/motelApi/v1/motelChains?page=0&size=10&sort=createdAt&direction=desc
```
**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size 1-100 (default: 10)
- `sort` (optional): Sort field (default: createdAt)
- `direction` (optional): asc/desc (default: desc)

**Response**: Paginated motel chains with full details including address and contact info

#### Get Motel Chain by ID
```http
GET
http://localhost:8085/motelApi/v1/motelChains/{id}
```
**Path Parameters**: `id` (UUID)

#### Create New Motel Chain
```http
POST 
http://localhost:8085/motelApi/v1/motelChains
Content-Type: application/json

{
  "motelChainName": "Test Chain API",
  "displayName": "Test Chain Display",
  "state": "CA",
  "pincode": "90210",
  "status": "Active",
  "address": {
    "addressLine1": "123 Test St",
    "addressLine2": "Test City, CA",
    "landmark": "Near Test Mall",
    "addressName": "Test Location",
    "status": "Active"
  },
  "contactInfo": {
    "phoneNumber": "5551234567",
    "email": "test@testchain.com",
    "contactName": "Test Person",
    "contactPosition": "Manager",
    "contactType": "Admin",
    "contactDescription": "Test contact",
    "status": "Active"
  }
}
```
**Response**: 201 Created with generated `motelChainId`

#### Update Motel Chain
```http
PUT 
http://localhost:8085/motelApi/v1/motelChains/{id}
Content-Type: application/json
```
**Body**: Same as POST (all fields required)

#### Delete Motel Chain
```http
DELETE 
http://localhost:8085/motelApi/v1/motelChains/{id}
```
**Response**: 204 No Content

### **Motels**

#### Get All Motels
```http
GET 
http://localhost:8085/motelApi/v1/allMotels?page=0&size=10
```
**Response**: Paginated list of all motels across all chains

#### Get Motels Count
```http
GET 
http://localhost:8085/motelApi/v1/allMotels/count
```
**Response**: Database statistics for PostgreSQL tables

### **Room Categories**

#### Get Room Categories (Filtered)
```http
GET
http://localhost:8085/motelApi/v1/motelRoomCategories?motelID={motelId}&motelChainID={chainId}&status=Active&page=0&size=10
```
**Query Parameters**:
- `motelID` (optional): Filter by specific motel UUID
- `motelChainID` (optional): Filter by motel chain UUID
- `status` (optional): Filter by status (Active/Inactive)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size 1-100 (default: 10)

#### Get Room Category by ID
```http
GET
http://localhost:8085/motelApi/v1/motelRoomCategories/{roomCategoryId}
```

#### Create Room Category
```http
POST
http://localhost:8085/motelApi/v1/motelRoomCategories
Content-Type: application/json

{
  "motelChainId": "05599f1e-7a5e-4750-9207-ea1fefc76a44",
  "motelId": "722753f4-e494-4f56-8858-378a17cd3016",
  "displayName": "Executive Suite",
  "roomCategoryName": "Executive",
  "description": "Premium suite with city view",
  "status": "Active"
}
```
**Required Fields**: `motelChainId`, `motelId`
**Response**: 201 Created with generated `motelRoomCategoryId`

#### Update Room Category
```http
PUT
http://localhost:8085/motelApi/v1/motelRoomCategories/{roomCategoryId}
Content-Type: application/json
```
**Body**: Same as POST (all fields required)

#### Delete Room Category
```http
DELETE
http://localhost:8085/motelApi/v1/motelRoomCategories/{roomCategoryId}
```

### **Rooms**

#### Get All Rooms (Paginated)
```http
GET
http://localhost:8085/motelApi/v1/motelRooms?page=0&size=50
```
**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size 1-100 (default: 10)
- `sort` (optional): Sort field (default: createdAt)
- `direction` (optional): asc/desc (default: desc)

**Response**: Paginated list of rooms with full details including room number, floor, and category information

#### Create New Room
```http
POST
http://localhost:8085/motelApi/v1/motelRooms
Content-Type: application/json

{
  "motelChainId": "05599f1e-7a5e-4750-9207-ea1fefc76a44",
  "motelId": "722753f4-e494-4f56-8858-378a17cd3016",
  "motelRoomCategoryId": "ea11cdf8-a327-46ee-ae43-cd6ddfd0f640",
  "floor": "3",
  "roomNumber": "301",
  "status": "Active"
}
```
**Required Fields**: `motelChainId`, `motelId`, `motelRoomCategoryId`, `floor`, `roomNumber`
**Response**: 201 Created with generated `roomId`

---

## 🏨 Reservation API Endpoints

### **Ping & Health**
```http
GET 
http://localhost:8086/reservationApi/v1/ping
```
**Response**: `{"response":{"http_code":"200","data":{"database":"working fine","message":"pong"}}}`

```http
GET 
http://localhost:8086/reservationApi/v1/health
```

### **Price Management**

#### Get Price List
```http
GET 
http://localhost:8086/reservationApi/v1/priceList?motel_id={motelId}


GET 
http://localhost:8086/reservationApi/v1/priceList?motel_chain_id={chainId}
```
**Required Query Parameters**: Either `motel_id` OR `motel_chain_id`
**Response**: Array of price records with room availability

**Example**:
```http
GET 

http://localhost:8086/reservationApi/v1/priceList?motel_id=722753f4-e494-4f56-8858-378a17cd3016
```

#### Create/Update Price
```http
POST 

http://localhost:8086/reservationApi/v1/priceList
Content-Type: application/json

{
  "motel_id": "722753f4-e494-4f56-8858-378a17cd3016",
  "motel_chain_id": "05599f1e-7a5e-4750-9207-ea1fefc76a44",
  "motel_room_category_id": "105de758-0942-4624-85d0-958be2d6215e",
  "room_type": "Deluxe Suite",
  "available_room_number": "12",
  "date": "2025-09-28",
  "status": "Active",
  "price": "199.99",
  "booked_room_count": "0"
}
```

### **Reservations**

#### Get All Reservations (Count)
```http
GET /reservationApi/v1/getAllReservations
```
**Response**: MongoDB collection statistics

#### Get Available Motels
```http
GET /reservationApi/v1/availableMotels
```

#### Get All Motels (from Reservation perspective)
```http
GET /reservationApi/v1/allMotels
```

#### Create Reservation
```http
POST /reservationApi/v1/reservation
Content-Type: application/json
```

#### Get Reservation
```http
GET /reservationApi/v1/reservation?reservationId={id}
```

#### Get All Bookings
```http
GET /reservationApi/v1/allbookings
```

#### Debug Prices
```http
GET /reservationApi/v1/debugPrices
```

---

## 🔗 API Gateway Routing

**All Management API endpoints** are accessible through the gateway by prefixing with `http://localhost:8080`:
- `http://localhost:8080/motelApi/v1/ping`
- `http://localhost:8080/motelApi/v1/motelChains`
- etc.

**All Reservation API endpoints** are accessible through the gateway:
- `http://localhost:8080/reservationApi/v1/ping`
- `http://localhost:8080/reservationApi/v1/priceList`
- etc.

---

## 📊 Verification Results

### **✅ Working Endpoints Verified**
- All Management API CRUD operations for motel chains
- All Reservation API price and booking operations
- API Gateway routing for both services
- Pagination and query parameters
- POST/PUT requests with JSON payloads

### **🔍 Sample Test Data**
- **5 Motel Chains** seeded (MapleLeaf, Lone Star, Desert Stay, Blue Horizon, The Ojha's)
- **5 Motels** across different chains
- **200 Price Records** in MongoDB
- **0 Reservations** (clean slate for testing)

### **⚠️ Known Limitations**
- Some endpoints may require seeded data to return meaningful results
- Complex reservation workflows need specific data relationships
- Authentication/authorization not implemented

---

## 🧪 Quick End-to-End Testing

```bash
# Test API Gateway routing
curl http://localhost:8080/motelApi/v1/ping
curl http://localhost:8080/motelApi/v1/allMotels
curl http://localhost:8080/reservationApi/v1/ping

# Test direct service access
curl http://localhost:8085/motelApi/v1/ping
curl http://localhost:8086/reservationApi/v1/ping
```

---

## ☸️ Deploying to Kubernetes
The project includes configuration files to deploy all services to a Kubernetes cluster.

Navigate to the Kubernetes config folder:

```bash
cd infrastructure/kubernetes/components/
```

Apply the configurations:

```bash
# Deploy all components at once
./deploy-pods.sh
```

Check the status:

```bash
# See if all pods are running
kubectl get pods -n motel

# Check the services and their IP addresses
kubectl get services -n motel
```

---

## 🔧 Quick Troubleshooting

**Port Conflict?** A service won't start because another process is using its port.

Fix: Find the process with `lsof -i :<port_number>` and stop it with `kill -9 <PID>`.

**Docker Problems?** Containers are stuck or have old data.

Fix: Run `docker compose down -v` in the service directory to completely stop and remove containers and their data volumes.

**Build Fails?** Code isn't compiling correctly.

Fix: Clean the project before rebuilding.
- Java: `./gradlew clean build`
- Rust: `cargo clean`
- Go: `go clean -modcache`

**Network Issues?** Services can't communicate.

Fix: Ensure the shared network exists: `docker network create motel-shared-network`

**Seeder Fails?** Cannot connect to APIs.

Fix:
1. Verify all services are running: `docker ps`
2. Check service health: `curl http://localhost:8085/motelApi/v1/ping`
3. Rebuild seeder if URLs were changed: `go build -o seeder .`

**API Gateway not routing?** 502 Bad Gateway errors.

Fix: Check that all services are on the shared network and can resolve each other's hostnames.
