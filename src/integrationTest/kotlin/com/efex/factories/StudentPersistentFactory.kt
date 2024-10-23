package com.efex.factories

import com.efex.context.students.domain.entities.Student
import com.efex.context.students.infrastructure.dynamo.entities.StudentEntity
import com.efex.context.students.infrastructure.dynamo.mappers.StudentEntityMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.LocalDate

@Singleton
class StudentPersistentFactory {
    @Inject
    lateinit var dynamoClient: DynamoDbClient

    @Inject
    lateinit var mapper: StudentEntityMapper

    @Value("\${aws.dynamo.tables.students.name}")
    private lateinit var tableName: String

    fun create(
        id: Long? = 1,
        firstName: String? = "Scarlet",
        lastName: String? = "Evans",
        dateOfBirth: LocalDate? = LocalDate.MIN,
        grade: Long? = 5L,
        phone: String? = "+1111111",
        email: String? = "scarlet@email.com",
    ): Student {
        val table =
            DynamoDbEnhancedClient
                .builder()
                .dynamoDbClient(dynamoClient)
                .build()
                .table(tableName, TableSchema.fromBean(StudentEntity::class.java))

        val entity =
            StudentEntity(
                pk = StudentEntity.buildPk(id!!),
                sk = StudentEntity.buildSk(id),
                firstName = firstName,
                lastName = lastName,
                birthDate = dateOfBirth.toString(),
                grade = grade,
                phone = phone,
                email = email,
            )

        table.putItem(entity)

        return mapper.toDomain(entity)
    }
}
