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
User Action → SNS Topic → Multiple SQS FIFO Queues (specialized processing)
                      ↓
            Parallel Business Logic Execution with Guaranteed Ordering
```

### Queue Specialization
- **Payment Processing Queue**: Order payments, refunds, billing updates
- **Inventory Management Queue**: Stock updates, time-deal availability, product management
- **User Notification Queue**: Push notifications, email triggers, real-time updates
- **AI Processing Queue**: Challenge verification, image analysis, result processing

### Reliability Measures
- **SNS Fan-out**: Single publish to multiple specialized SQS FIFO queues
- **Message Deduplication**: SQS deduplication IDs prevent duplicate processing
- **Dead Letter Queues**: Failed messages automatically moved to DLQ for investigation
- **Visibility Timeout**: Configurable timeout prevents message loss during processing
- **Monitoring**: CloudWatch metrics for queue depth, processing rates, and error rates

### Example: Time-deal Purchase Flow
1. User clicks \"Purchase\" → SNS publishes purchase event
2. SNS fans out to multiple queues: Payment, Inventory, Notification
3. Each queue processes in FIFO order: payment authorization, stock deduction, user notification
4. Processing results trigger completion events through the same SNS/SQS pattern
