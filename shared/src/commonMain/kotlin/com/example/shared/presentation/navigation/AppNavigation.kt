package com.example.shared.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import com.example.shared.presentation.screens.DashboardScreen
import com.example.shared.presentation.screens.PlaceholderScreen
import com.example.shared.presentation.screens.UtilityBillFormScreen
import kotlinx.coroutines.launch

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    onOpenBill = { billId -> navController.navigate(BillRoute(billId)) }
                )
            }

            composable<DocumentsRoute> { PlaceholderScreen("Dokumente") }
            composable<ChecksRoute> { PlaceholderScreen("Prüfungen") }
            composable<DefectsRoute> { PlaceholderScreen("Mängel") }

            composable<AddApartmentRoute> {
                ApartmentFormScreen(
                    onSaved = { navController.popBackStack() },
                    onShowError = showError
                )
            }
            composable<BillRoute> { entry ->
                val route = entry.toRoute<BillRoute>()
                // TODO: UtilityBillOverviewScreen(billId = route.billId, ...)
                PlaceholderScreen("Abrechnung ${route.billId.take(8)}")
            }

            composable<LetterEditorRoute> { entry ->
                val route = entry.toRoute<LetterEditorRoute>()
                // TODO: LetterEditorScreen(route.billId, route.templateType, ...)
                PlaceholderScreen("Schreiben: ${route.templateType}")
            }

            composable<RemindersRoute> { entry ->
                val route = entry.toRoute<RemindersRoute>()
                ReminderListScreen(
                    apartmentId = route.apartmentId,
                    onNavigateToAddReminder = { /* TODO: AddReminderRoute */ },
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

