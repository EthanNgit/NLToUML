package com.group15.nltouml.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

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
    val text: String
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