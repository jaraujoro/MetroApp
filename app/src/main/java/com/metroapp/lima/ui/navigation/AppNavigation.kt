package com.metroapp.lima.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.metroapp.lima.ui.screens.dashboard.DashboardScreen
import com.metroapp.lima.ui.screens.reports.ReportsScreen
import com.metroapp.lima.ui.screens.settings.SettingsScreen
import com.metroapp.lima.ui.screens.topup.TopUpScreen
import com.metroapp.lima.ui.screens.transactions.TransactionsScreen
import com.metroapp.lima.ui.theme.MetroGreenContainer
import com.metroapp.lima.ui.theme.MetroGreenPrimary
import com.metroapp.lima.ui.viewmodel.MetroViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "Movimientos", Icons.AutoMirrored.Filled.ReceiptLong)
    object TopUp : Screen("topup", "Recargas", Icons.Default.AddCard)
    object Reports : Screen("reports", "Reportes", Icons.Default.BarChart)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun AppNavigation(
    viewModel: MetroViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val pendingCount by viewModel.pendingCount.collectAsState()

    val screens = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.TopUp,
        Screen.Reports,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (screen == Screen.Transactions && pendingCount > 0) {
                                BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MetroGreenPrimary,
                            selectedTextColor = MetroGreenPrimary,
                            indicatorColor = MetroGreenContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = {
                        navController.navigate(Screen.Transactions.route)
                    },
                    onNavigateToTopUp = {
                        navController.navigate(Screen.TopUp.route)
                    }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(viewModel = viewModel)
            }
            composable(Screen.TopUp.route) {
                TopUpScreen(
                    viewModel = viewModel,
                    onTopUpSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
