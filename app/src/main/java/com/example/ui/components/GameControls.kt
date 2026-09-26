package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameUiState

@Composable
fun GameTopBar(
    uiState: GameUiState,
    onOpenChat: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xEE12140A))
            .border(1.dp, Color(0xFF3A3A1E))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onBackToMenu,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("back_to_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali ke Menu",
                    tint = GoldPrimary
                )
            }

            Text(
                text = "PERANG",
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Sekutu: ${uiState.allyCount}",
                color = TextGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Musuh: ${uiState.enemyCount}",
                color = EnemyRed,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Wil: ${uiState.capturedTerritoriesCount}/${uiState.totalTerritoriesCount}",
                color = TextGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "Jnd: ${uiState.generalHpPercent}%",
                color = if (uiState.generalHpPercent < 40) EnemyRed else GoldPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Perkecil Peta",
                    tint = TextGold,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Perbesar Peta",
                    tint = TextGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenChat,
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0x33D4A832), RoundedCornerShape(4.dp))
                    .testTag("open_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Buka Obrolan Perang",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun GameBottomBar(
    uiState: GameUiState,
    onTogglePause: () -> Unit,
    onToggleSpeed: () -> Unit,
    onSetMoveMode: (String) -> Unit,
    onSetFormation: (String?) -> Unit,
    onCycleGeneral: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color(0xF212140A))
            .border(1.dp, Color(0xFF3A3A1E))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Selection counter
        Box(
            modifier = Modifier
                .clickable { onClearSelection() }
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = "Pilih: ${uiState.selectedCount}",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFF3A3A1E))
        )

        // Pause/Play Button
        TacticalButton(
            text = if (uiState.isPaused) "►" else "||",
            isActive = uiState.isPaused,
            onClick = onTogglePause,
            tag = "pause_play_button"
        )

        // Speed 2x Button
        TacticalButton(
            text = "2x",
            isActive = uiState.isFastSpeed,
            onClick = onToggleSpeed,
            tag = "speed_2x_button"
        )

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFF3A3A1E))
        )

        // Serang vs GoTo
        TacticalButton(
            text = "Serang",
            isActive = uiState.moveMode == "atk",
            onClick = { onSetMoveMode("atk") },
            tag = "mode_serang_button"
        )

        TacticalButton(
            text = "GoTo",
            isActive = uiState.moveMode == "goto",
            onClick = { onSetMoveMode("goto") },
            tag = "mode_goto_button"
        )

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFF3A3A1E))
        )

        // Formations: Globus, Simplex, Duplex, V-Shape, Free
        TacticalButton(
            text = "Globus(o)",
            isActive = uiState.formationMode == "globus",
            onClick = { onSetFormation("globus") },
            tag = "formation_globus_button"
        )

        TacticalButton(
            text = "Simplex(-)",
            isActive = uiState.formationMode == "simplex",
            onClick = { onSetFormation("simplex") },
            tag = "formation_simplex_button"
        )

        TacticalButton(
            text = "Duplex(=)",
            isActive = uiState.formationMode == "duplex",
            onClick = { onSetFormation("duplex") },
            tag = "formation_duplex_button"
        )

        TacticalButton(
            text = "V(v)",
            isActive = uiState.formationMode == "vshape",
            onClick = { onSetFormation("vshape") },
            tag = "formation_vshape_button"
        )

        TacticalButton(
            text = "Free(f)",
            isActive = uiState.formationMode == null,
            onClick = { onSetFormation(null) },
            tag = "formation_free_button"
        )

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFF3A3A1E))
        )

        // General position button
        TacticalButton(
            text = "Jendral: ${uiState.generalMode.replaceFirstChar { it.uppercase() }}",
            isActive = true,
            onClick = onCycleGeneral,
            tag = "general_position_button"
        )
    }
}

@Composable
fun TacticalButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) Color(0x33D4A832) else Color(0xDA282A18))
            .border(
                width = 1.dp,
                color = if (isActive) GoldPrimary else Color(0xFF4A4A2A),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) GoldPrimary else TextGold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
