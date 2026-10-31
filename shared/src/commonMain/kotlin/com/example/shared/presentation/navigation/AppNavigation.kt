package com.example.shared.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.shared.domain.model.QuickAction
import com.example.shared.presentation.screens.ApartmentFormScreen
import com.example.shared.presentation.screens.ChecksScreen
import com.example.shared.presentation.screens.DashboardScreen
import com.example.shared.presentation.screens.LetterEditorScreen
import com.example.shared.presentation.screens.PlaceholderScreen
import com.example.shared.presentation.screens.ReminderFormScreen
import com.example.shared.presentation.screens.ReminderListScreen
import com.example.shared.presentation.screens.UtilityBillFormScreen
import com.example.shared.presentation.screens.UtilityBillOverviewScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showError: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = topLevelTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true
    }
    val showTopBar = currentDestination != null && !showBottomBar


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(titleFor(currentDestination)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.hasRoute(tab.routeClass) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTab(tab) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable<DashboardRoute> {
                DashboardScreen(
                    onQuickAction = { action, _ ->
                        // apartmentId wird später über einen geteilten "ausgewählte Wohnung"-State gelöst
                        when (action) {
                            QuickAction.CHECK_BILL -> navController.navigateToTab(topLevelTabs[2])
                            QuickAction.ADD_DOCUMENT -> navController.navigateToTab(topLevelTabs[1])
                            QuickAction.DOCUMENT_DEFECT -> navController.navigateToTab(topLevelTabs[3])
                            // Brief braucht eine Abrechnung: erst in "Prüfungen" auswählen
                            QuickAction.WRITE_LANDLORD -> navController.navigateToTab(topLevelTabs[2])
                        }
                    },
                    onAddApartment = { navController.navigate(AddApartmentRoute) },
                    onOpenBill = { billId -> navController.navigate(BillRoute(billId)) },
                    onOpenReminders = { apartmentId -> navController.navigate(RemindersRoute(apartmentId)) },


                    )
            }

            composable<DocumentsRoute> { PlaceholderScreen("Dokumente") }
            composable<ChecksRoute> {
                ChecksScreen(
                    onAddBill = { apartmentId -> navController.navigate(UtilityBillFormRoute(apartmentId)) },
                    onOpenBill = { billId -> navController.navigate(BillRoute(billId)) }
                )
            }
            composable<DefectsRoute> { PlaceholderScreen("Mängel") }

            composable<AddApartmentRoute> {
                ApartmentFormScreen(
                    onSaved = { navController.popBackStack() },
                    onShowError = showError
                )
            }
            composable<BillRoute> { entry ->
                val route = entry.toRoute<BillRoute>()
                UtilityBillOverviewScreen(
                    billId = route.billId,
                    onWriteLetter = { billId, type ->
                        navController.navigate(LetterEditorRoute(billId, type.name))
                    },
                    onShowError = showError
                )
            }

            composable<LetterEditorRoute> { entry ->
                val route = entry.toRoute<LetterEditorRoute>()
                LetterEditorScreen(
                    billId = route.billId,
                    templateType = route.templateType,
                    onShowError = showError
                )
            }

            composable<RemindersRoute> { entry ->
                val route = entry.toRoute<RemindersRoute>()
                ReminderListScreen(
                    apartmentId = route.apartmentId,
                    onNavigateToAddReminder = { navController.navigate(AddReminderRoute(route.apartmentId)) },
                    onShowError = showError
                )
            }

            composable<UtilityBillFormRoute> { entry ->
                val route = entry.toRoute<UtilityBillFormRoute>()
                UtilityBillFormScreen(
                    apartmentId = route.apartmentId,
                    onSaved = { billId ->
                        navController.navigate(BillRoute(billId)) {
                            popUpTo<UtilityBillFormRoute> { inclusive = true }
                        }
                    },
                    onShowError = showError
                )
            }
            composable<AddReminderRoute> { entry ->
                val route = entry.toRoute<AddReminderRoute>()
                ReminderFormScreen(
                    apartmentId = route.apartmentId,
                    onSaved = { navController.popBackStack() },
                    onShowError = showError
                )
            }

        }
    }
}

/** Standard-Verhalten für Bottom Nav: Zustand pro Tab erhalten, keine doppelten Einträge. */
private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun titleFor(destination: NavDestination?): String = when {
    destination == null -> ""
    destination.hasRoute<BillRoute>() -> "Abrechnung"
    destination.hasRoute<UtilityBillFormRoute>() -> "Abrechnung erfassen"
    destination.hasRoute<LetterEditorRoute>() -> "Schreiben an den Vermieter"
    destination.hasRoute<AddApartmentRoute>() -> "Wohnung anlegen"
    destination.hasRoute<RemindersRoute>() -> "Fristen"
    destination.hasRoute<AddReminderRoute>() -> "Frist hinzufügen"
    else -> ""
}