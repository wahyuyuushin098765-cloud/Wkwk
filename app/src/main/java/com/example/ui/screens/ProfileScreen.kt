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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.playerProfile.collectAsState()
    var nameInput by remember(profile?.generalName) { mutableStateOf(profile?.generalName ?: "Jenderal") }
    var selectedAvatarId by remember(profile?.avatarRes) { mutableIntStateOf(profile?.avatarRes ?: 0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PROFIL JENDERAL",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF14170D))
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Avatar Circle
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, GoldPrimary, CircleShape)
                    .background(Color(0x22D4A832)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_general_avatar_1790441484671),
                    contentDescription = "Foto Jenderal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "Pilih Lambang & Nama Panglima",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            // Name Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Nama Jenderal (Maksimal 20 karakter):",
                    color = TextGold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        if (it.length <= 20) nameInput = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF4A4A2A),
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextGold,
                        focusedContainerColor = Color(0xFF14170D),
                        unfocusedContainerColor = Color(0xFF14170D)
                    )
                )
                Text(
                    text = "${nameInput.length}/20",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }

            // Save Button
            Button(
                onClick = {
                    val finalName = if (nameInput.isBlank()) "Jenderal" else nameInput.trim()
                    viewModel.saveProfile(finalName, selectedAvatarId)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Profil", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Career War Statistics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3A3A1E), RoundedCornerShape(4.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16190E))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STATISTIK KARIER PERANG",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    StatRow("Total Kemenangan", "${profile?.totalWins ?: 0}")
                    StatRow("Total Kekalahan", "${profile?.totalDefeats ?: 0}")
                    StatRow("Wilayah Ditaklukkan", "${profile?.totalTerritoriesCaptured ?: 0}")
                    StatRow("Sekutu Bertahan Tertinggi", "${profile?.highestAlliedSurviving ?: 0}")
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
