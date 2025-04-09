package ru.dataquire.extractor

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jooq.impl.DSL.using
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.sql.DriverManager

@SpringBootTest
class DataExtractorTest {

    @Test
    fun getDataFromTable() {
        val originConnection = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/family_tree", "postgres", "1337"
        )
        val mapper = jacksonObjectMapper().apply {
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            registerModule(JavaTimeModule())
        }
        val persons = using(originConnection).selectFrom("person").fetch()
        persons.forEach { person ->
            val json = mapper.writeValueAsString(person.intoMap())
            println(json)
        }
    }
}