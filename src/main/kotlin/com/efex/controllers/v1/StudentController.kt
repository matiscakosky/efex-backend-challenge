package com.efex.controllers.v1

import com.efex.context.students.application.commands.CreateStudentCommand
import com.efex.context.students.application.commands.UpdateStudentCommand
import com.efex.context.students.application.services.StudentService
import com.efex.context.students.domain.entities.Student
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.validation.Valid

@Validated
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/students")
class StudentController(
    @Inject val studentService: StudentService,
) {
    @Post
    fun create(
        @Valid @Body
        command: CreateStudentCommand,
    ): HttpResponse<Student> {
        val createdStudent = studentService.createStudent(command)
        return HttpResponse.created(createdStudent)
    }

    @Get
    fun list(): List<Student> = studentService.getAllStudents()

    @Get("/{id}")
    fun get(
        @PathVariable id: Long,
    ): HttpResponse<Student> {
        val student = studentService.getStudentById(id)
        return if (student != null) HttpResponse.ok(student) else HttpResponse.notFound()
    }

    @Patch("/{id}")
    fun update(
        @PathVariable id: Long,
        @Body command: UpdateStudentCommand,
    ): HttpResponse<Student> {
        val updatedStudent = studentService.updateStudent(id, command)
        return if (updatedStudent != null) HttpResponse.ok(updatedStudent) else HttpResponse.notFound()
    }
}
