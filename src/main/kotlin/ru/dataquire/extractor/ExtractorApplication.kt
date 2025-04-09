package ru.dataquire.extractor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class]
)
class ExtractorApplication

fun main(args: Array<String>) {
    runApplication<ExtractorApplication>(*args)
}
