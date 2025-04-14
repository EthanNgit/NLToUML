package com.group15.nltouml.generation.ai

import com.group15.nltouml.model.AiEngineModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AiEngineFactory(
    @Autowired private val engines: Map<String, AiEngine>
) {
    fun createAiEngine(engine: AiEngineModel): AiEngine {
        val tag = "generator_${engine.name.lowercase()}"
        return engines[tag] ?: throw Exception("Ai engine does not exist")
    }
}