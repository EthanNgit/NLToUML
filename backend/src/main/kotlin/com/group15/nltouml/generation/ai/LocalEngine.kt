package com.group15.nltouml.generation.ai

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.group15.nltouml.model.DiagramType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service("generator_local")
class LocalEngine: AiEngine {
    private val logger = LoggerFactory.getLogger(LocalEngine::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("http://csai01:8000/generate")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String {
        val objectMapper = jacksonObjectMapper()

        val requestBody = mapOf(
            "prompt" to """
                 You only output JSON. Don't include any explanations or introductions.
                 Based on the user requirements "$input", generate the UML syntax for the "$diagramType" diagram.
                 Where an example of its syntax looks like "$syntax". Return in json format
                 { 
                    "uml": "..." 
                 }
            """.trimIndent(),
            "temperature" to 0.0
        )

        return try {
            val response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String::class.java)
                .block()

            val jsonNode = objectMapper.readTree(response)
            jsonNode["text"]?.asText() ?: "No response text found"

        } catch (e: Exception) {
            logger.error("Error during AI call", e)
            "Error: ${e.message}"
        }
    }
}