import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.landoop.telecom.telecomitalia.telecommunications.smsCallInternet;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class BlueprintService {

    static String kafka_server = "localhost:9092";
    static String schema_registry_url = "http://localhost:8081";
    static String kafka_topic = "telecom_italia_data";
    static String kafka_app = "blueprint-service"; // Think this is used to remember where this app last read the stream
    static int kafka_stream_threads = 1;

    public static KafkaStreams streams;

    public static void main(String[] args) throws Exception {
        final StreamsBuilder builder = new StreamsBuilder();
        
        // Serde
        //
        Serde<String> stringSerde = Serdes.serdeFrom(String.class);
        final Map<String, String> serdeConfig =
                Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schema_registry_url);        
        final SpecificAvroSerde<smsCallInternet> italiaSerde = new SpecificAvroSerde<>();
        italiaSerde.configure(serdeConfig, false);

        builder.stream(kafka_topic, Consumed.with(stringSerde, italiaSerde))
            .foreach((key, message) -> {
                System.out.println("Received message");
            });            

        streams = new KafkaStreams(builder.build(), getKafkaProperties());
        streams.start();
    }

    static Properties getKafkaProperties() {
        Properties properties = new Properties();

        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka_server);
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, kafka_app);
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, kafka_stream_threads);
        properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        return properties;
    }
}
