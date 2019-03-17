package example.wallet

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

fun main(args : Array<String>) {

    val properties = Properties()
    properties[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    val partitions = 1
    val replicationFactor = 1

    val adminClient = AdminClient.create(properties)

    val topics = Arrays.asList(
            NewTopic("payments-inflight", partitions, replicationFactor.toShort()),
            NewTopic("transactions", partitions, replicationFactor.toShort())
    )

    val result = adminClient.createTopics(topics)
    try {
        result.all().get(60, TimeUnit.SECONDS)
    } catch (e: ExecutionException) {
        if (e.cause is TopicExistsException) {
            // TopicExistsException - Swallow this exception, just means the topic already exists
            println(e.message)
        }
        else {
            throw e
        }
    }
}