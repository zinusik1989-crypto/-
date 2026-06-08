package com.neuro.photostudio.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Минимальный клиент Replicate (https://replicate.com).
 * Использует endpoint моделей: POST /v1/models/{owner}/{name}/predictions,
 * что позволяет запускать актуальную версию модели без хеша версии.
 */
class ReplicateClient(
    private val client: OkHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val jsonMedia = "application/json".toMediaType()

    /**
     * Запускает генерацию изображения по входному фото (data-URI) и промпту.
     * Возвращает URL готового изображения.
     */
    suspend fun generate(
        apiKey: String,
        model: String,
        prompt: String,
        imageDataUri: String,
        onStatus: (String) -> Unit = {}
    ): String = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Не указан API-ключ Replicate" }
        require(model.contains("/")) { "Модель должна быть в формате owner/name" }

        val payload = buildJsonObject {
            put("input", buildJsonObject {
                put("prompt", prompt)
                put("input_image", imageDataUri)
                put("output_format", "jpg")
            })
        }

        val createRequest = Request.Builder()
            .url("https://api.replicate.com/v1/models/$model/predictions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("Prefer", "wait")
            .post(payload.toString().toRequestBody(jsonMedia))
            .build()

        var prediction = client.newCall(createRequest).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error(parseError(body) ?: "Ошибка запроса: HTTP ${response.code}")
            }
            json.parseToJsonElement(body).jsonObject
        }

        // Если модель ещё считает — опрашиваем статус.
        var attempts = 0
        while (status(prediction).let { it != "succeeded" && it != "failed" && it != "canceled" }) {
            if (attempts++ > 60) error("Превышено время ожидания генерации")
            onStatus("Обработка нейросетью… (${status(prediction)})")
            val getUrl = prediction["urls"]?.jsonObject?.get("get")?.jsonPrimitive?.contentOrNull
                ?: error("Нет ссылки для опроса статуса")
            delay(2000)
            val pollRequest = Request.Builder()
                .url(getUrl)
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()
            prediction = client.newCall(pollRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("Ошибка опроса: HTTP ${response.code}")
                json.parseToJsonElement(body).jsonObject
            }
        }

        when (status(prediction)) {
            "succeeded" -> extractOutputUrl(prediction)
                ?: error("Модель не вернула изображение")
            else -> error(
                prediction["error"]?.jsonPrimitive?.contentOrNull
                    ?: "Генерация завершилась со статусом: ${status(prediction)}"
            )
        }
    }

    private fun status(obj: JsonObject): String =
        obj["status"]?.jsonPrimitive?.contentOrNull ?: "unknown"

    private fun extractOutputUrl(obj: JsonObject): String? {
        return when (val output = obj["output"]) {
            is JsonArray -> output.firstOrNull()?.let { (it as? JsonPrimitive)?.contentOrNull }
            is JsonPrimitive -> output.contentOrNull
            else -> null
        }
    }

    private fun parseError(body: String): String? = runCatching {
        json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()
}
