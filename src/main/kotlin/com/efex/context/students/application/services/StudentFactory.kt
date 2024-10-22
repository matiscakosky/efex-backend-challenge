package com.efex.context.students.application.services

import com.efex.context.students.application.commands.CreateStudentCommand
import com.efex.context.students.domain.entities.Student
import com.efex.context.students.domain.repositories.StudentRepository
import com.efex.context.students.infrastructure.dynamo.repositories.DynamoIncrementalRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class StudentFactory(
    @Inject private val studentRepository: StudentRepository,
    @Inject private val incrementalRepository: DynamoIncrementalRepository,
) {

    fun buildStudent(command: CreateStudentCommand): Student{
        return Student(
            id = incrementalRepository.getNextId(),
            firstName = command.firstName!!,
            lastName = command.lastName!!,
            dateOfBirth = command.dateOfBirth!!,
            email = command.email!!,
            grade = command.grade!!,
            phone = command.phone!!
        )
    }

}
