package com.efex.context.students.application.commands

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate

@Introspected
data class UpdateStudentCommand(
    val firstName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: LocalDate? = null,
    val grade: Int? = null,
    val phone: String? = null,
    val email: String? = null,
)
