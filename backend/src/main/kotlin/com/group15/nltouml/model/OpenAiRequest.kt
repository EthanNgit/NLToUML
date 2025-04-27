package com.group15.nltouml.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class OpenAIResponse(
    val choices: List<Choice>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Choice(
    val message: Message
)

data class Message(
    val content: String
)
