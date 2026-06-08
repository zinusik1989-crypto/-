package com.neuro.photostudio.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.neuro.photostudio.data.ApiProvider
import com.neuro.photostudio.data.AppState
import com.neuro.photostudio.data.Category
import com.neuro.photostudio.ui.components.PhotoCard
import com.neuro.photostudio.ui.components.gradientFor
import com.neuro.photostudio.util.ImageUtils

@Composable
fun GenerateScreen(
    category: Category,
    state: AppState,
    busy: Boolean,
    status: String?,
    error: String?,
    onGenerate: (Uri?, Int) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val context = LocalContext.current
    val corner = state.settings.cardCorner
    val columns = state.settings.gridColumns
    val results = state.photos.filter { it.categoryId == category.id }
    val realMode = state.settings.api.provider == ApiProvider.REPLICATE &&
        state.settings.api.apiKey.isNotBlank()

    var count by remember { mutableIntStateOf(if (realMode) 1 else 4) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedUri = uri }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok) selectedUri = pendingCameraUri }

    fun launchCamera() {
        val (_, uri) = ImageUtils.createCaptureTarget(context)
        pendingCameraUri = uri
        takePicture.launch(uri)
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // Превью стиля
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.6f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(gradientFor(category.colorHex, category.title.hashCode())),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(category.emoji, fontSize = 56.sp)
                        Text(
                            category.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            category.description,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }

                // Исходное фото
                Text("Ваше фото", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = !busy) {
                            pickMedia.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val uri = selectedUri
                    if (uri != null) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Исходное фото",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Нажмите, чтобы выбрать фото",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Text("  Галерея")
                    }
                    OutlinedButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
                        },
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Text("  Камера")
                    }
                }

                // Количество кадров
                Text("Количество кадров", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = if (realMode) listOf(1, 2, 3, 4) else listOf(2, 4, 6, 9)
                    options.forEach { n ->
                        FilterChip(
                            selected = count == n,
                            onClick = { count = n },
                            label = { Text("$n") },
                            enabled = !busy
                        )
                    }
                }

                // Режим
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (realMode) "Режим: нейросеть Replicate · модель ${state.settings.api.model}"
                        else "Демо-режим: реальная обработка отключена. Подключите API в Настройках → Нейросеть.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (busy) {
                    Column {
                        Text(
                            status ?: "Генерация…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { onGenerate(selectedUri, count) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Text("  Сгенерировать", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (results.isNotEmpty()) {
                    Text(
                        "Результаты (${results.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        items(results, key = { it.id }) { item ->
            PhotoCard(
                item = item,
                corner = corner,
                onFavorite = { onToggleFavorite(item.id) }
            )
        }
    }
}
