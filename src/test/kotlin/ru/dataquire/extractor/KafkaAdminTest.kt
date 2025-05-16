package ru.dataquire.extractor

import org.apache.kafka.clients.admin.AdminClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin

@SpringBootTest
class KafkaAdminTest(
    @Autowired private val kafkaAdmin: KafkaAdmin
) {
    private val url: String = "jdbc:postgresql://localhost:5432/family_tree"

    @Test
    fun createTopicTest() {
        AdminClient.create(kafkaAdmin.configurationProperties).use {
            val topic = TopicBuilder
                .name("test")
                .partitions(2)
                .build()
            it.createTopics(listOf(topic))
        }
    }
}