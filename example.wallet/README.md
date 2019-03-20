# Wallet & payment processing example

Objective
- Produce 10 payments to topic `payments-inflight`: `{ fromAccount: "alice", toAccount: "bob", amount: 1 }`
- Consumer (`PaymentProcessor.kt`) split payment into a debit and credit transaction on the `transactions` topic
- Display the balance after every payment

Objective output:
```sh
Account: alice, Balance: -1
Account: alice, Balance: -2
Account: alice, Balance: -3
Account: alice, Balance: -4
Account: alice, Balance: -5
Account: alice, Balance: -6
Account: alice, Balance: -7
Account: alice, Balance: -8
Account: alice, Balance: -9
Account: alice, Balance: -10
```

## IntelliJ setup

![../docs/intellij.jpg](../docs/intellij.jpg)

1. Run CreateTopicsKt
2. Start PaymentsProcessorKt
3. Run PaymentsProducerKt

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