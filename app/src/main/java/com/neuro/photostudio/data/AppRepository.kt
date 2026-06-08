package com.neuro.photostudio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neuro_state")

/** Единое хранилище состояния приложения поверх DataStore. */
class AppRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val stateKey = stringPreferencesKey("app_state")

    val state: Flow<AppState> = context.dataStore.data.map { prefs ->
        val raw = prefs[stateKey]
        val decoded = raw?.let {
            runCatching { json.decodeFromString(AppState.serializer(), it) }.getOrNull()
        }
        normalize(decoded ?: AppState())
    }

    private fun normalize(state: AppState): AppState {
        // При первом запуске наполняем дефолтными категориями.
        return if (state.categories.isEmpty()) {
            state.copy(categories = SampleData.defaultCategories())
        } else state
    }

    private suspend fun update(transform: (AppState) -> AppState) {
        context.dataStore.edit { prefs ->
            val current = prefs[stateKey]?.let {
                runCatching { json.decodeFromString(AppState.serializer(), it) }.getOrNull()
            } ?: AppState()
            val base = normalize(current)
            prefs[stateKey] = json.encodeToString(AppState.serializer(), transform(base))
        }
    }

    // ----- Настройки -----
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) =
        update { it.copy(settings = transform(it.settings)) }

    suspend fun updateApi(transform: (ApiSettings) -> ApiSettings) =
        update { it.copy(settings = it.settings.copy(api = transform(it.settings.api))) }

    suspend fun acceptAgreement() =
        update { it.copy(settings = it.settings.copy(agreementAccepted = true)) }

    // ----- Категории -----
    suspend fun upsertCategory(category: Category) = update { state ->
        val exists = state.categories.any { it.id == category.id }
        val list = if (exists) {
            state.categories.map { if (it.id == category.id) category else it }
        } else {
            state.categories + category
        }
        state.copy(categories = list)
    }

    suspend fun deleteCategory(id: String) = update { state ->
        state.copy(categories = state.categories.filterNot { it.id == id })
    }

    suspend fun moveCategory(id: String, up: Boolean) = update { state ->
        val list = state.categories.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index < 0) return@update state
        val target = if (up) index - 1 else index + 1
        if (target in list.indices) {
            val tmp = list[index]
            list[index] = list[target]
            list[target] = tmp
        }
        state.copy(categories = list)
    }

    suspend fun resetCategories() = update { state ->
        state.copy(categories = SampleData.defaultCategories())
    }

    // ----- Фото -----
    suspend fun addPhotos(items: List<PhotoItem>) = update { state ->
        state.copy(photos = items + state.photos)
    }

    suspend fun toggleFavorite(id: String) = update { state ->
        state.copy(photos = state.photos.map {
            if (it.id == id) it.copy(favorite = !it.favorite) else it
        })
    }

    suspend fun deletePhoto(id: String) = update { state ->
        state.copy(photos = state.photos.filterNot { it.id == id })
    }

    suspend fun clearPhotos() = update { it.copy(photos = emptyList()) }

    suspend fun resetAll() {
        context.dataStore.edit { prefs ->
            prefs[stateKey] = json.encodeToString(
                AppState.serializer(),
                AppState(categories = SampleData.defaultCategories())
            )
        }
    }
}
