package com.group15.nltouml.service

import com.group15.nltouml.generation.ai.AiEngine
import com.group15.nltouml.generation.ai.AiEngineFactory
import com.group15.nltouml.generation.ai.GeminiEngine
import com.group15.nltouml.generation.uml.PlantUMLEngine
import com.group15.nltouml.generation.uml.UMLEngine
import com.group15.nltouml.model.AiEngineModel
import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.model.MethodBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import kotlin.math.log

@Service
class UMLDiagramGenerationService(
    @Autowired private val aiEngineFactory: AiEngineFactory,
    @Autowired private val plantUMLEngine: PlantUMLEngine, //tmp
) {
    private val logger = LoggerFactory.getLogger(UMLDiagramGenerationService::class.java)

    fun process(text: String, diagramType: DiagramType, generationMethod: AiEngineModel): ByteArray {
        val aiEngine = aiEngineFactory.createAiEngine(generationMethod)

        val uml = aiEngine.convertTextInputToUMLSyntax(text, diagramType, plantUMLEngine.getSyntaxForDiagramType(diagramType))
        logger.debug("Generated diagram syntex for $diagramType diagram with result $uml")

        val diagram = plantUMLEngine.textToDiagram(uml, 3)

        return diagram
    }

    fun test(text: String, diagramType: DiagramType, generationMethod: AiEngineModel): String {
        val aiEngine = aiEngineFactory.createAiEngine(generationMethod)

        return aiEngine.convertTextInputToUMLSyntax(text, diagramType, "PlantUML")
    }

    fun getAvailableAi(): List<String> {
        // check which ai are connected and able to run
        // simple check for efficiency, does the key exist...

        // get images and return directly, even though we should use
        // url for client to get them, this works easiest for now...

        return  enumValues<AiEngineModel>().map { it.name }
    }

    fun getAvailableDiagramTypes(): List<String> {
        // since we only have plant uml, just hard code for now

        return enumValues<DiagramType>().map { it.name }
    }
}