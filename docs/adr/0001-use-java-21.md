# ADR-0001: Use Java 21 for Backend Development

## Status
Accepted

## Context
Leafresh requires high concurrency handling for AI verification requests, chatbot interactions, and time-deal purchases. The system needs to process thousands of simultaneous requests while maintaining low response times. Spring Boot 3.4.5 requires Java 17+ and officially recommends Java 21.

## Decision
Use Java 21 (LTS) as the primary development language for the backend system.

## Consequences

### Positive
- **Virtual Threads**: Enables handling 100,000+ concurrent threads with minimal memory overhead
- **Performance**: G1GC and ZGC optimizations provide sub-millisecond pause times
- **Spring Compatibility**: Official recommendation for Spring Boot 3.4.5
- **Long-term Support**: LTS version with security updates until 2030
- **Modern Features**: Pattern matching, sealed classes, improved NIO performance

### Negative
- **Learning Curve**: Team needs to understand Virtual Thread programming model
- **Library Compatibility**: Some libraries may not be fully optimized for Java 21
- **Production Stability**: Newer LTS version with less production battle-testing

## Alternatives Considered

### Java 17
- **Pros**: More mature, wider adoption, stable ecosystem
- **Cons**: No Virtual Threads, limited concurrency improvements
- **Verdict**: Rejected due to missing high-concurrency features needed for AI request processing

### Java 11
- **Pros**: Very stable, extensive library support
- **Cons**: Missing modern features, poor concurrency performance, Spring Boot 3.x incompatibility
- **Verdict**: Rejected due to Spring Boot 3.x requirements

## Implementation Notes
- Virtual Threads will be used for AI server communication and blocking I/O operations
- G1GC configured as default for consistent latency
- Gradual migration strategy from platform threads to virtual threads for critical paths