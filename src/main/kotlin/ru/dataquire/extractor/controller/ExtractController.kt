package ru.dataquire.extractor.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.dataquire.extractor.dto.request.ExtractRequest
import ru.dataquire.extractor.service.ExtractService

@RestController
@RequestMapping("/v1/api/extractor")
class ExtractController(
    private val extractService: ExtractService
) {

    @PostMapping("/extract")
    fun extract(@RequestBody request: ExtractRequest) = extractService.extract(request)
}