# FAK Backend

Spring Boot backend for the Formal Agent Kernel prototype.

## IntelliJ Setup

1. Open this `backend` folder directly in IntelliJ IDEA.
2. When prompted, import it as a Maven project.
3. Set the Project SDK to Java 23:
   - `File` -> `Project Structure` -> `Project SDK` -> Java 23.
4. Wait for Maven indexing to finish.
5. Run the saved configuration named `Fak Backend`.

If IntelliJ still shows `release version 5 not supported`, force a Maven reload:

```text
Maven tool window -> Reload All Maven Projects
```

Then check:

```text
File -> Project Structure -> Project -> Language level: 23
File -> Project Structure -> Modules -> fak-backend -> Language level: 23
Settings -> Build, Execution, Deployment -> Compiler -> Java Compiler -> Target bytecode version: 23
```

The backend starts on:

```text
http://localhost:8080
```

Useful endpoints:

```text
GET  /api/v1/health
POST /api/v1/validate
POST /api/v1/policies/test
GET  /api/v1/audit/events
GET  /api/v1/metrics
```

## CLI

```bash
mvn test
mvn spring-boot:run
```

The default database is file-backed H2 at `backend/data/fak`. PostgreSQL can be enabled with:

```bash
export FAK_DB_URL=jdbc:postgresql://localhost:5432/fak
export FAK_DB_USER=fak
export FAK_DB_PASSWORD=fak
```
