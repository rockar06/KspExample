package com.example.ksp.processor

import com.example.ksp.annotations.AutoBuilder
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate

class AutoBuilderSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols: Sequence<KSClassDeclaration> = resolver
            .getSymbolsWithAnnotation(AutoBuilder::class.java.name)
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        symbols.forEach { symbol ->
            if (!symbol.modifiers.contains(Modifier.DATA)) {
                logger.error("AutoBuilder annotation must be used in a data class", symbol)
                return emptyList()
            }

            symbol.accept(AutoBuilderVisitor(codeGenerator, logger, options), Unit)
        }

        return symbols.filterNot { it.validate() }.toList()
    }
}