package com.example.shared.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

data class TopLevelTab(
    val label: String,
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector
)

val topLevelTabs = listOf(
    TopLevelTab("Start", DashboardRoute, DashboardRoute::class, Icons.Filled.Home),
    TopLevelTab("Dokumente", DocumentsRoute, DocumentsRoute::class, Icons.AutoMirrored.Filled.List),
    TopLevelTab("Prüfungen", ChecksRoute, ChecksRoute::class, Icons.Filled.Check),
    TopLevelTab("Mängel", DefectsRoute, DefectsRoute::class, Icons.Filled.Warning)
)