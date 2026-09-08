# FAK — Formal Agent Kernel

Runtime policy enforcement for AI agents.
University of Warwick MSc Dissertation 2026 — Saurav Jaiswal

---

## What is FAK?

FAK sits between an AI agent and its tools. Before any tool executes,
the agent submits an **ActionEnvelope** to the FAK kernel, which evaluates
a YAML policy and returns **ALLOW**, **DENY**, or **REQUIRE_APPROVAL**.

```
Agent ──► ActionEnvelope ──► FAK Kernel ──► ALLOW / DENY / REQUIRE_APPROVAL
                                   │
                              AuditEvent (persisted)
```

---

## Architecture

| Component       | Technology                        | Port  |
|-----------------|-----------------------------------|-------|
| Policy kernel   | Spring Boot 3, JPA, H2            | 8080  |
| Agent bridge    | FastAPI                           | 8000  |
| Agent runtime   | LangGraph + OpenAI (gpt-4o-mini)  | —     |
| Frontend        | Next.js 16                        | 3000  |

---

## Prerequisites

- **Java 23+** — `java --version`
- **Maven 3.9+** — `mvn --version`
- **Python 3.11+** — `python3 --version`
- **Node.js 18+** — `node --version`
- **npm 9+** — `npm --version`

---

## Environment setup

Create `agents/.env`:

```
OPENAI_API_KEY=sk-proj-...your key here...
FAK_URL=http://localhost:8080
```

Get a free API key at https://platform.openai.com

---

## Running locally (3 terminals)

### Terminal 1 — FAK Kernel (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Wait for: `Started FakApplication in X.XXX seconds`

Endpoints:
- `POST http://localhost:8080/api/v1/validate`
- `GET  http://localhost:8080/api/v1/health`
- `GET  http://localhost:8080/api/v1/audit/events`
- `GET  http://localhost:8080/api/v1/metrics`
- `POST http://localhost:8080/api/v1/policies/test`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/fak`, User: `sa`, Password: blank)

---

### Terminal 2 — Agent Bridge (FastAPI)

```bash
cd agents

# First time only
python3 -m venv venv
source venv/bin/activate
pip install fastapi uvicorn langchain-openai langgraph requests pydantic python-dotenv

# Start
uvicorn api:app --reload --port 8000
```

Wait for: `Uvicorn running on http://127.0.0.1:8000`

---

### Terminal 3 — Frontend (Next.js)

```bash
cd frontend
npm install        # first time only
npm run dev
```

Wait for: `▲ Next.js 16 — ready on http://localhost:3000`

Open **http://localhost:3000**

---

## Pages

| URL | Description |
|-----|-------------|
| `http://localhost:3000` | Validation demo |
| `http://localhost:3000/chat` | Agent chat UI |
| `http://localhost:3000/policies` | Policy Manager |

---

## Demo scenarios (chat page)

| Scenario | Expected FAK decision |
|----------|-----------------------|
| Deploy payments-service to staging | ALLOW |
| Deploy payments-service to production | REQUIRE_APPROVAL |
| Run `rm -rf /var/app/data` | DENY |
| Health check command | ALLOW |

---

## Policy configuration

Policies live in `backend/src/main/resources/configs/policies.yml`.

To hot-reload without restarting:
1. Go to `http://localhost:3000/policies`
2. Click **+ New Draft**, paste updated YAML
3. Click **Activate This Version**

---

## Project structure

```
.
├── backend/                   Spring Boot kernel
│   └── src/main/java/com/fak/
│       ├── core/              ActionEnvelope, ConstraintEngine, ValidationService
│       ├── config/            PolicyConfig, ConfigRegistry (hot-reload)
│       ├── policy/            PolicyVersion CRUD + activation
│       ├── audit/             AuditEvent persistence
│       └── api/               FakController (/api/v1/*)
├── agents/
│   ├── .env                   OpenAI API key (create this yourself)
│   ├── api.py                 FastAPI bridge
│   └── langgraph_deploy_agent/
│       ├── agent.py           LangGraph ReAct agent (gpt-4o-mini)
│       ├── protected_tools.py FAK-gated deploy + shell tools
│       └── fak_client.py      Python FAK client
├── frontend/                  Next.js 16 app
│   └── app/
│       ├── page.tsx           Validation demo
│       ├── chat/page.tsx      Agent chat UI
│       └── policies/page.tsx  Policy Manager
└── docker-compose.yml         Single-command startup
```

---

*Saurav Jaiswal · sauravjaiswal999@gmail.com · University of Warwick · 2026*
