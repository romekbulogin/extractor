package ru.dataquire.extractor.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.kafka.clients.admin.AdminClient
import org.jooq.impl.DSL.using
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.dataquire.extractor.configuration.properties.ExtractorProperties
import ru.dataquire.extractor.dto.request.ExtractRequest
import ru.dataquire.extractor.dto.response.ExtractResponse
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Executors
import javax.sql.DataSource

@Service
class ExtractService(
    private val mapper: ObjectMapper,
    private val extractorProperties: ExtractorProperties,
    private val kafkaAdmin: KafkaAdmin,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val logger: Logger = LoggerFactory.getLogger(ExtractService::class.java)

    fun extract(request: ExtractRequest): ExtractResponse {
        return try {
            logger.info("[EXTRACT] Starting {}", request)
            HikariDataSource(HikariConfig().apply {
                jdbcUrl = request.dataSource.url
                username = request.dataSource.username
                password = request.dataSource.password
                minimumIdle = extractorProperties.connection.minimumIdle
                maximumPoolSize = extractorProperties.connection.maximumPoolSize
            }).use { dataSource ->
                val countRecordsInTable = dataSource.connection.use { connection ->
                    using(connection).selectFrom(request.table).count()
                }
                if (countRecordsInTable == 0) {
                    logger.warn("[EXTRACT] No records found in {}", request.table)
                    return ExtractResponse(
                        "No records found in ${request.table}",
                        countRecordsInTable
                    )
                }
                val chunkSize = calculateChunkSize(countRecordsInTable)
                val stealingPool = Executors.newWorkStealingPool(extractorProperties.threadPoolSize)

                for (chunk in (1..countRecordsInTable).chunked(chunkSize)) {
                    stealingPool.submit(extractData(dataSource, request, chunk)).get()
                }

                ExtractResponse("Success", countRecordsInTable)
            }
        } catch (ex: IllegalArgumentException) {
            logger.warn("[EXTRACT] request={}. Failed message={}", request, ex)
            ExtractResponse("Failed: ${ex.localizedMessage}", 0)
        } catch (ex: SQLException) {
            logger.error("[EXTRACT] request={}. Failed message={}", request, ex)
            ExtractResponse("Failed: ${ex.localizedMessage}", 0)
        }
    }

    private fun extractData(
        dataSource: DataSource,
        request: ExtractRequest,
        chunk: List<Int>
    ) = Runnable {
        val catalog = request.dataSource.url.split("/", limit = 4).last()
        val topicName = "extract-load-${request.table}-${catalog}-${request.dataSource.url.hashCode()}"

        AdminClient.create(kafkaAdmin.configurationProperties).use { admin ->
            val topic = TopicBuilder
                .name(topicName)
                .config("max.message.bytes", "26214400")
                .replicas(1)
                .partitions(10)
                .build()
            admin.createTopics(listOf(topic))
        }

        dataSource.connection.use { connection ->
            logger.debug("[EXTRACT] Processing chunk {}-{}", chunk.first(), chunk.last())
            val records = using(connection)
                .selectFrom(request.table)

                /**
                 * Можно запоминать последнее записаное число, и от этого идти дальше
                 * в случае отказа воркера
                 */
                .limit(chunk.first() - 1, chunk.last()) // делаем выборку таблицы по частям
                .fetch()
            records.forEach { record ->
                val json = mapper.writeValueAsString(record.intoMap())
                kafkaTemplate.send(topicName, UUID.randomUUID().toString(), json)
                logger.debug("[EXTRACT] {}", json)
            }
        }
    }

    private fun calculateChunkSize(countRecords: Int): Int {
        val chunkSize = countRecords / extractorProperties.threadPoolSize
        return if (chunkSize < 1) countRecords else chunkSize
    }
}