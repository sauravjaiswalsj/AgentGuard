#!/bin/bash
set -e

echo "=== FAK - Formal Agent Kernel ==="
echo ""

# Check prerequisites
command -v java >/dev/null 2>&1 || { echo "ERROR: Java 21+ is required. Install from https://adoptium.net"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "ERROR: Maven is required. Install from https://maven.apache.org"; exit 1; }
command -v node >/dev/null 2>&1 || { echo "ERROR: Node.js 18+ is required. Install from https://nodejs.org"; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "ERROR: Python 3.11+ is required. Install from https://python.org"; exit 1; }

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Check OpenAI key
if [ ! -f "$ROOT_DIR/agents/.env" ] || grep -q "YOUR_OPENAI_API_KEY_HERE" "$ROOT_DIR/agents/.env"; then
  echo "ERROR: Please set your OpenAI API key in agents/.env"
  echo "  OPENAI_API_KEY=sk-proj-..."
  exit 1
fi

echo "[1/3] Starting backend (Spring Boot)..."
cd "$ROOT_DIR/backend"
mvn spring-boot:run -q &
BACKEND_PID=$!

echo "[2/3] Starting agent server (FastAPI)..."
cd "$ROOT_DIR/agents"
if [ ! -d "venv" ]; then
  python3 -m venv venv
  source venv/bin/activate
  pip install -q -r requirements.txt
else
  source venv/bin/activate
fi
uvicorn server:app --port 8000 &
AGENT_PID=$!

echo "[3/3] Starting frontend (Next.js)..."
cd "$ROOT_DIR/frontend"
if [ ! -d "node_modules" ]; then
  npm install -q
fi
npm run dev &
FRONTEND_PID=$!

echo ""
echo "=== All services starting ==="
echo "  Frontend:  http://localhost:3000"
echo "  Backend:   http://localhost:8080"
echo "  Agent API: http://localhost:8000"
echo ""
echo "Press Ctrl+C to stop all services."

# Stop all on exit
trap "echo 'Stopping...'; kill $BACKEND_PID $AGENT_PID $FRONTEND_PID 2>/dev/null; exit" INT TERM

wait
