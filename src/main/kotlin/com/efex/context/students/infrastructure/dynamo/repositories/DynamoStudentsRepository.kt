package com.efex.context.students.infrastructure.dynamo.repositories

import com.efex.context.students.domain.entities.Student
import com.efex.context.students.domain.repositories.StudentRepository
import com.efex.context.students.infrastructure.dynamo.entities.StudentEntity
import com.efex.context.students.infrastructure.dynamo.mappers.StudentEntityMapper
import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@Singleton
class DynamoStudentsRepository(
    @Property(name = "aws.dynamo.tables.students.name") private val tableName: String,
    @Inject private val client: DynamoDbClient,
    private val studentMapper: StudentEntityMapper
) : StudentRepository {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(StudentRepository::class.java)
    }
    private val enhancedClient by lazy { DynamoDbEnhancedClient.builder().dynamoDbClient(client).build() }
    private val schema by lazy { TableSchema.fromBean(StudentEntity::class.java) }
    private val table by lazy { enhancedClient.table(tableName, schema) }

    override fun create(student: Student): Student {
        val entity = studentMapper.toEntity(student)
        this.table.putItem(entity)
        return student.copy(id = student.id)
    }

    override fun findAll(): List<Student> {
        return this.table.scan().items().map { studentMapper.toDomain(it) }
    }

    override fun findById(id: String): Student? {
        logger.info("about to retrieve student for id = $id")

        return table.query(
            QueryConditional.keyEqualTo(
                Key.builder()
                    .partitionValue(StudentEntity.buildPk(id))
                    .build(),
            ),
        ).items().map { studentMapper.toDomain(it) }.first()
    }

    override fun update(id: Long, student: Student): Student? {
        this.table.getItem(StudentEntity().apply { this.pk = id.toString() }) ?: return null
        val updatedEntity = studentMapper.toEntity(student.copy(id = id))
        this.table.updateItem(updatedEntity)
        return studentMapper.toDomain(updatedEntity)
    }
}
