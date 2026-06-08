package com.neuro.photostudio.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuro.photostudio.data.ApiProvider
import com.neuro.photostudio.data.AppRepository
import com.neuro.photostudio.data.AppState
import com.neuro.photostudio.data.Category
import com.neuro.photostudio.data.PhotoItem
import com.neuro.photostudio.data.ThemeMode
import com.neuro.photostudio.data.network.ReplicateClient
import com.neuro.photostudio.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app.applicationContext)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val replicate = ReplicateClient(httpClient)

    val state: StateFlow<AppState> = repo.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppState()
    )

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    // ----- Настройки / интерфейс -----
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        repo.updateSettings { it.copy(themeMode = mode) }
    }

    fun setAccent(hex: Long) = viewModelScope.launch {
        repo.updateSettings { it.copy(accentHex = hex) }
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        repo.updateSettings { it.copy(dynamicColor = enabled) }
    }

    fun setGridColumns(columns: Int) = viewModelScope.launch {
        repo.updateSettings { it.copy(gridColumns = columns.coerceIn(1, 4)) }
    }

    fun setCardCorner(corner: Int) = viewModelScope.launch {
        repo.updateSettings { it.copy(cardCorner = corner.coerceIn(0, 36)) }
    }

    fun acceptAgreement() = viewModelScope.launch { repo.acceptAgreement() }

    fun resetAll() = viewModelScope.launch { repo.resetAll() }

    // ----- Настройки API -----
    fun setApiProvider(provider: ApiProvider) = viewModelScope.launch {
        repo.updateApi { it.copy(provider = provider) }
    }

    fun setApiKey(key: String) = viewModelScope.launch {
        repo.updateApi { it.copy(apiKey = key) }
    }

    fun setApiModel(model: String) = viewModelScope.launch {
        repo.updateApi { it.copy(model = model) }
    }

    // ----- Категории -----
    fun saveCategory(category: Category) = viewModelScope.launch { repo.upsertCategory(category) }

    fun deleteCategory(id: String) = viewModelScope.launch { repo.deleteCategory(id) }

    fun moveCategory(id: String, up: Boolean) = viewModelScope.launch { repo.moveCategory(id, up) }

    fun resetCategories() = viewModelScope.launch { repo.resetCategories() }

    fun newCategoryId(): String = "cat_" + UUID.randomUUID().toString().take(8)

    // ----- Генерация / фото -----
    fun generate(category: Category, inputUri: Uri?, count: Int = 4) = viewModelScope.launch {
        if (_busy.value) return@launch
        _busy.value = true
        _error.value = null
        val ctx = getApplication<Application>()
        try {
            val api = state.value.settings.api
            val useReal = api.provider == ApiProvider.REPLICATE && api.apiKey.isNotBlank()

            val inputPath = inputUri?.let { ImageUtils.copyToInternal(ctx, it) }
            if (useReal && inputPath == null) {
                error("Выберите фото из галереи или сделайте снимок — оно нужно нейросети")
            }

            for (i in 0 until count) {
                _status.value = "Создаём кадр ${i + 1} из $count…"
                val now = System.currentTimeMillis()
                val item = if (useReal) {
                    val dataUri = ImageUtils.toDataUri(inputPath!!)
                    val url = replicate.generate(
                        apiKey = api.apiKey,
                        model = api.model,
                        prompt = category.buildPrompt(),
                        imageDataUri = dataUri,
                        onStatus = { s -> _status.value = s }
                    )
                    val savedPath = ImageUtils.downloadToInternal(ctx, httpClient, url)
                    PhotoItem(
                        id = UUID.randomUUID().toString(),
                        categoryId = category.id,
                        categoryTitle = category.title,
                        emoji = category.emoji,
                        colorHex = category.colorHex,
                        seed = Random.nextInt(0, 1_000_000),
                        createdAt = now + i,
                        imageUrl = savedPath,
                        inputUri = inputPath
                    )
                } else {
                    // Демо-режим: имитируем обработку, показываем фото пользователя (если есть).
                    kotlinx.coroutines.delay(700)
                    PhotoItem(
                        id = UUID.randomUUID().toString(),
                        categoryId = category.id,
                        categoryTitle = category.title,
                        emoji = category.emoji,
                        colorHex = category.colorHex,
                        seed = Random.nextInt(0, 1_000_000),
                        createdAt = now + i,
                        imageUrl = inputPath,
                        inputUri = inputPath
                    )
                }
                repo.addPhotos(listOf(item))
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Не удалось выполнить генерацию"
        } finally {
            _busy.value = false
            _status.value = null
        }
    }

    fun toggleFavorite(id: String) = viewModelScope.launch { repo.toggleFavorite(id) }

    fun deletePhoto(id: String) = viewModelScope.launch { repo.deletePhoto(id) }

    fun clearGallery() = viewModelScope.launch { repo.clearPhotos() }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                return AppViewModel(app) as T
            }
        }
    }
}
