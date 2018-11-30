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

# Option 1 - Docker
$ cd blueprint
$ docker build -t play-kafka/blueprint:1.0.0 .
$ docker run --rm play-kafka/blueprint:1.0.0

# Option 2 - Local (Requires java & gradle)
$ gradle build
$ gradle run
```

**IntelliJ IDEA** works great for Kafka dev

## Local Development

Although you can setup everything you need for kafka on your machine, a great way to get started with Kafka is with a docker image from Landoop:

```sh
$ docker run --rm --net=host landoop/fast-data-dev
```

The [Confluent platform](https://www.confluent.io/download/) is also a brilliant dev environment:

```sh
confluent start
```

## Avro

Avro is a popular tool for data schema's and coupled with Confluent's [schema registry](https://medium.com/@stephane.maarek/introduction-to-schemas-in-apache-kafka-with-the-confluent-schema-registry-3bf55e401321) it's a winner.

## KSQL

- [KSQL Video](https://www.youtube.com/watch?v=FD2z3bdN1Jw) & [Examples](https://github.com/rmoff/quickstart-demos)

Deploy a KSQL server to your cluster or start one locally with the confluent platform.  You can port-forward KSQL server.

```sh
$ confluent start
# OR port-forward
$ kubectl port-forward svc/polaris-kafka-cp-ksql-server 8088  

$ ksql
```

```sql
SET 'auto.offset.reset' = 'earliest';

SHOW topics;
PRINT 'topic-name' FROM BEGINNING;

-- Create streams
CREATE STREAM dogs WITH (KAFKA_TOPIC='dogs', VALUE_FORMAT='AVRO');
CREATE STREAM updates WITH (KAFKA_TOPIC='updates', VALUE_FORMAT='AVRO');

-- Create queries
INSERT INTO updates SELECT 'DOG' AS TYPE, 'CREATE' AS ACTION, NAME AS DATA FROM dogs;
-- This query now runs, it is a continous query, with every append on dogs, updates receives an append

show queries;
```

## Kafka Connect

- [JDBC Connector](https://docs.confluent.io/3.2.0/connect/connect-jdbc/docs/source_connector.html)

PostgreSQL and SQLLite supported out of the box, an example config:

```json
{
    "connection.url": "jdbc:postgresql://lena.12345678.eu-west-1.rds.amazonaws.com:5432/db-name?user=user&password=password",
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "incrementing.column.name": "Id",
    "key.converter.schema.registry.url": "http://polaris-kafka-cp-schema-registry:8081",
    "key.converter": "io.confluent.connect.avro.AvroConverter",
    "mode": "incrementing",
    "table.whitelist": "Accounts,Users",
    "topic.prefix": "sql-",
    "validate.non.null": "false",
    "value.converter.schema.registry.url": "http://polaris-kafka-cp-schema-registry:8081",
    "value.converter": "io.confluent.connect.avro.AvroConverter"
  }
```

On a cluster you can port-forward the Kafka connect ui:

```sh
$ kubectl port-forward svc/polaris-kafka-kafka-connect-ui 8000 
```

## Gradle & Kotlin

At the moment the full power of Kafka is only available through the JVM.  I prefer a nice functional language, Kotlin and use Gradle as my package manager and build tool.

`gradle build` will also generate Java classes from Avro schemas

## Kafka tricks

```sh
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

# To create more partitions
kafka-topics --zookeeper cp-kafka-cp-zookeeper:2181 --alter --topic test --partitions 40

#To clear a topic
TOPICS="test test2"
for topic in $TOPICS
do
  echo clearing $topic
  kafka-configs --zookeeper cp-kafka-cp-zookeeper:2181 --alter --entity-type topics --add-config retention.ms=1000 --entity-name $topic
done
sleep 60
for topic in $TOPICS
do
  kafka-configs --zookeeper cp-kafka-cp-zookeeper:2181 --alter --entity-type topics --delete-config retention.ms --entity-name $topic
done
```

## Running Kafka manually

```sh
# Start Zookeeper & Kafka
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
```