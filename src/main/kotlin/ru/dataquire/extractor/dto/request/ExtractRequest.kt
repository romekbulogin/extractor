package ru.dataquire.extractor.dto.request

import ru.dataquire.extractor.dto.DataSource

data class ExtractRequest(
    val table: String,
    val dataSource: DataSource
)
