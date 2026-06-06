# Formal Agent Kernel Prototype

FAK is implemented as a modular monolith matching the dissertation architecture: API layer, guardrails, router, multi-agent manager, FAK core, execution simulation, configuration registry, audit log, replay, and metrics.

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080` and uses H2 by default. To use PostgreSQL, start Docker Compose and set:

```bash
export FAK_DB_URL=jdbc:postgresql://localhost:5432/fak
export FAK_DB_USER=fak
export FAK_DB_PASSWORD=fak
```

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

## Verify

```bash
cd backend
mvn test
```

## Demo Agents

```bash
python3 agents/sql_agent_demo.py
python3 agents/devops_agent_demo.py
```

# Terminal 1 — FAK backend (Spring Boot)
cd Documents/Warwick/Disertation/backend
mvn spring-boot:run

# Terminal 2 — Agent API bridge (FastAPI)
cd Documents/Warwick/Disertation/agents
source venv/bin/activate
pip install fastapi uvicorn   # if not already installed
uvicorn api:app --reload --port 8000

# Terminal 3 — Next.js frontend
cd Documents/Warwick/Disertation/frontend
npm run dev