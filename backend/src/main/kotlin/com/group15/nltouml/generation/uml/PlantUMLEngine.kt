package com.group15.nltouml.generation.uml

import com.group15.nltouml.model.DiagramType
import net.sourceforge.plantuml.SourceStringReader
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*

@Service("uml_plantuml")
class PlantUMLEngine: UMLEngine {
    override fun textToDiagram(input: String): ByteArray {
        val reader = SourceStringReader(input)
        val outputStream = ByteArrayOutputStream()
        reader.outputImage(outputStream)
        return outputStream.toByteArray()
    }

    override fun getSyntaxForDiagramType(type: DiagramType): String {
        // If diagram generation fails, specifically set the syntax requirements...
        return when (type) {
            DiagramType.Class-> {
                "PlantUML"
            }
            DiagramType.UseCase -> {
                "PlantUML"
            }
            DiagramType.Component -> {
                "PlantUML"
            }
            DiagramType.Sequence -> {
                "PlantUML"
            }
            DiagramType.Activity -> {
                "PlantUML"
            }
            else -> {
                return ""
            }
        }
    }

    override fun getAvailableDiagramTypes(): List<String> {
        // for now hardcode
        return listOf("Class", "Use case", "Component", "Sequence", "Activity")
    }
}