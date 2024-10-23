package com.efex.context.students.infrastructure.dynamo.repositories

import io.micronaut.context.annotation.Property
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse

@Singleton
class DynamoIncrementalRepository(
    @Property(name = "aws.dynamo.tables.resources.name") private val tableName: String,
    @Inject private val client: DynamoDbClient,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DynamoIncrementalRepository::class.java)
    }

    fun getNextId(): Long {
        logger.info("Increment version of next student")
        val key = IncrementalEntity()

        val updateRequest =
            UpdateItemRequest
                .builder()
                .tableName(tableName)
                .key(
                    mapOf("pk" to AttributeValue.builder().s(key.pk).build()),
                ).updateExpression("SET #val = if_not_exists(#val, :start) + :inc")
                .expressionAttributeNames(mapOf("#val" to "val"))
                .expressionAttributeValues(
                    mapOf(
                        ":inc" to AttributeValue.builder().n("1").build(),
                        ":start" to AttributeValue.builder().n("0").build(),
                    ),
                ).returnValues("UPDATED_NEW")
                .build()

        val response: UpdateItemResponse = client.updateItem(updateRequest)

        return response.attributes()["val"]?.n()?.toLong() ?: 0
    }
}

@DynamoDbBean
class IncrementalEntity(
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("pk")
    var pk: String? = "INCREMENTAL_VALUE",
    @get:DynamoDbAttribute("val")
    var value: Long? = null,
)
