# Advanced Prompts — VNair Backend

Purpose: These prompts are structured, executable templates so an AI agent can plan and implement work consistently for the VNair Spring Boot project.

How to use:
- Pick a template matching your task (architecture, refactoring, integration, modernization, multi-agent, learning, performance, scalability, security).
- Fill required input fields at the top (context/problem/goals). Keep VNair fixed context as-is.
- Ask the AI to produce the required YAML output exactly as specified in each file. Your tooling can parse it.
- Use the included checklists before acting. Don’t skip rollout/rollback steps for risky areas (booking/payment).

Conventions:
- Output must follow the YAML blocks defined in each template so it’s machine-readable.
- Prefer incremental, reversible changes with tests and metrics.
- Reflect VNair constraints: keep REST compatibility, DB schema stability, JWT flow, audit trail.

Files:
- `architectural-prompts` — decision records with options and recommendation
- `code-refactoring` — safe, measurable refactors
- `complex-integration-prompt` — payment gateway-grade integration
- `legacy-system-modernisation-prompt` — hybrid/strangler plan
- `multi-agent-orchestration` — roles, handoffs, quality gates
- `new-learning-prompts` — learning with deliverables
- `performance-investigation` — hypothesis-driven perf plan
- `scalability-planning` — phased scaling roadmap
- `security-implementation` — threat-model-first design

Tip: After AI generates YAML, commit it under `docs/adr/` or `docs/plans/` with an ID for traceability.