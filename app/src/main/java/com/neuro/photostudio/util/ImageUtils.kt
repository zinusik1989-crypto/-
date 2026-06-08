package com.neuro.photostudio.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.UUID

/** Утилиты хранения изображений в приватной памяти приложения и кодирования для API. */
object ImageUtils {

    private fun imagesDir(context: Context): File =
        File(context.filesDir, "images").apply { if (!exists()) mkdirs() }

    fun authority(context: Context): String = "${context.packageName}.fileprovider"

    /** Создаёт пустой файл для съёмки и возвращает его вместе с content:// Uri для камеры. */
    fun createCaptureTarget(context: Context): Pair<File, Uri> {
        val file = File(imagesDir(context), "in_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, authority(context), file)
        return file to uri
    }

    /** Копирует изображение по Uri во внутреннее хранилище, возвращает абсолютный путь. */
    fun copyToInternal(context: Context, uri: Uri, prefix: String = "in"): String? {
        return runCatching {
            val file = File(imagesDir(context), "${prefix}_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            file.absolutePath
        }.getOrNull()
    }

    /** Кодирует файл в data-URI (base64) для передачи в API. */
    fun toDataUri(path: String): String {
        val bytes = File(path).readBytes()
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$b64"
    }

    /** Скачивает изображение по URL во внутреннее хранилище, возвращает путь. */
    fun downloadToInternal(context: Context, client: OkHttpClient, url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Не удалось скачать результат: HTTP ${response.code}")
            val bytes = response.body?.bytes() ?: error("Пустой ответ при загрузке изображения")
            val file = File(imagesDir(context), "out_${UUID.randomUUID()}.jpg")
            file.writeBytes(bytes)
            return file.absolutePath
        }
    }
}
