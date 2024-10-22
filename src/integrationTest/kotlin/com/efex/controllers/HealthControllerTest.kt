package com.efex.controllers

import com.efex.config.BaseIntegrationTest
import com.efex.context.students.application.commands.CreateStudentCommand
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

@MicronautTest
class HealthControllerTest : BaseIntegrationTest() {

    @Test
    fun `it should return status 200 when the services are up`() {
        val (response, _) = get<Any>("/health")

        Assertions.assertEquals(HttpStatus.OK, response.status)
    }
}
