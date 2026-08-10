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
| Agent runtime   | LangGraph + Groq (qwen3-27b)      | —     |
| Frontend        | Next.js 14                        | 3000  |

---

## Prerequisites

Make sure you have the following installed:

- **Java 23+** — `java --version`
- **Maven 3.9+** — `./mvnw` is bundled, no install needed
- **Python 3.11+** — `python3 --version`
- **Node.js 18+** — `node --version`
- **npm 9+** — `npm --version`

---

## Environment setup

A `.env` file is included in the repo root with the Groq API key.
The agent bridge reads it automatically via the startup command below.

```
GROQ_API_KEY=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
FAK_URL=http://localhost:8080
```

---

## Running locally (3 terminals)

### Terminal 1 — FAK Kernel (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

Wait until you see:
```
Started FakApplication in X.XXX seconds
```

The kernel is now live at **http://localhost:8080**

- Validation endpoint: `POST http://localhost:8080/api/v1/validate`
- Health check:        `GET  http://localhost:8080/api/v1/health`
- H2 console:         `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/fak`
  - User: `sa` / Password: *(leave blank)*

---

### Terminal 2 — Agent Bridge (FastAPI)

```bash
cd agents

# First time only — create and activate a virtual environment
python3 -m venv venv
source venv/bin/activate          # macOS/Linux
# venv\Scripts\activate           # Windows

# First time only — install dependencies
pip install fastapi uvicorn langchain-groq langgraph requests pydantic

# Load the .env and start the server
export $(grep -v '^#' ../.env | xargs)
uvicorn api:app --reload --port 8000
```

Wait until you see:
```
Uvicorn running on http://0.0.0.0:8000
```

The agent bridge is now live at **http://localhost:8000**

- Chat endpoint: `POST http://localhost:8000/chat`
- Health check:  `GET  http://localhost:8000/health`

---

### Terminal 3 — Frontend (Next.js)

```bash
cd frontend

# First time only — install dependencies
npm install

# Start the dev server
npm run dev
```

Wait until you see:
```
▲ Next.js — ready on http://localhost:3000
```

Open **http://localhost:3000** in your browser.

---

## Pages

| URL                          | Description                                      |
|------------------------------|--------------------------------------------------|
| `http://localhost:3000`      | Validation demo — submit envelopes manually      |
| `http://localhost:3000/chat` | Chat UI — talk to the deploy agent, see FAK decisions inline |
| `http://localhost:3000/policies` | Policy Manager — view versions, create drafts, activate |

---

## Demo scenarios (chat page)

| Scenario                  | Expected FAK decision  |
|---------------------------|------------------------|
| Deploy to staging          | ✅ ALLOW               |
| Deploy to production       | ⏳ REQUIRE_APPROVAL    |
| Run `rm -rf /var/app/data` | 🚫 DENY                |
| Health check command       | ✅ ALLOW               |

---

## Running the standalone demo scripts

```bash
cd agents
source venv/bin/activate
export $(grep -v '^#' ../.env | xargs)

# SQL reporting agent scenarios
python3 sql_agent_demo.py

# DevOps deploy agent scenarios
python3 devops_agent_demo.py
```

Both scripts require the FAK kernel (Terminal 1) to be running.

---

## Policy configuration

Policies live in `backend/src/main/resources/configs/policies.yml`.

To change policy at runtime without restarting:

1. Go to `http://localhost:3000/policies`
2. Click **+ New Draft**
3. Paste your updated YAML, add a description and author
4. Click **⚡ Activate This Version**

The kernel hot-reloads the new policy immediately — no restart needed.

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
│       └── api/               FakController (/api/v1/validate)
├── agents/
│   ├── api.py                 FastAPI bridge
│   └── langgraph_deploy_agent/
│       ├── agent.py           LangGraph ReAct agent
│       ├── protected_tools.py FAK-gated deploy + shell tools
│       └── fak_client.py      Python FAK client
├── frontend/                  Next.js 14 app
│   └── app/
│       ├── page.tsx           Homepage / validation demo
│       ├── chat/page.tsx      Agent chat UI
│       └── policies/page.tsx  Policy Manager
├── .env                       API keys (Groq)
└── docker-compose.yml         Single-command startup
```

---

*Saurav Jaiswal · sauravjaiswal999@gmail.com · University of Warwick · 2026*
