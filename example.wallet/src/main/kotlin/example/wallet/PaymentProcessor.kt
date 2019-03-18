package example.wallet

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import polaris.kafka.PolarisKafka

fun main(args : Array<String>) {

    with(PolarisKafka("payments-processor", "localhost:9092", "http://localhost:8081")) {

        val paymentsInflight = topic<TransactionKey, Payment>("payments-inflight")
        val transactions = topic<TransactionKey, Transaction>("transactions")

        consumeStream(paymentsInflight)
                .map { key, payment ->
                    KeyValue(key, Transaction(payment.getFromAccount(),
                            payment.getAmount(),
                            payment.getReference(),
                            payment.getDescription(),
                            "DEBIT"))
                }
                .through(transactions.topic, transactions.producedWith())
                .groupByKey()
                // As soon as we materialize in aggregate call below
                // Kafka starts the previous map multiple times in parallel before our aggregation completes
                .aggregate({ 0 }, { _, transaction: Transaction, balance : Int ->
                    if (transaction.getType() == "CREDIT") {
                        balance + transaction.getAmount()
                    }
                    else {
                        balance - transaction.getAmount()
                    }
                }, Materialized.`as`<TransactionKey, Int, KeyValueStore<Bytes, ByteArray>>("balances")
                        .withCachingDisabled()
                        .withKeySerde(transactions.keySerde)
                        .withValueSerde(Serdes.Integer()))
                .toStream { key, balance ->
                    println("Account: ${key.getFromAccount()}, Balance: $balance")
                }

        start()
    }
}