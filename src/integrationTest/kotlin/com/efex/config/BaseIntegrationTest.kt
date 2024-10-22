package com.efex.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.context.annotation.ContextConfigurer
import io.micronaut.context.annotation.Value
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.WriteRequest

@ContextConfigurer
@MicronautTest(environments = ["integration"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest : TestPropertyProvider {
    companion object {
        private const val WEB_SERVER_PORT = 3055
        private const val LOCALSTACK_SCRIPTS_HOST_PATH = "./src/main/resources/localstack/scripts"
        private val localStackContainer: LocalStackContainer =
            LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2.0"))
                .withServices(LocalStackContainer.Service.DYNAMODB)
                .withReuse(true)
                .withFileSystemBind(LOCALSTACK_SCRIPTS_HOST_PATH, "/etc/localstack/init/ready.d", BindMode.READ_ONLY)

        init {
            localStackContainer.start()
        }
    }

    @Value("\${aws.dynamo.tables.students.name}")
    private lateinit var studentsTableName: String

    @Value("\${aws.dynamo.tables.resources.name}")
    private lateinit var resourcesTableName: String

    @Inject
    private lateinit var dynamoClient: DynamoDbClient

    @Inject
    @field:Client("/")
    lateinit var baseHttpClient: HttpClient

    @Inject
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        waitForDynamoDbTables()
    }

    private fun waitForDynamoDbTables() {
        val timeout = System.currentTimeMillis() + 30000
        while (System.currentTimeMillis() < timeout) {
            try {
                val listTablesResponse = dynamoClient.listTables()
                val existingTables = listTablesResponse.tableNames()
                if (existingTables.containsAll(listOf(studentsTableName, resourcesTableName))) {
                    return
                }
            } catch (e: Exception) {
            }
            Thread.sleep(250)
        }
        throw IllegalStateException("Timeout waiting dynamo tables")
    }

    @AfterEach
    fun resetEnvironment() {
        resetEnvironment(dynamoClient, studentsTableName)
        resetEnvironment(dynamoClient, resourcesTableName)
    }

    fun resetEnvironment(
        dynamoDbClient: DynamoDbClient,
        vararg tables: String,
    ) {
        tables.forEach {
            truncateDynamoTable(dynamoDbClient, it)
        }
    }

    private fun truncateDynamoTable(
        dynamoDbClient: DynamoDbClient,
        tableName: String,
    ) {
        val scanResponse = dynamoDbClient.scanPaginator(ScanRequest.builder().tableName(tableName).build())

        // DynamoDB limits to 25 items per batch, don't increase this value
        scanResponse.flatMap { it.items() }.chunked(25).forEach { chunk ->
            val writeRequests =
                chunk.map { item ->
                    val key = mapOf("pk" to item["pk"], "sk" to item["sk"])
                    WriteRequest
                        .builder()
                        .deleteRequest(DeleteRequest.builder().key(key).build())
                        .build()
                }

            val batchWriteRequest =
                BatchWriteItemRequest
                    .builder()
                    .requestItems(mapOf(tableName to writeRequests))
                    .build()

            dynamoDbClient.batchWriteItem(batchWriteRequest)
        }
    }

    val defaultHeaders =
        mapOf(
            HttpHeaders.AUTHORIZATION to "Bearer 1234",
        )

    protected inline fun <reified T> post(
        url: String,
        requestBody: String,
        headers: Map<String, String> = defaultHeaders,
    ): Pair<HttpResponse<String>, T> {
        val request =
            HttpRequest
                .POST(url, requestBody)
                .also {
                    headers.entries.forEach { header ->
                        it.header(header.key, header.value)
                    }
                }

        val httpResponse =
            baseHttpClient
                .toBlocking()
                .exchange(request, Argument.of(String::class.java), Argument.of(String::class.java))

        val responseBody = objectMapper.readValue<T>(httpResponse.body.get())

        return Pair(httpResponse, responseBody)
    }

    protected inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = defaultHeaders,
    ): Pair<HttpResponse<String>, T> {
        val request =
            HttpRequest
                .GET<Any>(url)
                .also {
                    headers.entries.forEach { header ->
                        it.header(header.key, header.value)
                    }
                }

        val httpResponse =
            baseHttpClient
                .toBlocking()
                .exchange(request, Argument.of(String::class.java), Argument.of(String::class.java))

        val responseBody = objectMapper.readValue<T>(httpResponse.body.get())

        return Pair(httpResponse, responseBody)
    }

    protected inline fun <reified T> patch(
        url: String,
        requestBody: String,
        headers: Map<String, String> = defaultHeaders,
    ): Pair<HttpResponse<String>, T> {
        val request =
            HttpRequest
                .PATCH(url, requestBody)
                .also {
                    headers.entries.forEach { header ->
                        it.header(header.key, header.value)
                    }
                }

        val httpResponse =
            baseHttpClient
                .toBlocking()
                .exchange(request, Argument.of(String::class.java), Argument.of(String::class.java))

        val responseBody = objectMapper.readValue<T>(httpResponse.body.get())

        return Pair(httpResponse, responseBody)
    }

    override fun getProperties(): MutableMap<String, String> =
        mutableMapOf(
            "aws.dynamo.endpoint" to
                localStackContainer
                    .getEndpointOverride(LocalStackContainer.Service.DYNAMODB)
                    .toString(),
            "services.products.base-url" to "http://localhost:$WEB_SERVER_PORT",
            "services.financial-indicators.base-url" to "http://localhost:$WEB_SERVER_PORT",
            "auth0.issuers.B2B" to "http://localhost:$WEB_SERVER_PORT",
        )
}
