# ADR-0004: Implement OAuth2 + JWT Hybrid Authentication

## Status
Accepted

## Context
Leafresh needs a user-friendly authentication system that supports social login while maintaining server-side control over user sessions. The system requires stateless authentication for REST APIs, token refresh capabilities, and the ability to revoke access when needed. Frontend is implemented as a Single Page Application (SPA) requiring token-based authentication.

## Decision
Implement OAuth2 social login (Kakao) for user onboarding, followed by server-issued JWT tokens for API authentication.

## Consequences

### Positive
- **User Experience**: Simplified registration with Kakao social login
- **Server Control**: Full control over token lifecycle and user sessions
- **Stateless Design**: JWT tokens enable horizontal scaling without session storage
- **Security Features**: Token refresh rotation, Redis-based blacklisting, configurable expiration
- **Flexibility**: Easy to extend to additional OAuth providers (Google, Naver)
- **SPA Compatibility**: Perfect fit for React frontend architecture

### Negative
- **Implementation Complexity**: Requires handling both OAuth2 flow and JWT generation
- **Token Management**: Need to implement refresh token rotation and blacklisting
- **Security Overhead**: JWT tokens require careful validation and expiration handling

## Alternatives Considered

### Pure OAuth2 (External Token Dependency)
- **Pros**: Simpler implementation, delegated token management
- **Cons**: External dependency for every API call, limited server control
- **Verdict**: Rejected due to external service dependency and control limitations

### Session-Based Authentication Only
- **Pros**: Simple server-side session management
- **Cons**: Stateful design incompatible with scaling, poor SPA support
- **Verdict**: Rejected due to scaling and frontend architecture requirements

### JWT-Only (No Social Login)
- **Pros**: Full control over authentication flow
- **Cons**: Poor user experience with manual registration
- **Verdict**: Rejected due to user experience requirements

## Implementation Details

### Authentication Flow
1. User initiates Kakao OAuth2 login
2. Backend receives OAuth2 authorization code
3. Backend exchanges code for user information
4. Backend generates internal JWT access/refresh token pair
5. Subsequent API calls use server-issued JWT tokens

### Security Measures
- **Token Expiration**: Access tokens (1 hour), Refresh tokens (30 days)
- **Token Rotation**: New refresh token issued on each refresh
- **Blacklisting**: Redis-based token revocation for logout
- **CORS Configuration**: Strict origin validation for SPA
- **CSRF Protection**: Token-based CSRF prevention

### Future Extensibility
- Additional OAuth2 providers can be easily integrated
- Custom user registration can be added alongside social login
- Multi-factor authentication can be layered on top of current system