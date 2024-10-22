package com.efex.context.students.domain.entities

import java.time.LocalDate

data class Student(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val grade: Int,
    val phone: String,
    val email: String
)
