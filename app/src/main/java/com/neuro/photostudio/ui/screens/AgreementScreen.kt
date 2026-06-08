package com.neuro.photostudio.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class AgreementSection(val title: String, val body: String)

private val sections = listOf(
    AgreementSection(
        "1. Общие положения",
        "Настоящее Пользовательское соглашение регулирует использование мобильного приложения «НейроФото» (далее — Приложение). Устанавливая и используя Приложение, вы подтверждаете, что ознакомились с условиями и принимаете их в полном объёме."
    ),
    AgreementSection(
        "2. Назначение сервиса",
        "Приложение предоставляет инструменты для создания стилизованных изображений (нейрофотосессии) на основе выбранных пользователем категорий и настроек. Результаты носят творческий характер и могут не являться точным отображением реальных объектов или людей."
    ),
    AgreementSection(
        "3. Контент и ответственность",
        "Пользователь несёт ответственность за загружаемые материалы и создаваемый контент. Запрещается использовать Приложение для создания материалов, нарушающих закон, права третьих лиц, а также материалов оскорбительного или дискриминационного характера."
    ),
    AgreementSection(
        "4. Конфиденциальность",
        "Приложение хранит ваши настройки и созданные кадры локально на устройстве. Персональные данные не передаются третьим лицам без вашего согласия. Вы можете в любой момент удалить созданные кадры и сбросить настройки."
    ),
    AgreementSection(
        "5. Интеллектуальная собственность",
        "Все права на дизайн, исходный код и графические элементы Приложения принадлежат правообладателю. Созданные вами изображения остаются в вашем распоряжении в рамках действующего законодательства."
    ),
    AgreementSection(
        "6. Изменение условий",
        "Правообладатель вправе обновлять условия Соглашения. Продолжая использовать Приложение после внесения изменений, вы соглашаетесь с обновлёнными условиями."
    )
)

@Composable
fun AgreementScreen(
    gate: Boolean,
    onAccept: () -> Unit,
    onBack: () -> Unit
) {
    if (gate) {
        AgreementGate(onAccept)
    } else {
        AgreementReader(onBack)
    }
}

@Composable
private fun AgreementGate(onAccept: () -> Unit) {
    var checked by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            Icon(
                Icons.Filled.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "  Пользовательское соглашение",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            "Перед началом работы ознакомьтесь с условиями использования.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                sections.forEach { SectionBlock(it) }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp).fillMaxWidth()
        ) {
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            Text("Я прочитал(а) и принимаю условия Соглашения")
        }

        Button(
            onClick = onAccept,
            enabled = checked,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Принять и продолжить", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AgreementReader(onBack: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(sections) { SectionBlock(it) }
        item {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Назад") }
        }
    }
}

@Composable
private fun SectionBlock(section: AgreementSection) {
    Column {
        Text(section.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(
            section.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
