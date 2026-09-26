package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    BackHandler {
        if (uiState.isChatOpen) {
            viewModel.toggleChat()
        } else {
            onNavigateBack()
        }
    }

    // Auto-dismiss toast banner after 2.5s
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(2500)
            viewModel.dismissToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Main Interactive Battle Canvas
        GameCanvas(
            engine = viewModel.engine,
            modifier = Modifier.fillMaxSize()
        )

        // Top HUD Bar
        GameTopBar(
            uiState = uiState,
            onOpenChat = { viewModel.toggleChat() },
            onZoomIn = {
                viewModel.engine.camZ = (viewModel.engine.camZ * 1.25f).coerceIn(0.2f, 2.5f)
            },
            onZoomOut = {
                viewModel.engine.camZ = (viewModel.engine.camZ / 1.25f).coerceIn(0.2f, 2.5f)
            },
            onBackToMenu = onNavigateBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Tactical Minimap in Top Right (below top bar)
        MinimapView(
            engine = viewModel.engine,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 54.dp, end = 6.dp)
        )

        // Toast Banner at Top Center
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
        ) {
            uiState.toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .background(Color(0xD9221C0A), RoundedCornerShape(4.dp))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = msg,
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Tactical Command Bar
        GameBottomBar(
            uiState = uiState,
            onTogglePause = { viewModel.togglePause() },
            onToggleSpeed = { viewModel.toggleSpeed() },
            onSetMoveMode = { viewModel.setMoveMode(it) },
            onSetFormation = { viewModel.setFormation(it) },
            onCycleGeneral = { viewModel.cycleGeneralPosition() },
            onClearSelection = { viewModel.clearSelection() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Collapsible In-Game Chat Drawer (from right side)
        AnimatedVisibility(
            visible = uiState.isChatOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            InGameChatDrawer(
                messages = chatMessages,
                room = currentRoom,
                onClose = { viewModel.toggleChat() },
                onSendMessage = { viewModel.sendChatMessage(it) },
                onSendPing = { viewModel.sendTacticalPing(it) }
            )
        }

        // Game Over / Victory Modal Dialog
        if (uiState.isGameOver) {
            AlertDialog(
                onDismissRequest = { /* forced action */ },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.startNewGame(viewModel.engine.playerName, viewModel.engine.playerAvatarId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("play_again_button")
                    ) {
                        Text(
                            text = "Main Lagi",
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("exit_to_menu_button")
                    ) {
                        Text(
                            text = "Menu Utama",
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                title = {
                    Text(
                        text = if (uiState.isVictory) "KEMENANGAN MUTLAK" else "KEKALAHAN PERANG",
                        color = if (uiState.isVictory) GoldPrimary else EnemyRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.gameOverMessage,
                            color = TextGold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Divider(color = DarkBorder, thickness = 1.dp)
                        Text(
                            text = "Bidak Sekutu Tersisa: ${uiState.allyCount}",
                            color = AllyGreen,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Wilayah Ditaklukkan: ${uiState.capturedTerritoriesCount}/${uiState.totalTerritoriesCount}",
                            color = GoldSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                containerColor = Color(0xF212150B),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.border(1.5.dp, GoldPrimary, RoundedCornerShape(6.dp))
            )
        }
    }
}
