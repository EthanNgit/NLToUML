package com.group15.nltouml.generation.ai

import com.group15.nltouml.model.DiagramType

interface AiEngine {
    fun convertTextInputToUMLSyntax(input: String, diagramType: DiagramType, syntax: String): String
}