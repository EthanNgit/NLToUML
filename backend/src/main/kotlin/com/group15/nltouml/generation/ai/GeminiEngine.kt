package com.group15.nltouml.generation.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.group15.nltouml.model.AiEngineModel
import com.group15.nltouml.model.AiResponseJson
import com.group15.nltouml.model.DiagramType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

// Gemini response structure
data class GeminiResponse(
    val candidates: List<Candidate>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Candidate(
    val content: Content
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String // as json
)

// Gemini request structure
data class GeminiRequest(
    val contents: List<ContentItem>,
    val generationConfig: GenerationConfig
)

data class ContentItem(
    val role: String,
    val parts: List<PartItem>
)

data class PartItem(
    val text: String
)

data class GenerationConfig(
    val temperature: Double
)

@Service("generator_gemini")
class GeminiEngine(
    @Value("\${gemini.api.key}") private val geminiApiKey: String,
    @Value("\${gemini.model.name}") private val geminiModelName: String,
): AiEngine {
    private val logger = LoggerFactory.getLogger(GeminiEngine::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/${geminiModelName}:generateContent")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String {
        val requestBody = GeminiRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(
                        PartItem(
                            text = """
                                 You only output JSON. Don't include any explanations or introductions.
                                 Based on the user requirements "$input", generate the UML syntax for the "$diagramType" diagram.
                                 Where an example of its syntax looks like "$syntax". Return in json format.
                                 Since the result is in json, it is important to make sure the output is properly escaped and parsable
                                 Example "1" -- "1" -> \"1\" -- \"1\"
                                 { 
                                    "uml": "..."
                                 }
                            """.trimIndent()
                        )
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.0)
        )

        return try {
            webClient.post()
                .uri { uriBuilder ->
                    uriBuilder.queryParam("key", geminiApiKey).build()
                }
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse::class.java)
                .map { response ->
                    var contentJson = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "[]"
                    logger.debug("Raw Gemini Response: {}", contentJson)  // Add this for debugging

                    contentJson = contentJson.trim()
                    contentJson = contentJson.removePrefix("```json").trim()
                    contentJson = contentJson.removeSuffix("```").trim()

                    val jsonNode: JsonNode = jacksonObjectMapper().readTree(contentJson)

                    jacksonObjectMapper().convertValue(jsonNode, object : TypeReference<AiResponseJson>() {}).uml
                }
                .block() ?: "No response text found"
        } catch (e: Exception) {
            logger.error("Error during AI call", e)
            "Error: ${e.message}"
        }
    }
}