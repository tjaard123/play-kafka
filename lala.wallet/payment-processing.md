# Payment processing

To minimize data loss and preserve message order:

- `replication.factor` - 3+
- `acks` - all
- `min.insync.replicas` - 2+
- `unclean.leader.election.enable` - false
- `max.in.flight.requests.per.connection` - 1

## Enabling Exactly Once Semantics

Producer

- `enable.idempotence` - true (Default: false)
- Use the new transactions API
- Set `transactional.id` to guarantee that transactions using the same transactional ID have completed prior to starting new transactions

Consumer

- `isolation.level` - `read_committed` (Default: `read_uncommitted` )