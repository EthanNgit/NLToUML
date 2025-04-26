package com.group15.nltouml.generation.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.group15.nltouml.model.AiResponseJson
import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.service.PromptFileService
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
    val temperature: Double,
    val maxOutputTokens: Int = 1000
)

@Service("generator_gemini")
class GeminiEngine(
    @Value("\${gemini.api-key}") private val apiKey: String,
    @Value("\${gemini.model-name}") private val modelName: String,
    @Value("\${gemini.prompt-file}") private val promptFileName: String,
    @Autowired private val promptFileService: PromptFileService,
): AiEngine {
    private val logger = LoggerFactory.getLogger(GeminiEngine::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String {
        val prompt = promptFileService.getNlToUMLPrompt(input, diagramType, syntax, promptFileName)

        val requestBody = GeminiRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.0)
        )

        return try {
            webClient.post()
                .uri { uriBuilder ->
                    uriBuilder.queryParam("key", apiKey).build()
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

    override suspend fun ping(): Boolean {
        val requestBody = GeminiRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(
                        PartItem(text = "ping")
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.0, maxOutputTokens = 1)
        )

        return try {
            val res = webClient.post()
                .uri { uriBuilder ->
                    uriBuilder.queryParam("key", apiKey).build()
                }
                .bodyValue(requestBody)
                .retrieve()
                .toBodilessEntity()
                .awaitSingle()

            res.statusCode.is2xxSuccessful
        } catch (e: Exception) {
            logger.error("Error during AI call", e)
            false
        }
    }
}

