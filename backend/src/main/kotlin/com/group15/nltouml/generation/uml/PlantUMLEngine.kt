package com.group15.nltouml.generation.uml

import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.util.titlecase
import net.sourceforge.plantuml.SourceStringReader
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*

@Service("uml_plantuml")
class PlantUMLEngine: UMLEngine {
    val themeMap: Map<Int, String> = listOf(
        "default", "amiga", "aws-orange", "black-knight", "bluegray", "blueprint", "carbon-gray",
        "cerulean-outline", "cerulean", "cloudscape-design", "crt-amber", "crt-green",
        "cyborg-outline", "cyborg", "hacker", "lightgray", "mars", "materia-outline",
        "materia", "metal", "mimeograph", "minty", "mono", "none", "plain",
        "reddress-darkblue", "reddress-darkgreen", "reddress-darkorange", "reddress-darkred",
        "reddress-lightblue", "reddress-lightgreen", "reddress-lightorange", "reddress-lightred",
        "sandstone", "silver", "sketchy-outline", "sketchy", "spacelab-white", "spacelab",
        "Sunlust", "superhero-outline", "superhero", "toy", "united", "vibrant"
    ).withIndex().associate { it.index to it.value }

    override fun textToDiagram(input: String, themeId: Int): ByteArray {
        val inputString = addThemeToInput(input, themeId)

        val reader = SourceStringReader(inputString)
        val outputStream = ByteArrayOutputStream()
        val desc = reader.outputImage(outputStream)

        println("output desc: $desc")

        if (desc == null || desc.description?.contains("(Error)") == true) {
            throw IllegalArgumentException("PlantUML syntax error detected.")
        }

        return outputStream.toByteArray()
    }

    private fun addThemeToInput(input: String, themeId: Int): String {
        if (!input.contains("@enduml") || !input.contains("@startuml")) {
            throw IllegalArgumentException("PlantUML syntax error detected.")
        }
        val theme = themeMap[themeId]

        if (themeId == 0 || theme == null) {
            // no theme
            return input
        }

        // add "!theme name" after @startuml
        return input.replaceFirst("@startuml", "@startuml\n!theme $theme")
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

    override fun getAvailableDiagramThemes(): Map<Int, String> {
        // for now hardcode
        return themeMap.mapValues { formatThemeName(it.value) }
    }

    private fun formatThemeName(name: String): String {
        return titlecase(name.replace("-", " "))
    }

    override fun getAvailableDiagramFileTypes(): List<String> {
        return listOf("png", "svg")
    }
}