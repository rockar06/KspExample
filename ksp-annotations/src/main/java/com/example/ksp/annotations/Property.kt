package com.example.ksp.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class Property(val name: String)
