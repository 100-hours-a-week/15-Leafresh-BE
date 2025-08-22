# ADR-0002: Choose Spring Boot for Backend Framework

## Status
Accepted

## Context
Leafresh requires a robust backend framework to handle complex business logic including authentication, AI service integration, challenge management, and e-commerce transactions. The system needs strong transaction support, security features, and extensive ecosystem integration.

## Decision
Use Spring Boot 3.4.5 as the primary backend framework.

## Consequences

### Positive
- **Mature Ecosystem**: Comprehensive security, data access, and integration capabilities
- **Team Expertise**: Strong team familiarity with Spring framework and patterns
- **Transaction Management**: Advanced `@Transactional` support with propagation and rollback
- **Security Integration**: Built-in OAuth2, JWT, CORS, and CSRF protection
- **Auto Configuration**: Reduced boilerplate with intelligent defaults
- **Testing Support**: Comprehensive test slicing and mock capabilities
- **Production Ready**: Actuator endpoints for monitoring and health checks

### Negative
- **Boot Time**: Slower startup compared to lightweight frameworks
- **Memory Usage**: Higher memory footprint due to comprehensive feature set
- **Learning Curve**: Complex configuration for advanced use cases

## Alternatives Considered

### FastAPI (Python)
- **Pros**: Excellent async performance, automatic OpenAPI generation
- **Cons**: Team lacks Python expertise, weaker transaction support, GIL limitations
- **Verdict**: Rejected due to team skill mismatch and transaction requirements

### NestJS (Node.js)
- **Pros**: TypeScript support, familiar to frontend developers
- **Cons**: Single-threaded limitations, weaker enterprise security features
- **Verdict**: Rejected due to concurrency limitations for AI processing

### Go with Gin/Fiber
- **Pros**: Excellent performance, lightweight
- **Cons**: No team experience, limited ecosystem for complex business logic
- **Verdict**: Rejected due to learning curve and development speed requirements

## Implementation Notes
- Clean Architecture pattern adopted with clear layer separation
- Domain-driven design principles for business logic organization
- Comprehensive test coverage using Spring Boot Test slices
- Integration with Spring Security for OAuth2 + JWT hybrid authentication