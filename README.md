# University Management System - Microservices

## System Requirements
- Java 17
- Maven
- Oracle Database
- ELK Stack (for centralized logging)

## Database Setup
1. Create Oracle PDBs for each service:
   - STUDENT_PDB
   - COURSE_PDB
   - TEACHER_PDB
   - AUTH_PDB
   - ATTENDANCE_PDB

2. For each PDB, use the following credentials:
   - Username: system
   - Password: oracle123

## How to Run the System

### Step 1: Build the Project
```bash
mvn clean install
```

### Step 2: Start Services in Order

1. **Start Service Discovery (Eureka Server)**
```bash
cd service-discovery
mvn spring-boot:run
```

2. **Start Config Server**
```bash
cd config-server
mvn spring-boot:run
```

3. **Start Auth Service**
```bash
cd auth-Service
mvn spring-boot:run
```

4. **Start Domain Services**
```bash
# Start these services in separate terminals
cd student-service
mvn spring-boot:run

cd course-service
mvn spring-boot:run

cd teacher-service
mvn spring-boot:run

cd attendance-service
mvn spring-boot:run
```

5. **Start API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
```

### Step 3: Verify Services
- Service Discovery: http://localhost:8761
- API Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- Student Service: http://localhost:8082
- Course Service: http://localhost:8083
- Teacher Service: http://localhost:8084
- Attendance Service: http://localhost:8085

## Centralized Logging (ELK Stack)
1. Install and start Elasticsearch, Logstash, and Kibana
2. Configure Logstash to listen on port 5000
3. Access Kibana dashboard at http://localhost:5601

## Troubleshooting
- If you encounter database connectivity issues, verify that Oracle PDBs are created and accessible
- Check application logs for specific error messages
- Ensure services are started in the correct order