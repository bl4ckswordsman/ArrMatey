package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.log

private const val HEADER_X_API_KEY = "X-Api-Key"

fun createInstanceClient(
    instance: Instance?,
    json: Json,
    customLogger: Logger
) =
    HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            if (instance?.slowInstance == true) {
                val customTimeout = instance.customTimeout?.let { it * 1000 } // convert seconds to millis
                val timeout = customTimeout ?: (10 * 60_000) // 10 second default
                requestTimeoutMillis = timeout
                socketTimeoutMillis = timeout
            }
        }

        install(Logging) {
            logger = customLogger
            level = LogLevel.ALL
        }

        instance?.let { instance ->
            defaultRequest {
                header(HEADER_X_API_KEY, instance.apiKey)
                instance.headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
        }
    }

class HttpClientFactory(private val json: Json, private val logger: Logger) {
    fun create(instance: Instance): HttpClient =
        createInstanceClient(instance, json, logger)

    fun createGeneric(): HttpClient =
        createInstanceClient(null, json, logger)
}

enum class LoggerLevel(
    internal val ktorValue: LogLevel
) {
    All(LogLevel.ALL),
    Headers(LogLevel.HEADERS),
    Body(LogLevel.BODY),
    Info(LogLevel.INFO),
    None(LogLevel.NONE)
}

class DynamicLogger(
    private val preferencesStore: PreferencesStore
): Logger {
    private var currentLogLevel = LogLevel.HEADERS

    init {
        CoroutineScope(Dispatchers.Default).launch {
            preferencesStore.httpLogLevel
                .collect { level ->
                    currentLogLevel = level.ktorValue
                }
        }
    }

    override fun log(message: String) {
        if (currentLogLevel == LogLevel.NONE) return
        if (currentLogLevel == LogLevel.ALL) {
            println(message)
            return
        }

        val lines = message.split("\n")
        val filteredOutput = StringBuilder()

        lines.forEach { line ->
            val shouldInclude = when (currentLogLevel) {
                LogLevel.INFO -> {
                    line.startsWith("REQUEST:") ||
                            line.startsWith("RESPONSE:") ||
                            line.startsWith("METHOD:")
                }
                LogLevel.HEADERS -> {
                    // Include everything except the body sections
                    !isBodyLine(line)
                }
                LogLevel.BODY -> {
                    // Include Request/Response lines and the JSON body, skip headers
                    line.startsWith("REQUEST:") ||
                            line.startsWith("RESPONSE:") ||
                            line.startsWith("METHOD:") ||
                            isBodyLine(line)
                }
                else -> false
            }

            if (shouldInclude) {
                filteredOutput.append(line).append("\n")
            }
        }

        val result = filteredOutput.toString().trim()
        if (result.isNotEmpty()) {
            println(result)
        }
    }
}

private fun isBodyLine(line: String): Boolean {
    val trimmed = line.trim()
    return trimmed.startsWith("BODY") ||
            trimmed.startsWith("{") ||
            trimmed.startsWith("}") ||
            trimmed.startsWith("[") ||
            trimmed.startsWith("]") ||
            trimmed.startsWith("\"")
}