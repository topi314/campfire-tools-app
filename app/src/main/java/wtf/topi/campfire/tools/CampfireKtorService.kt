package wtf.topi.campfire.tools

import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object CampfireKtorService {
    private const val BASE_URL = "https://campfire-tools.topi.wtf/api/"

    val client: HttpClient by lazy {
        HttpClient(Android) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    suspend fun importEvents(eventLinks: List<String>): HttpResponse {
        return client.post("${BASE_URL}events") {
            contentType(ContentType.Application.Json)
            setBody(eventLinks)
        }
    }
}