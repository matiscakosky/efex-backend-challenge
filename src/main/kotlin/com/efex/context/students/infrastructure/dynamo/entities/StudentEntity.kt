package com.efex.context.students.infrastructure.dynamo.entities

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean
class StudentEntity(
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("pk")
    var pk: String? = null,
    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("sk")
    var sk: String? = null,
    @get:DynamoDbAttribute(ATT_LAST_NAME)
    var lastName: String? = null,
    @get:DynamoDbAttribute(ATT_FIRST_NAME)
    var firstName: String? = null,
    @get:DynamoDbAttribute(ATT_BIRTH)
    var birthDate: String? = null,
    @get:DynamoDbAttribute(ATT_GRADE)
    var grade: Long? = null,
    @get:DynamoDbAttribute(ATT_PHONE)
    var phone: String? = null,
    @get:DynamoDbAttribute(ATT_EMAIL)
    var email: String? = null
) {
    companion object {
        private const val ATT_FIRST_NAME = "first_name"
        private const val ATT_LAST_NAME = "last_name"
        private const val ATT_BIRTH = "birth_date"
        private const val ATT_GRADE = "grade"
        private const val ATT_PHONE = "phone"
        private const val ATT_EMAIL = "email"
        private const val PARTITION_KEY_PREFIX = "STUDENT#"
        private const val SORT_KEY_PREFIX = "STUDENT#"

        fun buildPk(pk: Long) = "$PARTITION_KEY_PREFIX$pk"
        fun buildSk(sk: Long) = "$SORT_KEY_PREFIX$sk"
    }
}
