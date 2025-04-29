package com.group15.nltouml.generation.ai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.group15.nltouml.model.AiResponseJson
import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.model.OpenAIResponse
import com.group15.nltouml.service.PromptFileService
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service("generator_deepseek")
class DeepSeekEngine(
    @Value("\${llms.deepseek.enabled}") private val enabled: Boolean,
    @Value("\${llms.deepseek.base-url}") private val baseUrl: String,
    @Value("\${llms.deepseek.endpoint}") private val endpoint: String,
    @Value("\${llms.deepseek.api-key}") private val apiKey: String,
    @Value("\${llms.deepseek.model-name}") private val modelName: String,
    @Value("\${llms.deepseek.prompt-file}") private val promptFileName: String,
    @Autowired private val promptFileService: PromptFileService,
): AiEngine {
    private val logger = LoggerFactory.getLogger(DeepSeekEngine::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("$baseUrl$endpoint")
        .defaultHeader("Authorization", "Bearer $apiKey")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String {
        if (!enabled) return "llm not enabled"

        val prompt = promptFileService.getNlToUMLPrompt(input, diagramType, syntax, promptFileName)

        val requestBody = mapOf(
            "model" to modelName,
            "messages" to listOf(
                mapOf("role" to "user", "content" to prompt)
            ),
            "temperature" to 0.0
        )

        return try {
            webClient.post()
                .uri { uriBuilder ->
                    uriBuilder.queryParam("key", apiKey).build()
                }
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAIResponse::class.java)
                .map { response ->
                    var contentJson = response.choices.firstOrNull()?.message?.content ?: "[]"
                    logger.debug("Raw DeepSeek Response: {}", contentJson)  // Add this for debugging

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

        val requestBody = mapOf(
            "model" to modelName,
            "messages" to listOf(
                mapOf("role" to "system", "content" to "ping")
            ),
            "temperature" to 0.0,
            "max_tokens" to 1
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