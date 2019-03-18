package polaris.kafka

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.processor.StateStore
import java.util.*

val KAFKA_BOOTSTRAP_SERVERS_ENVVAR = "kafka_bootstrap_servers"
val KAFKA_SCHEMA_REGISTRY_URL_ENVVAR = "schema_registry_url"

data class SafeTopic<K, V>(
    val topic : String,
    val keySerde : Serde<K>,
    val valueSerde : Serde<V>,
    val properties : Properties) {

    var producer : KafkaProducer<K, V>? = null

    fun consumedWith() : Consumed<K, V> {
        return Consumed.with(keySerde, valueSerde)
    }

    fun producedWith() : Produced<K, V> {
        return Produced.with(keySerde, valueSerde)
    }

    fun serializedWith() : Serialized<K, V> {
        return Serialized.with(keySerde, valueSerde)
    }

    fun <S : StateStore> materializedWith() : Materialized<K, V, S> {
        return Materialized.with(keySerde, valueSerde)
    }

    // Only call this is you want a kafka producer for this topic
    // - keeping in mind the constraint that you can only have a single producer per topic per process
    //
    fun startProducer() {
        producer = KafkaProducer(properties)
    }
}

class PolarisKafka {
    private val properties = Properties()
    private val serdeConfig : Map<String, String>
    private val streamsBuilder : StreamsBuilder
    var streams : KafkaStreams? = null

    constructor(applicationId : String,
                bootstrapServers : String = System.getenv(KAFKA_BOOTSTRAP_SERVERS_ENVVAR),
                schemaRegistryUrl : String = System.getenv(KAFKA_SCHEMA_REGISTRY_URL_ENVVAR)) {

        // Main broker and application config
        //
        properties[StreamsConfig.APPLICATION_ID_CONFIG] = applicationId
        properties[StreamsConfig.CLIENT_ID_CONFIG] = "$applicationId-client"
        properties[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers

        // Schema and serialization config
        //
        properties["schema.registry.url"] = schemaRegistryUrl

        properties[StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG] =
            // LogAndFailExceptionHandler::class.java
            LogAndContinueExceptionHandler::class.java

        serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)

        properties["key.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
        properties["value.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"

        // Metrics for confluent control center
        //
        properties[StreamsConfig.PRODUCER_PREFIX + ProducerConfig.INTERCEPTOR_CLASSES_CONFIG] =
            "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor"

        properties[StreamsConfig.CONSUMER_PREFIX + ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG] =
            "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor"

        // Local state store commit interval
        //
        properties[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = "100"

        // Stream builder used for stream processing
        //
        streamsBuilder = StreamsBuilder()
    }

    fun <K : SpecificRecord, V : SpecificRecord>topic(name : String) : SafeTopic<K, V> {
        // Configure the serdes
        //
        val keySerde = SpecificAvroSerde<K>()
        val valueSerde = SpecificAvroSerde<V>()

        keySerde.configure(serdeConfig, true)
        valueSerde.configure(serdeConfig, false)

        return SafeTopic(name, keySerde, valueSerde, properties)
    }

    fun <K, V>consumeStream(topic : SafeTopic<K, V>) : KStream<K, V> {
        return streamsBuilder.stream(topic.topic, topic.consumedWith())
    }

    fun start() {
        println("Starting streams...")
        val topology = streamsBuilder.build()
        println(topology.describe())
        streams = KafkaStreams(topology, properties)
        streams?.cleanUp()
        streams?.start()
    }

    fun stop() {
        println("Stopping streams...")
        streams?.close()
    }
}

