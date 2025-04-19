package com.group15.nltouml.generation.uml

import com.group15.nltouml.model.DiagramType

interface UMLEngine {
    fun textToDiagram(input: String, themeId: Int): ByteArray
    fun getSyntaxForDiagramType(type: DiagramType): String
    fun getAvailableDiagramTypes(): List<String>
    fun getAvailableDiagramThemes(): Map<Int, String>
    fun getAvailableDiagramFileTypes(): List<String>
}