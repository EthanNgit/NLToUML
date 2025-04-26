package com.group15.nltouml.service

import com.group15.nltouml.generation.ai.AiEngineFactory
import com.group15.nltouml.generation.uml.PlantUMLEngine
import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.util.titlecase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UMLDiagramGenerationService(
    @Autowired private val aiEngineFactory: AiEngineFactory,
    @Autowired private val plantUMLEngine: PlantUMLEngine, //tmp
) {
    private val logger = LoggerFactory.getLogger(UMLDiagramGenerationService::class.java)

    fun process(text: String, diagramType: DiagramType, generationMethod: String): ByteArray {
        val aiEngine = aiEngineFactory.createAiEngine(generationMethod)

        val uml = aiEngine.convertTextInputToUMLSyntax(text, diagramType, plantUMLEngine.getSyntaxForDiagramType(diagramType))

        val diagram = plantUMLEngine.textToDiagram(uml, 23)

        return diagram
    }

    fun test(text: String, diagramType: DiagramType, generationMethod: String): String {
        val aiEngine = aiEngineFactory.createAiEngine(generationMethod)

        return aiEngine.convertTextInputToUMLSyntax(text, diagramType, "PlantUML")
    }

    suspend fun getAvailableAi(): List<String> {
        return aiEngineFactory.getAvailableEngines().map { titlecase(it) }
    }

    fun getAvailableDiagramTypes(): List<String> {
        // since we only have plant uml, just hard code for now
        return enumValues<DiagramType>().map { it.name }
    }
}