package com.efex.controllers.v1


import com.efex.context.students.application.commands.CreateStudentCommand
import com.efex.context.students.domain.entities.Student
import com.efex.context.students.application.services.StudentService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import jakarta.inject.Inject

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/students")
class StudentController(@Inject val studentService: StudentService) {

    @Post
    fun create(@Body student: CreateStudentCommand): HttpResponse<Student> {
        val createdStudent = studentService.createStudent(student)
        return HttpResponse.created(createdStudent)
    }

    @Get
    fun list(): List<Student> {
        return studentService.getAllStudents()
    }

    @Get("/{id}")
    fun get(@PathVariable id: Long): HttpResponse<Student> {
        val student = studentService.getStudentById(id)
        return if (student != null) HttpResponse.ok(student) else HttpResponse.notFound()
    }

    @Patch("/{id}")
    fun update(@PathVariable id: Long, @Body student: Student): HttpResponse<Student> {
        val updatedStudent = studentService.updateStudent(id, student)
        return if (updatedStudent != null) HttpResponse.ok(updatedStudent) else HttpResponse.notFound()
    }
}
