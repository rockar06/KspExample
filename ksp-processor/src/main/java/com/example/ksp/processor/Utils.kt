package com.example.ksp.processor

import com.example.ksp.annotations.Property
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.Locale

fun KSClassDeclaration.getProperties(): List<PropertySpec> {
    return getAllProperties().map { property ->

        PropertySpec.builder(
            property.getPropertyName(),
            property.getPropertyType(),
            KModifier.PRIVATE
        )
            .mutable()
            .initializer(property.getInitializer())
            .build()
    }.toList()
}

fun KSPropertyDeclaration.getPropertyName(): String {
    return simpleName.asString()
}

fun KSPropertyDeclaration.getPropertyType(): TypeName {
    return type.toTypeName()
}

fun KSPropertyDeclaration.getInitializer(): String {
    return if (getPropertyType().isNullable) "null" else {
        when (getPropertyType().toString()) {
            "kotlin.String" -> "\"\""
            "kotlin.Int" -> "0"
            "kotlin.Bool" -> "false"
            else -> "null"
        }
    }
}

fun KSClassDeclaration.getMethods(): List<FunSpec> {
    return getAllProperties().map { property ->

        val customPropertyName = property.getCustomPropertyName()

        FunSpec.builder(property.getMethodName())
            .addParameter(customPropertyName, property.getPropertyType())
            .addStatement("this.${property.getPropertyName()} = $customPropertyName")
            .returns(ClassName(getPackageName(), getBuilderName()))
            .addStatement("return this")
            .build()
    }.toList()
}

fun KSPropertyDeclaration.getCustomPropertyName(): String {
    return getAnnotationIfExist()?.let {
        it.arguments.firstOrNull { argument -> argument.name?.asString() == "name" }?.value as String
    } ?: getPropertyName()
}

fun KSPropertyDeclaration.getMethodName(): String {
    return "set${getCustomPropertyName().capitalizeFirst()}"
}

fun String.capitalizeFirst(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}


fun KSClassDeclaration.getPackageName(): String {
    return packageName.asString()
}

fun KSClassDeclaration.getBuilderName(): String {
    return "${simpleName.asString()}Builder"
}

fun KSPropertyDeclaration.getAnnotationIfExist(): KSAnnotation? {
    return annotations.firstOrNull { it.shortName.asString() == Property::class.java.simpleName }
}

fun KSClassDeclaration.getConstructorArguments(): String {
    return getAllProperties().map {
        "${it.getPropertyName()} = ${it.getPropertyName()}"
    }.joinToString(", ")
}

fun KSClassDeclaration.getBuildMethod(): FunSpec {
    return FunSpec.builder("build")
        .returns(ClassName(getPackageName(), simpleName.asString()))
        .addStatement(
            "return ${simpleName.asString()}(${getConstructorArguments()})"
        )
        .build()
}