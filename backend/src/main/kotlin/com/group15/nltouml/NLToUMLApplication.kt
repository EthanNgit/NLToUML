package com.group15.nltouml

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
class NLToUMLApplication

fun main(args: Array<String>) {
	runApplication<NLToUMLApplication>(*args)
}
