package com.efex.controllers.v1

import com.efex.config.BaseIntegrationTest
import com.efex.context.students.application.commands.CreateStudentCommand
import com.efex.context.students.application.commands.UpdateStudentCommand
import com.efex.context.students.domain.entities.Student
import com.efex.factories.StudentPersistentFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@MicronautTest
class StudentControllerTest : BaseIntegrationTest() {
    @Inject
    private lateinit var studentPersistentFactory: StudentPersistentFactory

    @Test
    fun `it should create a student and return status 201 when the request is valid`() {
        val command =
            CreateStudentCommand(
                firstName = "Scarlett",
                lastName = "Evans",
                dateOfBirth = LocalDate.parse("2010-05-01"),
                grade = 5,
                phone = "+111111111",
                email = "scarlet@email.com",
            )

        val (response, student) = post<Student>("/students", objectMapper.writeValueAsString(command))

        assertEquals(HttpStatus.CREATED, response.status)
        assertNotNull(student.id)
        assertEquals(command.firstName, student.firstName)
        assertEquals(command.lastName, student.lastName)
    }

    @Test
    fun `should return 400 when firstName is missing`() {
        val invalidCommand =
            CreateStudentCommand(
                firstName = null,
                lastName = "Evans",
                dateOfBirth = LocalDate.parse("2010-05-01"),
                grade = 5,
                phone = "+111111111",
                email = "scarlet@email.com",
            )

        try {
            post<Student>("/students", objectMapper.writeValueAsString(invalidCommand))

            fail("Expected HttpClientResponseException to be thrown")
        } catch (e: HttpClientResponseException) {
            assertEquals(HttpStatus.BAD_REQUEST, e.status, "Response status should be 400 - Bad Request")

            val responseBody = e.response.body.getOrNull()
            assertNotNull(responseBody, "Response body should not be null")

            assertTrue(
                responseBody.toString().contains("first name must be specified"),
                "Error message should mention the missing first name",
            )
        }
    }

    @Test
    fun `it should recover a student and return status 200 when the student already existes`() {
        val studentId = 5L
        studentPersistentFactory.create(
            id = studentId,
            firstName = "Harry",
            lastName = "Potter",
        )

        val (getResponse, student) = get<Student>("/students/$studentId")

        assertEquals(HttpStatus.OK, getResponse.status, "Expected 200 OK")
        assertEquals(studentId, student.id)
        assertEquals("Harry", student.firstName)
        assertEquals("Potter", student.lastName)
    }

    @Test
    fun `should return 404 when the student does not exist`() {
        val notFundId = 999L

        try {
            get<Student>("/students/$notFundId")
            fail("Expected HttpClientResponseException to be thrown")
        } catch (e: HttpClientResponseException) {
            assertEquals(HttpStatus.NOT_FOUND, e.status, "Expected 404 Not Found")
        }
    }

    @Test
    fun `it should update a student and return status 200 when the student is already registered`() {
        val studentId = 5L

        studentPersistentFactory.create(
            id = studentId,
            firstName = "Harry",
            lastName = "Potter",
        )

        val command =
            UpdateStudentCommand(
                firstName = "Scarlett",
                lastName = "Evans",
            )

        val (response, student) = patch<Student>("/students/$studentId", objectMapper.writeValueAsString(command))

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(studentId, student.id)
        assertEquals(command.firstName, command.firstName)
        assertEquals(command.lastName, command.lastName)
    }

    @Test
    fun `should return 404 when the updating student does not exist`() {
        val notFundId = 999L
        val command =
            UpdateStudentCommand(
                firstName = "Scarlett",
                lastName = "Evans",
            )

        try {
            patch<Student>("/students/$notFundId", objectMapper.writeValueAsString(command))
            fail("Expected HttpClientResponseException to be thrown")
        } catch (e: HttpClientResponseException) {
            assertEquals(HttpStatus.NOT_FOUND, e.status, "Expected 404 Not Found")
        }
    }
}
