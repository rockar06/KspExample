package com.example.kspexample

import com.example.ksp.annotations.AutoBuilder
import com.example.ksp.annotations.Property

@AutoBuilder
data class Person(
    @Property("testValue") val name: String,
    val lastName: String,
    @Property("age") val userAge: Int,
    val address: String?
)