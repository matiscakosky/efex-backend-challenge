package com.efex.context.students.application.commands

import io.micronaut.core.annotation.Introspected
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

@Introspected
data class CreateStudentCommand(
    @field:NotNull(message = "first name must be specified")
    val firstName: String? = null,
    @field:NotNull(message = "last name must be specified")
    val lastName: String? = null,
    @field:NotNull(message = "birth date must be specified")
    val dateOfBirth: LocalDate? = null,
    @field:NotNull(message = "grade must be specified")
    val grade: Int? = null,
    @field:NotNull(message = "phone must be specified")
    val phone: String? = null,
    @field:NotNull(message = "email must be specified")
    val email: String? = null,
)
