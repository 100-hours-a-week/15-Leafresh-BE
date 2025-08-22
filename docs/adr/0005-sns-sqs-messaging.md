# ADR-0005: Use AWS SNS + SQS FIFO for Messaging Architecture

## Status
Accepted

## Context
Leafresh requires both real-time user feedback and reliable message processing for critical business operations. Time-deal purchases need immediate user response (for UX) while ensuring accurate inventory management and payment processing. AI verification results need real-time delivery to users but also reliable processing for challenge completion.

## Decision
Implement AWS SNS + SQS FIFO messaging architecture for reliable business process handling with fan-out notification capabilities.

## Consequences

### AWS SNS + SQS FIFO Benefits
- **Fan-out Messaging**: SNS enables one-to-many message distribution to multiple subscribers
- **Guaranteed Delivery**: SQS FIFO provides at-least-once delivery with Dead Letter Queue support
- **Message Ordering**: FIFO processing ensures sequential business logic execution
- **Automatic Scaling**: Serverless scaling without infrastructure management
- **Retry Logic**: Built-in exponential backoff and visibility timeout handling
- **Deduplication**: Message deduplication prevents duplicate processing
- **Decoupling**: SNS topics decouple message producers from consumers

### Trade-offs
- **Latency**: Higher latency compared to in-memory solutions (hundreds of milliseconds)
- **Cost**: AWS services incur usage-based charges
- **Complexity**: Managing SNS topics and SQS queues requires proper configuration

## Alternatives Considered

### In-Memory Redis Pub/Sub
- **Pros**: Ultra-low latency, existing infrastructure integration
- **Cons**: Message loss potential, no delivery guarantees, requires Redis Cluster for scaling
- **Verdict**: Rejected due to reliability requirements for business-critical operations

### Apache Kafka
- **Pros**: High throughput, excellent for event streaming, strong ordering guarantees
- **Cons**: Complex infrastructure setup, requires ZooKeeper, team lacks Kafka expertise
- **Verdict**: Rejected due to operational complexity and expertise requirements

### RabbitMQ
- **Pros**: Rich messaging features, good Spring integration
- **Cons**: Requires separate infrastructure management, complex clustering setup
- **Verdict**: Rejected due to infrastructure overhead compared to managed AWS services

## Implementation Strategy

### Message Flow Architecture
```
User Action → Redis Pub/Sub (immediate feedback) + SQS (reliable processing)
           ↓
    Real-time UI Update + Background Business Logic Execution
```

### Use Case Distribution
- **Redis Pub/Sub**: UI notifications, real-time feeds, user interaction feedback
- **AWS SQS**: Payment processing, inventory updates, challenge completion, AI result processing

### Reliability Measures
- **Dual Publishing**: Critical events published to both Redis and SQS
- **Message Deduplication**: SQS deduplication IDs prevent duplicate processing
- **Dead Letter Queues**: Failed messages automatically moved to DLQ for investigation
- **Monitoring**: CloudWatch metrics for queue depth and processing rates

### Example: Time-deal Purchase Flow
1. User clicks "Purchase" → Immediate Redis notification for UI feedback
2. Purchase request → SQS FIFO queue for reliable inventory and payment processing
3. Processing result → Redis notification for real-time status update
4. Final confirmation → SQS for order fulfillment and user notification