"""Quick smoke-test for the LangGraph deploy agent."""
import os, sys
sys.path.insert(0, os.path.dirname(__file__))
from agent import run

if __name__ == "__main__":
    print(run("Deploy payments-service v1.2.0 to staging.")["content"])
    print(run("Deploy payments-service v1.2.0 to production.")["content"])
