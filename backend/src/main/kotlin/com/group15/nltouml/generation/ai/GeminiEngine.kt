package com.group15.nltouml.generation.ai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.group15.nltouml.model.*
import com.group15.nltouml.service.PromptFileService
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service("generator_gemini")
class GeminiEngine(
    @Value("\${llms.gemini.enabled}") private val enabled: Boolean,
    @Value("\${llms.gemini.base-url}") private val baseUrl: String,
    @Value("\${llms.gemini.endpoint}") private val endpoint: String,
    @Value("\${llms.gemini.api-key}") private val apiKey: String,
    @Value("\${llms.gemini.model-name}") private val modelName: String,
    @Value("\${llms.gemini.prompt-file}") private val promptFileName: String,
    @Autowired private val promptFileService: PromptFileService,
): AiEngine {
    private val logger = LoggerFactory.getLogger(GeminiEngine::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("$baseUrl$endpoint")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String {
        if (!enabled) return "llm not enabled"

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
        if (!enabled) return false

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

