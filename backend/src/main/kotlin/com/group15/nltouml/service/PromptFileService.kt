package com.group15.nltouml.service

import com.group15.nltouml.model.DiagramType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

@Service
class PromptFileService(
    @Value("\${prompts.path}") private val promptBaseLocation: String
) {
    fun getNlToUMLPrompt(input: String, diagramType: DiagramType, syntax: String, promptFileName: String): String {
        val fullPath = Paths.get(promptBaseLocation, promptFileName)
        val prompt = Files.readString(fullPath)

        return prompt
            .replace("\$input", input)
            .replace("\$diagramType", diagramType.name)
            .replace("\$syntax", syntax)
    }
}