# Wallet & payment processing example

![../docs/intellij.jpg](../docs/intellij.jpg)

1. Run CreateTopicsKt
2. Start Processor
3. Run Producer

Producer output:
```sh
Produced message: payments-inflight-0@0
Produced message: payments-inflight-0@1
Produced message: payments-inflight-0@2
Produced message: payments-inflight-0@3
Produced message: payments-inflight-0@4
Produced message: payments-inflight-0@5
Produced message: payments-inflight-0@6
Produced message: payments-inflight-0@7
Produced message: payments-inflight-0@8
Produced message: payments-inflight-0@9
```

Processor output:
```sh
Account: tjaard, Balance: -5
Account: tjaard, Balance: -10
```

## Configuration

To minimize data loss and preserve message order:

- `replication.factor` - 3+
- `acks` - all
- `min.insync.replicas` - 2+
- `unclean.leader.election.enable` - false
- `max.in.flight.requests.per.connection` - 1

Enabling Exactly Once Semantics

Producer

- `enable.idempotence` - true (Default: false)
- Use the new transactions API
- Set `transactional.id` to guarantee that transactions using the same transactional ID have completed prior to starting new transactions

Consumer

- `isolation.level` - `read_committed` (Default: `read_uncommitted` )