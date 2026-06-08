package com.neuro.photostudio.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.neuro.photostudio.data.ApiProvider
import com.neuro.photostudio.data.AppState
import com.neuro.photostudio.data.SampleData
import com.neuro.photostudio.data.ThemeMode
import com.neuro.photostudio.ui.components.ColorSwatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: AppState,
    onThemeMode: (ThemeMode) -> Unit,
    onAccent: (Long) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onGridColumns: (Int) -> Unit,
    onCardCorner: (Int) -> Unit,
    onApiProvider: (ApiProvider) -> Unit,
    onApiKey: (String) -> Unit,
    onApiModel: (String) -> Unit,
    onOpenCategories: () -> Unit,
    onOpenAgreement: () -> Unit,
    onClearGallery: () -> Unit,
    onResetAll: () -> Unit
) {
    val s = state.settings

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Тема
        item {
            SettingsCard(title = "Тема оформления") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(
                        ThemeMode.SYSTEM to "Системная",
                        ThemeMode.LIGHT to "Светлая",
                        ThemeMode.DARK to "Тёмная"
                    )
                    options.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = s.themeMode == mode,
                            onClick = { onThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size)
                        ) { Text(label) }
                    }
                }
            }
        }

        // Акцентный цвет
        item {
            SettingsCard(title = "Акцентный цвет") {
                FlowSwatches(
                    colors = SampleData.accentSwatches,
                    selected = s.accentHex,
                    onSelect = onAccent
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SwitchRow(
                        title = "Динамический цвет (Material You)",
                        subtitle = "Брать палитру из обоев системы",
                        checked = s.dynamicColor,
                        onCheckedChange = onDynamicColor
                    )
                }
            }
        }

        // Интерфейс
        item {
            SettingsCard(title = "Интерфейс") {
                Text("Колонок в сетке: ${s.gridColumns}", fontWeight = FontWeight.Medium)
                Slider(
                    value = s.gridColumns.toFloat(),
                    onValueChange = { onGridColumns(it.toInt()) },
                    valueRange = 1f..4f,
                    steps = 2
                )
                Text(
                    "Скругление карточек: ${s.cardCorner}dp",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Slider(
                    value = s.cardCorner.toFloat(),
                    onValueChange = { onCardCorner(it.toInt()) },
                    valueRange = 0f..36f
                )
            }
        }

        // Нейросеть (API)
        item {
            SettingsCard(title = "Нейросеть (API)") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(
                        ApiProvider.DEMO to "Демо",
                        ApiProvider.REPLICATE to "Replicate"
                    )
                    options.forEachIndexed { index, (provider, label) ->
                        SegmentedButton(
                            selected = s.api.provider == provider,
                            onClick = { onApiProvider(provider) },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size)
                        ) { Text(label) }
                    }
                }

                if (s.api.provider == ApiProvider.REPLICATE) {
                    var key by remember { mutableStateOf(s.api.apiKey) }
                    var model by remember { mutableStateOf(s.api.model) }
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it; onApiKey(it.trim()) },
                        label = { Text("API-ключ Replicate") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it; onApiModel(it.trim()) },
                        label = { Text("Модель (owner/name)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Ключ берётся на replicate.com → Account → API tokens. " +
                            "Модель по умолчанию редактирует ваше фото по стилю. " +
                            "Без ключа работает демо-режим.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Демо-режим: офлайн-имитация генерации (без обращения к нейросети). " +
                            "Выберите Replicate и укажите ключ для реальной обработки фото.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Категории и соглашение
        item {
            SettingsCard(title = "Содержимое") {
                NavRow(
                    icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                    title = "Категории стилей",
                    subtitle = "Добавление, изменение, порядок",
                    onClick = onOpenCategories
                )
                NavRow(
                    icon = { Icon(Icons.Filled.Description, contentDescription = null) },
                    title = "Пользовательское соглашение",
                    subtitle = "Условия использования",
                    onClick = onOpenAgreement
                )
            }
        }

        // Данные
        item {
            SettingsCard(title = "Данные") {
                OutlinedButton(onClick = onClearGallery, modifier = Modifier.fillMaxWidth()) {
                    Text("Очистить галерею")
                }
                TextButton(
                    onClick = onResetAll,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("Сбросить приложение", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        item {
            Text(
                "НейроФото · версия 1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun FlowSwatches(colors: List<Long>, selected: Long, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.chunked(4).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowColors.forEach { c ->
                    ColorSwatch(color = c, selected = c == selected, onClick = { onSelect(c) })
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) { icon() }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
