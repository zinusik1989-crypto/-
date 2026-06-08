package com.neuro.photostudio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neuro.photostudio.data.AppState
import com.neuro.photostudio.ui.components.EmptyState
import com.neuro.photostudio.ui.components.PhotoCard

@Composable
fun GalleryScreen(
    state: AppState,
    onToggleFavorite: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val photos = state.photos
    if (photos.isEmpty()) {
        EmptyState(
            emoji = "🖼️",
            title = "Галерея пуста",
            subtitle = "Сгенерируйте кадры на вкладке «Студия» — они появятся здесь."
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(state.settings.gridColumns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(photos, key = { it.id }) { item ->
            PhotoCard(
                item = item,
                corner = state.settings.cardCorner,
                onFavorite = { onToggleFavorite(item.id) },
                onDelete = { onDelete(item.id) }
            )
        }
    }
}
