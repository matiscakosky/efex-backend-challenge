package com.efex.context.common.handlers
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.hateoas.JsonError
import jakarta.validation.ConstraintViolationException

@Controller
class GlobalExceptionHandler {
    @Error(global = true)
    fun handleConstraintViolation(
        request: HttpRequest<*>,
        exception: ConstraintViolationException,
    ): HttpResponse<JsonError> {
        val violations =
            exception.constraintViolations
                .joinToString(", ") { "${it.propertyPath.last().name}: ${it.message}" }

        val error = JsonError("Validation failed: $violations")
        return HttpResponse.badRequest(error)
    }
}
