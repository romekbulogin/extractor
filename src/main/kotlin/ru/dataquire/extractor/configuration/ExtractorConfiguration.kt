package ru.dataquire.extractor.configuration

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import ru.dataquire.extractor.configuration.properties.ExtractorProperties

private const val REGISTRATION_PATH: String = "/v1/api/coordinator/registration"

@Configuration
class ExtractorConfiguration(
    private val server: ServerProperties,
    private val extractorProperties: ExtractorProperties
) {
    private val logger: Logger = LoggerFactory.getLogger(ExtractorConfiguration::class.java)

    @PostConstruct
    fun registration() {
        if (extractorProperties.coordinator != null) {
            logger.debug("[REGISTRATION] Starting registration in Coordinator...")
            val restClient = RestClient.create()
            val response = restClient.post()
                .uri("${extractorProperties.coordinator.address}${REGISTRATION_PATH}")
                .body(server.port)
                .retrieve()
                .toEntity<String>()
            require(response.statusCode == HttpStatus.OK) { "Registration failed" }
            logger.info("[REGISTRATION] Registration in Coordinator successfully")
        } else {
            logger.warn("[REGISTRATION] Registration skipped")
        }
    }

    @Bean
    fun mapper() = jacksonObjectMapper().apply {
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        registerModule(JavaTimeModule())
    }
}