package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.DemoModeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VivaQnAScreen
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.ScamShieldTheme
import com.example.ui.viewmodel.ScamShieldViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScamShieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            ScamShieldTheme(darkTheme = isDarkMode) {
                ScamShieldApp(viewModel = viewModel)
            }
        }
    }
}

data class NavigationBarDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

val mainDestinations = listOf(
    NavigationBarDestination(
        screen = Screen.Home,
        label = "Home",
        icon = Icons.Default.Home,
        testTag = "nav_home"
    ),
    NavigationBarDestination(
        screen = Screen.Scanner,
        label = "Scanner",
        icon = Icons.Default.Security,
        testTag = "nav_scanner"
    ),
    NavigationBarDestination(
        screen = Screen.DemoMode,
        label = "Trainer",
        icon = Icons.Default.Psychology,
        testTag = "nav_demo"
    ),
    NavigationBarDestination(
        screen = Screen.Insights,
        label = "Insights",
        icon = Icons.Default.Analytics,
        testTag = "nav_insights"
    ),
    NavigationBarDestination(
        screen = Screen.History,
        label = "History",
        icon = Icons.Default.History,
        testTag = "nav_history"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScamShieldApp(viewModel: ScamShieldViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = mainDestinations.any { it.screen.route == currentRoute }

    Scaffold(
        topBar = {
            if (isTopLevelDestination) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberEmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "AI ScamShield PK",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Screen.Faq.route) },
                            modifier = Modifier.testTag("top_viva_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = "Security FAQs & Help",
                                tint = CyberEmeraldPrimary
                            )
                        }
                        IconButton(
                            onClick = { navController.navigate(Screen.Settings.route) },
                            modifier = Modifier.testTag("top_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("main_top_app_bar")
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    mainDestinations.forEach { dest ->
                        val selected = currentRoute == dest.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != dest.screen.route) {
                                    navController.navigate(dest.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyberEmeraldPrimary,
                                selectedTextColor = CyberEmeraldPrimary,
                                indicatorColor = CyberEmeraldPrimary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToScanner = { initialTab ->
                        navController.navigate("scanner?tab=$initialTab")
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToDemo = {
                        navController.navigate(Screen.DemoMode.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToResult = { recordId ->
                        navController.navigate(Screen.Result.createRoute(recordId))
                    }
                )
            }

            composable(
                route = "scanner?tab={tab}",
                arguments = listOf(
                    navArgument("tab") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getInt("tab") ?: 0
                ScannerScreen(
                    viewModel = viewModel,
                    initialTab = tab,
                    onNavigateToResult = { recordId ->
                        navController.navigate(Screen.Result.createRoute(recordId))
                    }
                )
            }

            composable(Screen.Scanner.route) {
                ScannerScreen(
                    viewModel = viewModel,
                    initialTab = 0,
                    onNavigateToResult = { recordId ->
                        navController.navigate(Screen.Result.createRoute(recordId))
                    }
                )
            }

            composable(
                route = Screen.Result.route,
                arguments = listOf(
                    navArgument("recordId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                ResultScreen(
                    recordId = recordId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToNewScan = {
                        viewModel.clearInputs()
                        navController.navigate(Screen.Scanner.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.DemoMode.route) {
                DemoModeScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { recordId ->
                        navController.navigate(Screen.Result.createRoute(recordId))
                    }
                )
            }

            composable(Screen.Insights.route) {
                InsightsScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { recordId ->
                        navController.navigate(Screen.Result.createRoute(recordId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToVivaQnA = {
                        navController.navigate(Screen.VivaQnA.route)
                    }
                )
            }

            composable(Screen.VivaQnA.route) {
                VivaQnAScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

