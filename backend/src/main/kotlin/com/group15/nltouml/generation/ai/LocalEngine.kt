package com.group15.nltouml.generation.ai

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.service.PromptFileService
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service("generator_local")
class LocalEngine(
    @Value("\${llms.local.enabled}") private val enabled: Boolean,
    @Value("\${llms.local.base-url}") private val baseUrl: String,
    @Value("\${llms.local.endpoint}") private val endpoint: String,
    @Value("\${llms.local.api-key}") private val apiKey: String,
    @Value("\${llms.local.model-name}") private val modelName: String,
    @Value("\${llms.local.prompt-file}") private val promptFileName: String,
    @Autowired private val promptFileService: PromptFileService,
): AiEngine {
    private val logger = LoggerFactory.getLogger(LocalEngine::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("$baseUrl$endpoint")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String {
        if (!enabled) return "llm not enabled"

        val objectMapper = jacksonObjectMapper()
        val prompt = promptFileService.getNlToUMLPrompt(input, diagramType, syntax, promptFileName)

        val requestBody = mapOf(
            "prompt" to prompt,
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

    override suspend fun ping(): Boolean {
        if (!enabled) return false

        val requestBody = mapOf(
            "prompt" to "ping",
            "temperature" to 0.0,
            "max_tokens" to 1
        )

        return try {
            val res = webClient.post()
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