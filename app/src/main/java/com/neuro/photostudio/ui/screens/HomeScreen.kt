package com.neuro.photostudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.photostudio.data.AppState
import com.neuro.photostudio.data.Category
import com.neuro.photostudio.ui.components.Pill

@Composable
fun HomeScreen(
    state: AppState,
    onOpenCategory: (Category) -> Unit
) {
    val categories = state.categories.filter { it.enabled }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            HeroHeader(photoCount = state.photos.size, styleCount = categories.size)
        }

        items(categories, key = { it.id }) { category ->
            CategoryTile(category = category, onClick = { onOpenCategory(category) })
        }

        if (categories.isEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Нет активных стилей. Добавьте или включите категории в Настройках → Категории.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(photoCount: Int, styleCount: Int) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        "  Нейрофотосессия",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
                Text(
                    "Выберите стиль и создайте серию портретов за секунды",
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 14.dp)
                ) {
                    Pill("Стилей: $styleCount")
                    Pill("Снимков: $photoCount")
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: Category, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(category.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.emoji, fontSize = 26.sp)
            }
            Text(
                category.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp)
            )
            Box(modifier = Modifier.height(4.dp))
        }
    }
}
