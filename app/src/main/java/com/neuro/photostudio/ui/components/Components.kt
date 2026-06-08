package com.neuro.photostudio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neuro.photostudio.data.PhotoItem
import java.io.File
import kotlin.math.abs

/** Детерминированный градиент по цвету категории и seed — имитирует уникальный нейро-кадр. */
fun gradientFor(colorHex: Long, seed: Int): Brush {
    val base = Color(colorHex)
    val h = abs(seed)
    val shift = (h % 60) / 100f
    val c1 = Color(
        red = (base.red + shift).coerceIn(0f, 1f),
        green = (base.green * (0.7f + (h % 30) / 100f)).coerceIn(0f, 1f),
        blue = (base.blue + shift / 2).coerceIn(0f, 1f)
    )
    val c2 = Color(
        red = (base.red * 0.5f).coerceIn(0f, 1f),
        green = (base.green * 0.4f).coerceIn(0f, 1f),
        blue = (base.blue * 0.8f + 0.2f).coerceIn(0f, 1f)
    )
    return when (h % 3) {
        0 -> Brush.linearGradient(listOf(c1, c2))
        1 -> Brush.verticalGradient(listOf(c1, c2))
        else -> Brush.linearGradient(listOf(c2, base, c1))
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PhotoCard(
    item: PhotoItem,
    corner: Int,
    onFavorite: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(corner.dp))
            .background(gradientFor(item.colorHex, item.seed))
    ) {
        if (item.imageUrl != null) {
            AsyncImage(
                model = File(item.imageUrl),
                contentDescription = item.categoryTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = item.emoji,
                fontSize = 56.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Затемнённая подпись снизу
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000))))
                .padding(10.dp)
        ) {
            Text(
                item.categoryTitle,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "seed #${item.seed}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }

        // Кнопка избранного
        IconBadge(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            onClick = onFavorite
        ) {
            Icon(
                imageVector = if (item.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "В избранное",
                tint = if (item.favorite) Color(0xFFFF4D6D) else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        if (onDelete != null) {
            IconBadge(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun IconBadge(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0x55000000))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun ColorSwatch(
    color: Long,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(emoji, fontSize = 56.sp)
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
