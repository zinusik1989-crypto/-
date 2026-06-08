package com.neuro.photostudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.photostudio.data.AppState
import com.neuro.photostudio.data.Category
import com.neuro.photostudio.data.SampleData
import com.neuro.photostudio.ui.components.ColorSwatch

@Composable
fun CategoriesScreen(
    state: AppState,
    onSave: (Category) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (String, Boolean) -> Unit,
    onReset: () -> Unit,
    newId: () -> String
) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editing = Category(
                        id = newId(),
                        title = "",
                        emoji = "✨",
                        description = "",
                        colorHex = SampleData.categorySwatches.first()
                    )
                    showEditor = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Новый стиль") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            itemsIndexed(state.categories, key = { _, c -> c.id }) { index, category ->
                CategoryRow(
                    category = category,
                    isFirst = index == 0,
                    isLast = index == state.categories.lastIndex,
                    onEdit = { editing = category; showEditor = true },
                    onDelete = { onDelete(category.id) },
                    onUp = { onMove(category.id, true) },
                    onDown = { onMove(category.id, false) },
                    onToggle = { onSave(category.copy(enabled = it)) }
                )
            }
            item {
                TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                    Text("Сбросить к стандартным категориям")
                }
            }
        }
    }

    if (showEditor) {
        val current = editing
        if (current != null) {
            CategoryEditorDialog(
                initial = current,
                onDismiss = { showEditor = false },
                onConfirm = {
                    onSave(it)
                    showEditor = false
                }
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(category.colorHex)),
                    contentAlignment = Alignment.Center
                ) { Text(category.emoji, fontSize = 24.sp) }

                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        category.title.ifBlank { "Без названия" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        category.description.ifBlank { "Без описания" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Switch(checked = category.enabled, onCheckedChange = onToggle)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                IconButton(onClick = onUp, enabled = !isFirst) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Вверх")
                }
                IconButton(onClick = onDown, enabled = !isLast) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Вниз")
                }
                Box(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Изменить")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(
    initial: Category,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    var title by remember { mutableStateOf(initial.title) }
    var description by remember { mutableStateOf(initial.description) }
    var emoji by remember { mutableStateOf(initial.emoji) }
    var color by remember { mutableStateOf(initial.colorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        initial.copy(
                            title = title.trim().ifBlank { "Новый стиль" },
                            description = description.trim(),
                            emoji = emoji.ifBlank { "✨" },
                            colorHex = color
                        )
                    )
                }
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        title = { Text("Стиль нейрофотосессии") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Название") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Эмодзи", fontWeight = FontWeight.Medium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(emoji, fontSize = 20.sp) }
                        )
                        Text(
                            "Выбрано",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    EmojiGrid(selected = emoji, onSelect = { emoji = it })
                }
                item {
                    Text("Цвет", fontWeight = FontWeight.Medium)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SampleData.categorySwatches.chunked(4).forEach { rowColors ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowColors.forEach { c ->
                                    ColorSwatch(
                                        color = c,
                                        selected = c == color,
                                        onClick = { color = c }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun EmojiGrid(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleData.emojiPalette.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { e ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (e == selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onSelect(e) },
                        contentAlignment = Alignment.Center
                    ) { Text(e, fontSize = 20.sp) }
                }
            }
        }
    }
}
