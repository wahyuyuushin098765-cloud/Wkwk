package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val profile by viewModel.playerProfile.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {
        composable("menu") {
            MainMenuScreen(
                viewModel = viewModel,
                onStartGame = {
                    val name = profile?.generalName ?: "Jenderal"
                    val avatar = profile?.avatarRes ?: 0
                    viewModel.startNewGame(name, avatar)
                    navController.navigate("game")
                },
                onOpenLobby = {
                    navController.navigate("lobby")
                },
                onOpenTutorial = {
                    navController.navigate("tutorial")
                },
                onOpenHistory = {
                    navController.navigate("history")
                },
                onOpenProfile = {
                    navController.navigate("profile")
                }
            )
        }

        composable("game") {
            GameScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("lobby") {
            LobbyScreen(
                viewModel = viewModel,
                onStartMatch = {
                    navController.navigate("game")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("tutorial") {
            TutorialScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
