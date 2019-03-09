import java.util.Collections
import java.util.Properties

import com.landoop.telecom.telecomitalia.telecommunications.smsCallInternet

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler
import org.apache.kafka.streams.kstream.Consumed

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import za.co.synthesis.dog
import za.co.synthesis.update

class ExampleService {
    companion object {
        val schema_registry_url = "http://localhost:8081"
        val kafka_app = "example-service"

        val builder = StreamsBuilder()
        val stringSerde = Serdes.serdeFrom(String::class.java)

        @JvmStatic
        fun main(args: Array<String>) {

//            avroExample()
//            filterExample()
            produceDog()
            produceUpdate()
            dogExample()

            val streams = KafkaStreams(builder.build(), getKafkaProperties(kafka_app = kafka_app))
            streams.start()
        }

        fun avroExample() {
            val serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schema_registry_url)
            val italiaSerde = SpecificAvroSerde<smsCallInternet>()
            italiaSerde.configure(serdeConfig, false)

            builder.stream("telecom_italia_data", Consumed.with(stringSerde, italiaSerde))
                    .foreach({ key, message -> println("Received message") })
        }

        fun filterExample() {
            builder.stream("lala-test", Consumed.with(stringSerde, stringSerde))
                    .filter({ key, message -> message == "hello" })
                    .foreach({ key, message -> println("Message: $message") })
        }

        fun produceDog() {
            val serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schema_registry_url)
            val dogSerde = SpecificAvroSerde<dog>()
            dogSerde.configure(serdeConfig, false)

            val producer = KafkaProducer<String, dog>(getKafkaProperties())
            val minki = dog("Minki", 7, "Dachshund")
            producer.send(ProducerRecord<String, dog>("dogs", minki)).get()
            producer.flush()
            producer.close()
        }

        fun dogExample() {
            val serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schema_registry_url)
            val dogSerde = SpecificAvroSerde<dog>()
            dogSerde.configure(serdeConfig, false)

            builder.stream("dogs", Consumed.with(stringSerde, dogSerde))
                    .foreach({ key, message -> println(message) })
        }

        fun produceUpdate() {
            val serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schema_registry_url)
            val updateSerde = SpecificAvroSerde<update>()
            updateSerde.configure(serdeConfig, false)

            val producer = KafkaProducer<String, update>(getKafkaProperties())
            val anUpdate = update("DOG", "CREATE", "{ name: 'Minki' }")
            producer.send(ProducerRecord<String, update>("updates", anUpdate)).get()
            producer.flush()
            producer.close()
        }

        fun joinExample() {
            // Customer complaint

            // Trigger of customer applying for credit from bureau

            // Join and alert
        }


        fun getKafkaProperties(
                kafka_server: String = "localhost:9092",
                kafka_app: String = "MyApp",
                kafka_stream_threads: Int = 1
        ): Properties {
            val properties = Properties()

            properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_server)
            properties.put(StreamsConfig.APPLICATION_ID_CONFIG, kafka_app)
            properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, kafka_stream_threads)
            properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler::class.java)

            // This is for producers really only
            //
            properties["key.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
            properties["value.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
            properties.put("schema.registry.url", schema_registry_url);

            return properties
        }
    }
}