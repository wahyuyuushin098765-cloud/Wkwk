package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit,
    onOpenLobby: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val profile by viewModel.playerProfile.collectAsState()
    val generalName = profile?.generalName ?: "Jenderal"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // General Avatar Profile Badge
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, GoldPrimary, CircleShape)
                    .background(Color(0x33D4A832))
                    .clickable { onOpenProfile() }
                    .testTag("avatar_profile_badge"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_general_avatar_1790441484671),
                    contentDescription = "Foto Profil Jenderal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = generalName,
                color = GoldPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Text(
                text = "Panglima Perang Nusantara",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Game Title
            Text(
                text = "PERANG STRATEGI",
                color = GoldPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Real-Time Strategy Taktik Nusantara",
                color = TextGold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            Text(
                text = "Taklukkan 20 wilayah kerajaan musuh dengan 50 prajurit sekutu, formasi militer kuno, dan taktik manuver flanking.",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            MenuActionButton(
                title = "MULAI PERANG (SOLO)",
                subtitle = "Pimpin sekutu menghadapi 20 penguasa wilayah",
                icon = Icons.Default.PlayArrow,
                isPrimary = true,
                onClick = onStartGame,
                tag = "start_singleplayer_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuActionButton(
                title = "MULTIPLAYER ONLINE & CHAT",
                subtitle = "Buat atau gabung ruang tempur bersama sekutu",
                icon = Icons.Default.Groups,
                isPrimary = false,
                onClick = onOpenLobby,
                tag = "open_multiplayer_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuActionButton(
                title = "PANDUAN TAKTIK & FORMASI",
                subtitle = "Pelajari Globus, Acies, V-Shape, dan Serangan Belakang",
                icon = Icons.Default.MenuBook,
                isPrimary = false,
                onClick = onOpenTutorial,
                tag = "open_tutorial_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.widthIn(max = 340.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenHistory,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4A4A2A))),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("match_history_button")
                ) {
                    Icon(Icons.Default.History, contentDescription = "Riwayat", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Riwayat", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                OutlinedButton(
                    onClick = onOpenProfile,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4A4A2A))),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("edit_profile_button")
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profil", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Profil", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun MenuActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(
                1.5.dp,
                if (isPrimary) GoldPrimary else Color(0xFF4A4A2A),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color(0xDD3A3416) else Color(0xCC202315)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isPrimary) GoldPrimary else Color(0xFF33381B), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) Color.Black else GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = if (isPrimary) GoldSecondary else GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
