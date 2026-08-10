# FAK — Formal Agent Kernel

Runtime policy enforcement for AI agents.
University of Warwick MSc Dissertation 2026 — Saurav Jaiswal

## Stack

- Kernel: Spring Boot 3, JPA, H2
- Agent: LangGraph + Groq
- Bridge: FastAPI
- UI: Next.js 14

## Quick start

    cd backend  && ./mvnw spring-boot:run
    cd agents   && uvicorn api:app --reload
    cd frontend && npm run dev
