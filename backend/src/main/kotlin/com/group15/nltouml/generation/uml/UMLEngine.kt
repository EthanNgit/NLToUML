package com.group15.nltouml.generation.uml

import com.group15.nltouml.model.DiagramType

interface UMLEngine {
    fun textToDiagram(input: String): ByteArray
    fun getSyntaxForDiagramType(type: DiagramType): String
    fun getAvailableDiagramTypes(): List<String>
}