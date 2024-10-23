package com.efex.infrastructure.dynamo

import com.efex.config.BaseIntegrationTest
import com.efex.context.students.domain.entities.Student
import com.efex.context.students.domain.repositories.StudentRepository
import com.efex.context.students.infrastructure.dynamo.mappers.StudentEntityMapper
import com.efex.context.students.infrastructure.dynamo.repositories.DynamoStudentsRepository
import com.efex.factories.StudentPersistentFactory
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.LocalDate

class DynamoStudentsRepositoryTest : BaseIntegrationTest() {
    @Value("\${aws.dynamo.tables.students.name}")
    private lateinit var tableName: String

    @Inject
    private lateinit var dynamoDbClient: DynamoDbClient

    @Inject
    private lateinit var studentPersistentFactory: StudentPersistentFactory

    @Inject
    private lateinit var studentMapper: StudentEntityMapper

    private val repository: StudentRepository by lazy {
        DynamoStudentsRepository(
            tableName = tableName,
            client = dynamoDbClient,
            studentMapper = studentMapper,
        )
    }

    @Test
    fun `given a student should save and recover it as the same student`() {
        val expected =
            Student(
                id = 6L,
                firstName = "Eric",
                lastName = "Gomez",
                dateOfBirth = LocalDate.parse("2010-01-01"),
                grade = 4,
                phone = "+11114444",
                email = "eric@email.com",
            )

        repository.create(expected)

        val actual = repository.findById(id = 6L)

        assertNotNull(actual, "persisted student could not be null")
        assertEquals(expected, actual)
    }

    @Test
    fun `it should return null when student is not found`() {
        val student = repository.findById(999L)

        assertNull(student, "Student with ID 999 should not be found")
    }

    @Test
    fun `it should retrieve all students`() {
        val students =
            listOf(
                Student(1L, "Capitan", "America", LocalDate.parse("2010-01-01"), 5, "+111111111", "john@email.com"),
                Student(2L, "Wonder", "Wonder", LocalDate.parse("2011-02-02"), 6, "+222222222", "jane@email.com"),
            )
        students.forEach { repository.create(it) }

        val allStudents = repository.findAll()

        assertEquals(2, allStudents.size, "There should be exactly 2 students")
        assertTrue(allStudents.any { it.firstName == "Capitan" })
        assertTrue(allStudents.any { it.firstName == "Wonder" })
    }

    @Test
    fun `it should return an empty list when no students are present`() {
        val students = repository.findAll()

        assertTrue(students.isEmpty(), "The student list should be empty")
    }

    @Test
    fun `it should update an existing student and return the updated student`() {
        val existingStudent =
            studentPersistentFactory.create(
                id = 1,
                firstName = "Harry",
                lastName = "Potter",
            )

        val updatedStudent =
            existingStudent.copy(
                firstName = "Scarlett",
                lastName = "Evans",
            )
        val result = repository.update(existingStudent.id!!, updatedStudent)

        assertNotNull(result, "The student list should not be empty")
        assertEquals(updatedStudent.firstName, result!!.firstName)
        assertEquals(updatedStudent.lastName, result.lastName)
    }

    @Test
    fun `it should create the student when try to update directly in database`() {
        val nonExistentId = 999L
        val studentToUpdate =
            Student(
                id = nonExistentId,
                firstName = "Capitan",
                lastName = "America",
                dateOfBirth = LocalDate.parse("2010-01-01"),
                grade = 5,
                phone = "+222222222",
                email = "john.doe@email.com",
            )

        val result = repository.update(nonExistentId, studentToUpdate)

        assertEquals(studentToUpdate, result)
    }
}
