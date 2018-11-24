# Kafka Docker for local dev

```sh
$ docker run --rm --net=host landoop/fast-data-dev
```

# AVRO Schema registry

`gradle build` will generate Java classes from avro schema. This needs to be copied from build/generated-main-avro-java into src/main/java

# Kafka Local

```sh
# Start Zookeeper & Kafka
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

# Create topic
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test

# List topics
bin/kafka-topics.sh --list --zookeeper localhost:2181

# Send messages
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
This is a message
This is another message

# Read topic messages
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
```

Tom's cluster
```
/usr/bin/kafka-console-consumer --bootstrap-server kafka:9092 --topic new-track-topic --from-beginning
/usr/bin/kafka-topics --zookeeper kafka-zookeeper:2181 --list
```