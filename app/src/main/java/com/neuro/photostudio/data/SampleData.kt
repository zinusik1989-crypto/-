package com.neuro.photostudio.data

object SampleData {

    val accentSwatches: List<Long> = listOf(
        0xFF7C4DFF, // фиолетовый
        0xFF2962FF, // синий
        0xFF00BFA5, // бирюзовый
        0xFF00C853, // зелёный
        0xFFFF6D00, // оранжевый
        0xFFFF1744, // красный
        0xFFD500F9, // маджента
        0xFF455A64  // графит
    )

    val categorySwatches: List<Long> = listOf(
        0xFF7C4DFF, 0xFF2962FF, 0xFF00BFA5, 0xFF00C853,
        0xFFFFAB00, 0xFFFF6D00, 0xFFFF1744, 0xFFD500F9,
        0xFF1DE9B6, 0xFFEC407A, 0xFF26C6DA, 0xFF8D6E63
    )

    val emojiPalette: List<String> = listOf(
        "✨", "👔", "🪄", "🤖", "🌃", "🎞️", "🏔️", "🌊",
        "🔥", "❄️", "🌸", "🦾", "🎭", "📸", "🌅", "🪩",
        "👑", "🧬", "🛸", "🎨", "🐉", "💎", "🌿", "⚡"
    )

    fun defaultCategories(): List<Category> = listOf(
        Category(
            id = "biz",
            title = "Деловой портрет",
            emoji = "👔",
            description = "Строгий студийный портрет для LinkedIn и резюме",
            colorHex = 0xFF2962FF
        ),
        Category(
            id = "cyberpunk",
            title = "Киберпанк",
            emoji = "🌃",
            description = "Неон, дождь, отражения ночного мегаполиса",
            colorHex = 0xFFD500F9
        ),
        Category(
            id = "fantasy",
            title = "Фэнтези",
            emoji = "🐉",
            description = "Магический мир, эпичные доспехи и драконы",
            colorHex = 0xFF7C4DFF
        ),
        Category(
            id = "vintage",
            title = "Винтаж",
            emoji = "🎞️",
            description = "Плёночная эстетика 80-х и тёплая зернистость",
            colorHex = 0xFFFF6D00
        ),
        Category(
            id = "nature",
            title = "На природе",
            emoji = "🏔️",
            description = "Горы, рассвет и живой естественный свет",
            colorHex = 0xFF00C853
        ),
        Category(
            id = "glam",
            title = "Гламур",
            emoji = "💎",
            description = "Глянцевый бьюти-портрет с идеальным светом",
            colorHex = 0xFFEC407A
        ),
        Category(
            id = "scifi",
            title = "Sci-Fi",
            emoji = "🛸",
            description = "Космос, скафандры и далёкие галактики",
            colorHex = 0xFF00BFA5
        ),
        Category(
            id = "art",
            title = "Арт-портрет",
            emoji = "🎨",
            description = "Живописный стиль, мазки и насыщенный цвет",
            colorHex = 0xFFFFAB00
        )
    )
}
