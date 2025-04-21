package ru.dataquire.extractor.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "extractor")
class ExtractorProperties(
    val threadPoolSize: Int,
    val connection: ConnectionProperties,
    val coordinator: CoordinatorProperties?
)