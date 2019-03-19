package example.wallet

/*
Wallet debit / credit example, using interactive queries
 */

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import polaris.kafka.PolarisKafka

fun main(args : Array<String>) {

    with(PolarisKafka("debit-processor", "localhost:9092", "http://localhost:8081")) {
        val paymentsInflight = topic<TransactionKey, Payment>("payments-inflight")
        val transactions = topic<TransactionKey, Transaction>("transactions")

        consumeStream(paymentsInflight)
                .map { key, payment ->
                    KeyValue(key, Transaction(payment.getAmount(),
                            payment.getToAccount(),
                            payment.getReference(),
                            payment.getDescription(),
                            "DEBIT"))
                }
                .through(transactions.topic, transactions.producedWith())

        start()
    }

    with(PolarisKafka("credit-processor", "localhost:9092", "http://localhost:8081")) {

        val transactions = topic<TransactionKey, Transaction>("transactions")
        val transactionsStream = consumeStream(transactions)

        transactionsStream
                .groupByKey()
                .aggregate({ 0 }, { key, transaction: Transaction, balance : Int ->

                    val newBalance : Int
                    if (transaction.getType() == "CREDIT") {
                        newBalance = balance + transaction.getAmount()
                    }
                    else {
                        newBalance = balance - transaction.getAmount()
                    }
                    println("Materialized account [${key.getFromAccount()}], new balance [$newBalance]")
                    newBalance
                }, Materialized.`as`<TransactionKey, Int, KeyValueStore<Bytes, ByteArray>>("BalanceKeyValueStoreQuery")
                        .withCachingDisabled() // Materialize on every message
                        .withKeySerde(transactions.keySerde)
                        .withValueSerde(Serdes.Integer()))

        transactionsStream
                .filter { _, transaction -> transaction.getType() == "DEBIT" }
                .map { key, debitTransaction ->

                    val keyValueStore = streams?.store("BalanceKeyValueStoreQuery", QueryableStoreTypes.keyValueStore<TransactionKey, Int>())
                    val balanceAfterDebit = keyValueStore?.get(key)

                    println("Processing credit for account [${key.getFromAccount()}] of [${debitTransaction.getAmount()}]")
                    println("Account [${key.getFromAccount()}] balance after debit is [${balanceAfterDebit}]")

                    val creditTransaction = debitTransaction
                    creditTransaction.setType("CREDIT")

                    if (balanceAfterDebit != null && balanceAfterDebit >= 0) {
                        // Credit the recipient account
                        println("Crediting [${debitTransaction.getAccountToCredit()}]...")
                        KeyValue(TransactionKey(debitTransaction.getAccountToCredit()), creditTransaction)
                    }
                    else {
                        // Insufficient funds, reverse (CREDIT same account)
                        println("Insufficient funds, reversing...")
                        KeyValue(key, debitTransaction)
                    }
                }
                .to(transactions.topic, transactions.producedWith())

        start()
    }
}