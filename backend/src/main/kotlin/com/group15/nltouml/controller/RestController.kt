package com.group15.nltouml.controller

import com.group15.nltouml.model.AiEngineModel
import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.model.MethodBody
import com.group15.nltouml.service.UMLDiagramGenerationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/uml/v1")
@CrossOrigin(origins = ["*"])
class RestController(
    @Autowired private val umlDiagramGenerationService: UMLDiagramGenerationService
) {
    data class ClientProcessBody (
        val text: String,
        val diagramType: DiagramType,
        val generationMethod: AiEngineModel,
    )

    @PostMapping("/process", produces = [MediaType.IMAGE_PNG_VALUE])
    fun process(@RequestBody body: ClientProcessBody): ResponseEntity<ByteArray> {
        val image = umlDiagramGenerationService.process(body.text, body.diagramType, body.generationMethod)

        return ResponseEntity(image, HttpStatus.CREATED)
    }

    @PostMapping("/test")
    fun test(@RequestBody body: ClientProcessBody): ResponseEntity<String> {
        val image = umlDiagramGenerationService.test(body.text, body.diagramType, body.generationMethod)

        return ResponseEntity(image, HttpStatus.CREATED)
    }

    @GetMapping("/diagrams")
    fun getDiagrams(): ResponseEntity<List<String>> {
        val types = umlDiagramGenerationService.getAvailableDiagramTypes()
        return ResponseEntity(types, HttpStatus.OK)
    }

    @GetMapping("/methods")
    fun getMethods(): ResponseEntity<List<String>> {
        val methods = umlDiagramGenerationService.getAvailableAi()

        return ResponseEntity(methods, HttpStatus.OK)
    }
}