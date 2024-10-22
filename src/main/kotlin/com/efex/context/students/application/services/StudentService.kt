package com.efex.context.students.application.services

import com.efex.context.students.application.commands.CreateStudentCommand
import com.efex.context.students.domain.entities.Student
import com.efex.context.students.domain.repositories.StudentRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class StudentService(
    @Inject private val studentRepository: StudentRepository,
    @Inject private val studentFactory: StudentFactory,
) {

    fun createStudent(createStudentCommand: CreateStudentCommand): Student {
        val student = studentFactory.buildStudent(command = createStudentCommand)
        return studentRepository.create(student)
    }

    fun getAllStudents(): List<Student> {
        return studentRepository.findAll()
    }

    fun getStudentById(id: String): Student? {
        return studentRepository.findById(id)
    }

    fun updateStudent(id: Long, student: Student): Student? {
        return studentRepository.update(id, student)
    }
}
