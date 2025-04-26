package com.group15.nltouml.generation.ai

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AiEngineFactory(
    @Autowired private val engines: Map<String, AiEngine>
) {
    fun createAiEngine(engine: String): AiEngine {
        val tag = "generator_${engine.lowercase().trim()}"
        return engines[tag] ?: throw Exception("Ai engine does not exist")
    }

    suspend fun getAvailableEngines(): List<String> = coroutineScope {
        engines.map { (name, engine) ->
            async {
                try {
                    if (engine.ping()) {
                        name.removePrefix("generator_")
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }
}