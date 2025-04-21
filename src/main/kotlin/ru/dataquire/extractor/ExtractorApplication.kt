package ru.dataquire.extractor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.dataquire.extractor.configuration.properties.ExtractorProperties

@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class]
)
@EnableConfigurationProperties(value = [ExtractorProperties::class])
class ExtractorApplication

fun main(args: Array<String>) {
    runApplication<ExtractorApplication>(*args)
}
