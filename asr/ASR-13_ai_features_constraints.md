# ASR-13: AI Features Constraints and Compliance

Status: Proposed

## Context

The integration of Artificial Intelligence (AI) features within our applications introduces significant architectural and compliance challenges, specifically concerning data privacy, data protection, and financial regulations across Canada. Applications will expose operations to AI agents through the Model Context Protocol (MCP).

We must ensure compliance with Canadian federal and provincial laws and regulations, including:
- **PIPEDA** (Personal Information Protection and Electronic Documents Act) at the federal level.
- **Quebec Law 25** (Act to modernize legislative provisions as regards the protection of personal information).
- **PIPA** (Personal Information Protection Act) in British Columbia and Alberta.
- **OSFI** (Office of the Superintendent of Financial Institutions) guidelines for financial data and model risk management.

*Out of Scope:* 
- Health data regulations (e.g., PHIPA) are explicitly not covered by this document. Any system handling health data will require a separate review.
- Long-running AI tasks are outside the scope of this ASR and will be addressed in future architectural decisions.

Data safety, transparency, and user consent are core tenets of our architecture. We must define how data is read and modified by AI agents to guarantee regulatory compliance while maintaining a secure and traceable environment.

## Decision

1. **Authentication, Authorization, and Identity Context**
   - MCP connections must propagate the user's OpenID Connect (OIDC) JWT.
   - The AI agent will operate strictly within the bounded authorization scopes and identity of the authenticated user. Agents will not possess elevated "system" privileges.

2. **Data Reads and Ingestion**
   - **Least Privilege & Scoping:** AI agents must only read data the user is explicitly authorized to access via their OIDC token.
   - **De-identification:** Personally Identifiable Information (PII) and sensitive financial data must be de-identified or masked before ingestion by AI models where operationally feasible, to minimize data exposure.
   - **Encryption:** All MCP payloads must be encrypted in transit using TLS 1.2 or higher (or stricter protocols if mandated by specific regional laws).

3. **Data Modifications and Human-In-The-Loop (HITL)**
   - **Explicit Confirmation:** AI agents are prohibited from making autonomous state mutations or data modifications. An explicit Human-In-The-Loop (HITL) checkpoint is mandatory.
   - **User Input:** Before any modification is executed, the end-user must be presented with a clear summary of the proposed action and provide affirmative confirmation. Active session confirmation is sufficient; step-up (elevation) authentication is not required.

4. **Consent and Transparency**
   - **ConsentManager Mandate:** A centralized `ConsentManager` architectural component must be implemented. This component is responsible for acquiring, recording, and enforcing explicit user consent before any AI agent processes their data, ensuring alignment with automated decision-making transparency required by laws such as Quebec Law 25.
   - **Transparency:** The system must visibly indicate to the user when they are interacting with an AI agent or when an action is proposed by AI.

5. **Auditing and Traceability**
   - Every read and write action performed or proposed by an AI agent must be explicitly logged in our centralized auditing system.
   - Audit records must be immutable and clearly tagged with a specific "AI-based" flag, alongside the user's identity and the standard metadata.

6. **Data Residency**
   - Canadian data residency for AI processing and data storage is strongly encouraged. While not strictly mandatory to block non-Canadian cloud services, preference must be given to deployments where data remains within Canadian borders.

## Consequences

- **Positive:** We achieve a high degree of compliance safety regarding Canadian privacy and financial laws. The clear delineation of AI boundaries reduces regulatory risk and builds user trust.
- **Negative (UX Friction):** The mandatory HITL checkpoints for modifications will introduce additional steps in user workflows, potentially disrupting a seamless AI experience.
- **Negative (Architectural Overhead):** Developing, integrating, and maintaining the central `ConsentManager` and ensuring robust OpenID Connect propagation through all MCP boundaries will require significant engineering effort.
- **Negative (Audit Storage):** Explicit, granular logging of all AI actions will increase the volume and storage costs for our centralized audit systems.

