package com.neuro.photostudio.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neuro.photostudio.ui.AppViewModel
import com.neuro.photostudio.ui.screens.AgreementScreen
import com.neuro.photostudio.ui.screens.CategoriesScreen
import com.neuro.photostudio.ui.screens.FavoritesScreen
import com.neuro.photostudio.ui.screens.GalleryScreen
import com.neuro.photostudio.ui.screens.GenerateScreen
import com.neuro.photostudio.ui.screens.HomeScreen
import com.neuro.photostudio.ui.screens.SettingsScreen

private sealed class Dest(val route: String, val title: String) {
    data class Tab(
        val r: String,
        val t: String,
        val selected: ImageVector,
        val unselected: ImageVector
    ) : Dest(r, t)
}

private val tabs = listOf(
    Dest.Tab("home", "Студия", Icons.Filled.Home, Icons.Outlined.Home),
    Dest.Tab("gallery", "Галерея", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary),
    Dest.Tab("favorites", "Избранное", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    Dest.Tab("settings", "Настройки", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroApp(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()

    if (!state.settings.agreementAccepted) {
        AgreementScreen(
            gate = true,
            onAccept = { viewModel.acceptAgreement() },
            onBack = {}
        )
        return
    }

    val busy by viewModel.busy.collectAsState()
    val genStatus by viewModel.status.collectAsState()
    val genError by viewModel.error.collectAsState()

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val isTab = tabs.any { it.route == currentRoute }
    val title = when {
        currentRoute == "categories" -> "Категории"
        currentRoute == "agreement" -> "Соглашение"
        currentRoute?.startsWith("generate") == true ->
            state.categories.firstOrNull {
                it.id == backStack?.arguments?.getString("categoryId")
            }?.title ?: "Генерация"
        else -> tabs.firstOrNull { it.route == currentRoute }?.title ?: "НейроФото"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (!isTab) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isTab) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                val t = tab as Dest.Tab
                                Icon(
                                    if (selected) t.selected else t.unselected,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            appGraph(navController, viewModel, state, busy, genStatus, genError)
        }
    }
}

private fun NavGraphBuilder.appGraph(
    navController: androidx.navigation.NavHostController,
    viewModel: AppViewModel,
    state: com.neuro.photostudio.data.AppState,
    busy: Boolean,
    genStatus: String?,
    genError: String?
) {
    composable("home") {
        HomeScreen(
            state = state,
            onOpenCategory = {
                viewModel.clearError()
                navController.navigate("generate/${it.id}")
            }
        )
    }
    composable("gallery") {
        GalleryScreen(
            state = state,
            onToggleFavorite = viewModel::toggleFavorite,
            onDelete = viewModel::deletePhoto
        )
    }
    composable("favorites") {
        FavoritesScreen(
            state = state,
            onToggleFavorite = viewModel::toggleFavorite,
            onDelete = viewModel::deletePhoto
        )
    }
    composable("settings") {
        SettingsScreen(
            state = state,
            onThemeMode = viewModel::setThemeMode,
            onAccent = viewModel::setAccent,
            onDynamicColor = viewModel::setDynamicColor,
            onGridColumns = viewModel::setGridColumns,
            onCardCorner = viewModel::setCardCorner,
            onApiProvider = viewModel::setApiProvider,
            onApiKey = viewModel::setApiKey,
            onApiModel = viewModel::setApiModel,
            onOpenCategories = { navController.navigate("categories") },
            onOpenAgreement = { navController.navigate("agreement") },
            onClearGallery = viewModel::clearGallery,
            onResetAll = viewModel::resetAll
        )
    }
    composable(
        route = "generate/{categoryId}",
        arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
    ) { entry ->
        val id = entry.arguments?.getString("categoryId")
        val category = state.categories.firstOrNull { it.id == id }
        if (category != null) {
            GenerateScreen(
                category = category,
                state = state,
                busy = busy,
                status = genStatus,
                error = genError,
                onGenerate = { uri, count -> viewModel.generate(category, uri, count) },
                onToggleFavorite = viewModel::toggleFavorite
            )
        }
    }
    composable("categories") {
        CategoriesScreen(
            state = state,
            onSave = viewModel::saveCategory,
            onDelete = viewModel::deleteCategory,
            onMove = viewModel::moveCategory,
            onReset = viewModel::resetCategories,
            newId = viewModel::newCategoryId
        )
    }
    composable("agreement") {
        AgreementScreen(
            gate = false,
            onAccept = {},
            onBack = { navController.popBackStack() }
        )
    }
}
