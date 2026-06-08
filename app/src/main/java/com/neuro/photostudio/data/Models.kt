package com.neuro.photostudio.data

import kotlinx.serialization.Serializable

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Провайдер генерации изображений. */
enum class ApiProvider { DEMO, REPLICATE }

/** Настройки подключения к нейросети. */
@Serializable
data class ApiSettings(
    val provider: ApiProvider = ApiProvider.DEMO,
    val apiKey: String = "",
    /** Модель Replicate в формате "owner/name". */
    val model: String = "black-forest-labs/flux-kontext-pro"
)

/** Стиль/категория нейрофотосессии. Полностью настраивается пользователем. */
@Serializable
data class Category(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val colorHex: Long,
    val enabled: Boolean = true,
    /** Доп. подсказка для нейросети (необязательно). */
    val prompt: String = ""
) {
    fun buildPrompt(): String {
        val base = prompt.ifBlank { "$title, $description" }
        return "$base, professional portrait photography, highly detailed, studio lighting, 4k"
    }
}

/** Результат генерации — карточка нейрофото. */
@Serializable
data class PhotoItem(
    val id: String,
    val categoryId: String,
    val categoryTitle: String,
    val emoji: String,
    val colorHex: Long,
    val seed: Int,
    val createdAt: Long,
    val favorite: Boolean = false,
    /** Локальный путь к результату (если есть изображение). null → рисуем градиент. */
    val imageUrl: String? = null,
    /** Локальный путь к исходному фото пользователя. */
    val inputUri: String? = null
)

@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentHex: Long = 0xFF7C4DFF,
    val dynamicColor: Boolean = false,
    val agreementAccepted: Boolean = false,
    val gridColumns: Int = 2,
    val cardCorner: Int = 20,
    val api: ApiSettings = ApiSettings()
)

/** Полное состояние приложения, хранимое в DataStore. */
@Serializable
data class AppState(
    val settings: AppSettings = AppSettings(),
    val categories: List<Category> = emptyList(),
    val photos: List<PhotoItem> = emptyList()
)
