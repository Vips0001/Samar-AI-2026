package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AvatarStudioScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SamarTheme
import com.example.ui.viewmodel.ChatViewModel

object NavigationDestinations {
    const val ROUTE_CHAT = "chat"
    const val ROUTE_HISTORY = "history"
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_AVATAR_STUDIO = "avatar_studio"
}

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val customAccentColor by viewModel.customAccentColor.collectAsStateWithLifecycle()
            val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()

            SamarTheme(
                themeMode = themeMode,
                customAccentColor = customAccentColor,
                fontSize = fontSize
            ) {
                SamarAppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SamarAppNavigation(viewModel: ChatViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationDestinations.ROUTE_CHAT,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(NavigationDestinations.ROUTE_CHAT) {
            ChatScreen(
                viewModel = viewModel,
                onNavigateToHistory = {
                    navController.navigate(NavigationDestinations.ROUTE_HISTORY)
                },
                onNavigateToDashboard = {
                    navController.navigate(NavigationDestinations.ROUTE_DASHBOARD)
                },
                onNavigateToSettings = {
                    navController.navigate(NavigationDestinations.ROUTE_SETTINGS)
                },
                onNavigateToAvatarStudio = {
                    navController.navigate(NavigationDestinations.ROUTE_AVATAR_STUDIO)
                }
            )
        }

        composable(NavigationDestinations.ROUTE_HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSessionSelected = {
                    navController.popBackStack(NavigationDestinations.ROUTE_CHAT, inclusive = false)
                }
            )
        }

        composable(NavigationDestinations.ROUTE_DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationDestinations.ROUTE_SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAvatarStudio = {
                    navController.navigate(NavigationDestinations.ROUTE_AVATAR_STUDIO)
                }
            )
        }

        composable(NavigationDestinations.ROUTE_AVATAR_STUDIO) {
            AvatarStudioScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
