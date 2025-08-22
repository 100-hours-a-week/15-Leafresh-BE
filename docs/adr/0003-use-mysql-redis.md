# ADR-0003: Use MySQL with Redis for Data Management

## Status
Accepted

## Context
Leafresh requires both transactional data consistency for critical operations (payments, challenge completion, user management) and high-performance caching for frequent reads (challenge feeds, leaderboards, AI verification results). The system needs to handle complex relational data while providing sub-200ms API response times.

## Decision
Use MySQL 8.0.41 as the primary database with Redis 7.0 as the caching and session store.

## Consequences

### MySQL Benefits
- **ACID Compliance**: Full transaction support for payment and challenge completion flows
- **Complex Relationships**: Excellent support for Member ↔ Challenge ↔ Verification relationships
- **Team Expertise**: Strong team experience with MySQL optimization and troubleshooting
- **Ecosystem Support**: Perfect integration with Spring Data JPA and HikariCP
- **Cloud Options**: Available as managed service (Cloud SQL, RDS) for production scaling

### Redis Benefits
- **Performance**: Sub-millisecond response times for cached data
- **Pub/Sub Messaging**: Real-time notifications for challenge updates and AI results
- **Distributed Locking**: Redisson-based concurrency control for time-deal purchases
- **Session Management**: Stateless JWT token blacklisting and user session storage
- **Bloom Filters**: Memory-efficient duplicate verification checking

### Negative Aspects
- **Complexity**: Managing two data stores increases operational overhead
- **Consistency**: Cache invalidation strategies required for data synchronization
- **Memory Usage**: Redis requires sufficient RAM for performance optimization

## Alternatives Considered

### PostgreSQL
- **Pros**: Advanced features, JSON support, better optimizer
- **Cons**: Team lacks PostgreSQL expertise, MySQL sufficient for requirements
- **Verdict**: Rejected due to learning curve without significant benefit

### MongoDB
- **Pros**: Flexible schema, excellent for rapid prototyping
- **Cons**: Poor support for complex transactions, team unfamiliar with NoSQL patterns
- **Verdict**: Rejected due to transaction requirements and expertise gap

### Single Database (MySQL only)
- **Pros**: Simplified architecture, single point of truth
- **Cons**: Cannot achieve sub-200ms response times for frequent queries
- **Verdict**: Rejected due to performance requirements

## Implementation Strategy
- MySQL for persistent, transactional data (users, challenges, orders, verification records)
- Redis for caching (challenge lists, user profiles, leaderboards)
- Redis Pub/Sub for real-time features (AI results, challenge notifications)
- Redisson for distributed locking (time-deal inventory management)
- Cache-aside pattern with TTL-based invalidation for data consistency