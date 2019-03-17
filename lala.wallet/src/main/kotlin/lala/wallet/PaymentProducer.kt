package lala.wallet

import de.huxhorn.sulky.ulid.ULID
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.StreamsConfig
import java.util.*

fun main(args : Array<String>) {

    val properties = Properties()
    properties[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    val paymentsTopic = "payments-inflight"
    val numberOfPayments = 10

    // Serialization config
    //
    properties["schema.registry.url"] = "http://localhost:8081"
    properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = io.confluent.kafka.serializers.KafkaAvroSerializer::class.java
    properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = io.confluent.kafka.serializers.KafkaAvroSerializer::class.java

    // To minimize data loss and preserve message order
    //
    properties["replication.factor"] = 3
    properties[ProducerConfig.ACKS_CONFIG] = "all"
    properties["min.insync.replicas"] = 2
    properties["unclean.leader.election.enable"] = false
    properties["max.in.flight.requests.per.connection"] = 1

    // Producer
    //
    val payment = Payment()
    payment.setFromAccount("tjaard")
    payment.setToAccount("tom")
    payment.setDescription("Coffee")
    payment.setAmount(1)

    val producer = KafkaProducer<TransactionKey, Payment>(properties)

    for (i in 1..numberOfPayments) {
        payment.setReference("#$i")
        producer.send(ProducerRecord<TransactionKey, Payment>(paymentsTopic, TransactionKey(payment.getFromAccount()), payment)) { metadata, exception ->
            if (exception != null) {
                println(exception.toString())
            } else {
                println("Produced message: $metadata")
            }
        }
    }

    producer.flush()
    producer.close()
}