package com.fak.routing;

import com.fak.guardrails.GuardrailsService;
import com.fak.orchestration.MultiAgentManager;
import com.fak.orchestration.MultiAgentManager.ClientRequest;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RouterService {
  private final GuardrailsService guardrails;
  private final MultiAgentManager manager;

  public RouterService(GuardrailsService guardrails, MultiAgentManager manager) {
    this.guardrails = guardrails;
    this.manager = manager;
  }

  public Map<String, Object> route(ClientRequest request) {
    Map<String, Object> guardrail = guardrails.inspect(request.query());
    if (!(Boolean) guardrail.get("passed")) {
      return Map.of("status", "DENY", "guardrails", guardrail);
    }
    Map<String, Object> response = manager.handle(request);
    return Map.of("guardrails", guardrail, "workflow", response);
  }
}
