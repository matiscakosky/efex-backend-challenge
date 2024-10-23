package com.efex.context.students.application.services

import com.efex.context.students.application.commands.CreateStudentCommand
import com.efex.context.students.application.commands.UpdateStudentCommand
import com.efex.context.students.domain.entities.Student
import com.efex.context.students.domain.repositories.StudentRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import la.pomelo.testing.config.UnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StudentServiceTest : UnitTest() {
    @MockK
    lateinit var studentRepository: StudentRepository

    @MockK
    lateinit var studentFactory: StudentFactory

    private lateinit var studentService: StudentService

    @BeforeEach
    fun setUp() {
        studentService = StudentService(studentRepository, studentFactory)
    }

    @Test
    fun `test create student`() {
        val command =
            CreateStudentCommand(
                firstName = "Scarlett",
                lastName = "Evans",
                dateOfBirth = LocalDate.parse("2010-05-01"),
                grade = 5,
                phone = "+111111111",
                email = "scarlet@email.com",
            )
        val student =
            Student(
                id = 1L,
                firstName = "Scarlett",
                lastName = "Evans",
                dateOfBirth = LocalDate.parse("2010-05-01"),
                grade = 5,
                phone = "+111111111",
                email = "scarlet@email.com",
            )

        every { studentRepository.create(student) } returns student
        every { studentFactory.buildStudent(command) } returns student

        val result = studentService.createStudent(command)

        assertNotNull(result)
        assertEquals("Scarlett", result.firstName)
        assertEquals("Evans", result.lastName)
        verify { studentRepository.create(student) }
    }

    @Test
    fun `test get all students`() {
        val students =
            listOf(
                Student(1L, "Scarlett", "Evans", LocalDate.parse("2010-05-01"), 5, "+111111111", "scarlet@email.com"),
                Student(2L, "Matias", "Martin", LocalDate.parse("2011-06-01"), 6, "+222222222", "matias@email.com"),
            )

        every { studentRepository.findAll() } returns students
        val result = studentService.getAllStudents()

        assertEquals(2, result.size)
        assertEquals("Scarlett", result[0].firstName)
        assertEquals("Matias", result[1].firstName)
        verify { studentRepository.findAll() }
    }

    @Test
    fun `test get student by id`() {
        val studentId = 1L
        val student =
            Student(
                studentId,
                "Scarlett",
                "Evans",
                LocalDate.parse("2010-05-01"),
                5,
                "+111111111",
                "scarlet@email.com",
            )

        every { studentRepository.findById(studentId) } returns student
        val result = studentService.getStudentById(studentId)

        assertNotNull(result)
        assertEquals("Scarlett", result?.firstName)
        verify { studentRepository.findById(studentId) }
    }

    @Test
    fun `test update student`() {
        val studentId = 1L
        val existingStudent =
            Student(
                studentId,
                "Scarlett",
                "Evans",
                LocalDate.parse("2010-05-01"),
                5,
                "+111111111",
                "scarlet@email.com",
            )
        val updateCommand = UpdateStudentCommand(firstName = "Scarlett Updated")
        val updatedStudent = existingStudent.copy(firstName = "Scarlett Updated")

        every { studentRepository.findById(studentId) } returns existingStudent
        every { studentRepository.update(studentId, updatedStudent) } returns updatedStudent

        val result = studentService.updateStudent(studentId, updateCommand)

        assertNotNull(result)
        assertEquals("Scarlett Updated", result?.firstName)
        verify { studentRepository.findById(studentId) }
        verify { studentRepository.update(studentId, updatedStudent) }
    }

    @Test
    fun `test update student not found`() {
        val studentId = 999L
        val updateCommand = UpdateStudentCommand(firstName = "Scarlett Updated")

        every { studentRepository.findById(studentId) } returns null

        val result = studentService.updateStudent(studentId, updateCommand)

        assertNull(result)
        verify { studentRepository.findById(studentId) }
        verify(exactly = 0) { studentRepository.update(any(), any()) }
    }
}
