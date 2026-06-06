# pyrefly: ignore [missing-import]
import os
# pyrefly: ignore [missing-import]
from langchain_groq import ChatGroq
# pyrefly: ignore [missing-import]
from langchain_core.tools import tool
# pyrefly: ignore [missing-import]
from langgraph.prebuilt import create_react_agent
# pyrefly: ignore [missing-import]
from langgraph.checkpoint.memory import InMemorySaver
# pyrefly: ignore [missing-import]
from protected_tools import deploy_to_cloud, run_shell_command

@tool
def deploy_tool(service: str, version: str, environment: str) -> str:
    """Deploy a service to an environment. Always calls FAK validation before executing."""
    return deploy_to_cloud(service, version, environment)

@tool
def shell_tool(command: str) -> str:
    """Run a shell command on the host. Always calls FAK validation before executing."""
    return run_shell_command(command)

llm = ChatGroq(
    model="qwen/qwen3.6-27b",
    api_key=os.environ["GROQ_API_KEY"]
)
tools = [deploy_tool, shell_tool]
graph = create_react_agent(
    model=llm,
    tools=tools,
    prompt=("You are a deployment assistant. Use tools to complete tasks. "
        "Always report what FAK decided and why."),
    checkpointer=InMemorySaver(),
)

def run(user_prompt: str) -> dict:
    config = {"configurable": {"thread_id": "1"}}
    result = graph.invoke({"messages": [("human", user_prompt)]}, config=config)

    messages = result["messages"]
    steps = []

    for msg in messages:
        msg_type = type(msg).__name__

        # Agent decided to call a tool
        if msg_type == "AIMessage":
            tool_calls = getattr(msg, "tool_calls", None) or []
            for tc in tool_calls:
                steps.append({
                    "type": "tool_call",
                    "tool_name": tc["name"],
                    "tool_args": tc.get("args", {}),
                    "tool_id": tc.get("id", ""),
                    "result": None,
                })

        # Tool returned a result — pair it with the pending tool_call step
        elif msg_type == "ToolMessage":
            # Find the matching pending tool_call by tool_call_id
            tool_call_id = getattr(msg, "tool_call_id", None)
            matched = False
            if tool_call_id:
                for step in reversed(steps):
                    if step.get("tool_id") == tool_call_id and step["result"] is None:
                        step["result"] = msg.content
                        matched = True
                        break
            # Fallback: attach to latest unresolved step
            if not matched:
                for step in reversed(steps):
                    if step["result"] is None:
                        step["result"] = msg.content
                        break

    final_content = messages[-1].content
    return {"content": final_content, "steps": steps}
