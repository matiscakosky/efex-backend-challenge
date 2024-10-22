package com.efex.controllers

import com.efex.config.BaseIntegrationTest
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class HealthControllerTest : BaseIntegrationTest() {
    @Test
    fun `it should return status 200 when the services are up`() {
        val (response, _) = get<Any>("/health")

        Assertions.assertEquals(HttpStatus.OK, response.status)
    }
}
