# Kafka Playground

This is a basic example & reference repo for Kafka using Kotlin and Gradle.  After venturing into microservices you'll soon discover Kafka:

- [Trurning the DB inside-out](https://www.confluent.io/blog/turning-the-database-inside-out-with-apache-samza/)
- [The Data Dichotomy](https://www.confluent.io/blog/data-dichotomy-rethinking-the-way-we-treat-data-and-services/)
- [Example Tutorial](https://medium.com/@stephane.maarek/how-to-use-apache-kafka-to-transform-a-batch-pipeline-into-a-real-time-one-831b48a6ad85)
- [Advanced Tools Comparison](https://medium.com/@stephane.maarek/the-kafka-api-battle-producer-vs-consumer-vs-kafka-connect-vs-kafka-streams-vs-ksql-ef584274c1e)

## Quickstart

```sh
# Kafka dev docker environment courtesy of Landoop
$ docker run --rm --net=host landoop/fast-data-dev

$ gradle build
$ gradle run
```

**IntelliJ IDEA** works great for Kafka dev

## Local Development

Although you can setup everything you need for kafka on your machine, a great way to get started with Kafka is with a docker image from Landoop:

```sh
$ docker run --rm --net=host landoop/fast-data-dev
```

## Avro

Avro is a popular tool for data schema's and coupled with Confluent's [schema registry](https://medium.com/@stephane.maarek/introduction-to-schemas-in-apache-kafka-with-the-confluent-schema-registry-3bf55e401321) it's a winner.

## Gradle & Kotlin

At the moment the full power of Kafka is only available through the JVM.  I prefer a nice functional language, Kotlin and use Gradle as my package manager and build tool.

`gradle build` will also generate Java classes from Avro schemas

## Running Kafka locally

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