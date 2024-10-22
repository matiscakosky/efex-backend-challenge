package com.efex.context.students.application.services

import com.efex.context.students.application.commands.CreateStudentCommand
import com.efex.context.students.application.commands.UpdateStudentCommand
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

    fun getStudentById(id: Long): Student? {
        return studentRepository.findById(id)
    }

    fun updateStudent(id: Long, command: UpdateStudentCommand): Student? {
        val student = getStudentById(id) ?: return null
        val updatedStudent = updateStudentProperties(student, command)
        return studentRepository.update(id, updatedStudent)
    }

    private fun updateStudentProperties(student: Student, command: UpdateStudentCommand) = student.copy(
        firstName = command.firstName ?: student.firstName,
        lastName = command.lastName ?: student.lastName,
        dateOfBirth = command.dateOfBirth ?: student.dateOfBirth,
        grade = command.grade ?: student.grade,
        phone = command.phone ?: student.phone,
        email = command.email ?: student.email
    )
}
