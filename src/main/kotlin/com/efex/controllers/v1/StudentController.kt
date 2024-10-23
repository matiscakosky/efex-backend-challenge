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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Validated
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/students")
class StudentController(
    @Inject val studentService: StudentService,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(StudentController::class.java)
    }

    @Post
    fun create(
        @Valid @Body
        command: CreateStudentCommand,
    ): HttpResponse<Student> {
        logger.info("about to create a new student")

        val createdStudent = studentService.createStudent(command)
        return HttpResponse.created(createdStudent)
    }

    @Get
    fun list(): List<Student> = studentService.getAllStudents()

    @Get("/{id}")
    fun get(
        @PathVariable id: Long,
    ): HttpResponse<Student> {
        logger.info("about to get student $id")

        val student = studentService.getStudentById(id)

        if (student == null) {
            logger.info("student $id not found")
            return HttpResponse.notFound()
        }

        return HttpResponse.ok(student)
    }

    @Patch("/{id}")
    fun update(
        @PathVariable id: Long,
        @Body command: UpdateStudentCommand,
    ): HttpResponse<Student> {
        logger.info("about to update student $id")

        val updatedStudent = studentService.updateStudent(id, command)

        if (updatedStudent == null) {
            logger.info("student $id not found, there is no update")
            return HttpResponse.notFound()
        }

        return HttpResponse.ok(updatedStudent)
    }
}
