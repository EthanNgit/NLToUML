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


data class OpenAIResponse(
    val choices: List<Choice>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Choice(
    val message: Message
)

data class Message(
    val content: String // as json
)


@Service("generator_openai")
class OpenAiEngine(
    @Value("\${openai.api.key}") private val openAiApiKey: String,
    @Value("\${openai.model.name}") private val openAiModelName: String
): AiEngine {
    private val logger = LoggerFactory.getLogger(GeminiEngine::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("https://api.openai.com/v1/chat/completions")
        .defaultHeader("Authorization", "Bearer $openAiApiKey")
        .defaultHeader("Content-Type", "application/json")
        .build()

    override fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String {
        val requestBody = mapOf(
            "model" to openAiModelName,
            "messages" to listOf(
                mapOf("role" to "system", "content" to "You only output JSON. Dont include any explanations or introductions."),
                mapOf("role" to "user", "content" to """
                     You only output JSON. Don't include any explanations or introductions.
                     Based on the user requirements "$input", generate the UML syntax for the "$diagramType" diagram.
                     Where an example of its syntax looks like "$syntax". Return in json format
                     { 
                        "uml": "..." 
                     }
                """.trimIndent())
            ),
            "temperature" to 0.0
        )

        return try {
            webClient.post()
                .uri { uriBuilder ->
                    uriBuilder.queryParam("key", openAiApiKey).build()
                }
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAIResponse::class.java)
                .map { response ->
                    var contentJson = response.choices.firstOrNull()?.message?.content ?: "[]"
                    logger.debug("Raw OpenAi Response: {}", contentJson)  // Add this for debugging

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