package com.example.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class AutoBuilderVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.packageName.asString()
        val builderName = "${classDeclaration.simpleName.asString()}Builder"
        val properties = classDeclaration.getProperties()
        val builderMethods = classDeclaration.getMethods()
        val buildMethod = classDeclaration.getBuildMethod()

        val fileSpec = FileSpec.builder(
            packageName = packageName, fileName = builderName
        )
            .addType(
                TypeSpec.classBuilder(builderName)
                    .addProperties(properties)
                    .addFunctions(builderMethods)
                    .addFunction(buildMethod)
                    .build()
            )
            .build()

        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            aggregating = false
        )
    }
}
