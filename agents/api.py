from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import sys, os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "langgraph_deploy_agent"))

from agent import run
from protected_tools import clear_decisions, get_decisions

app = FastAPI(title="FAK Agent API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    message: str

@app.post("/chat")
def chat(req: ChatRequest):
    clear_decisions()
    result = run(req.message)
    decisions = get_decisions()

    # Enrich steps with structured FAK decision data where available
    steps = result.get("steps", [])
    decision_map = {d.get("tool", ""): d for d in decisions}
    tool_name_map = {"deploy_tool": "deploy_to_cloud", "shell_tool": "run_shell_command"}
    for step in steps:
        key = tool_name_map.get(step["tool_name"], step["tool_name"])
        if key in decision_map:
            step["fak"] = decision_map[key]

    return {
        "response": result["content"],
        "steps": steps,
        "fak_decisions": decisions,
    }

@app.get("/health")
def health():
    return {"status": "ok"}
