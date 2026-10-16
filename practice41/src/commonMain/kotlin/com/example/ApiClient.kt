package com.example

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class ApiClient(private val client: HttpClient = HttpClient()) {
    suspend fun fetch(url: String): String = client.get(url).bodyAsText()
}
