package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MultiplayerRoom
import com.example.ui.components.InGameChatDrawer
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: GameViewModel,
    onStartMatch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentRoom by viewModel.currentRoom.collectAsState()
    val publicRooms by viewModel.publicRooms.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var roomTitleInput by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }

    LaunchedEffect(currentRoom?.isStarted) {
        if (currentRoom?.isStarted == true) {
            onStartMatch()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentRoom != null) "RUANG TEMPUR #${currentRoom?.roomCode}" else "LOBI MULTIPLAYER",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentRoom != null) {
                                viewModel.multiplayerManager.leaveRoom()
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("lobby_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF14170D)
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (currentRoom == null) {
            // Room browser / Join / Create Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Quick Join / Create actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("create_room_button"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buat Ruang", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direct Room Code Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it },
                        placeholder = { Text("Kode Ruang (6 digit)...", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("join_code_input"),
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

                    Button(
                        onClick = {
                            if (joinCodeInput.isNotBlank()) {
                                viewModel.multiplayerManager.joinRoom(joinCodeInput, profile?.generalName ?: "Jenderal")
                            }
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("join_room_by_code_button"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33381B))
                    ) {
                        Text("Masuk", color = GoldPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Daftar Ruang Terbuka Aktif:",
                    color = TextGold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(publicRooms) { room ->
                        RoomCard(
                            room = room,
                            onJoin = {
                                viewModel.multiplayerManager.joinRoom(room.roomCode, profile?.generalName ?: "Jenderal")
                            }
                        )
                    }
                }
            }
        } else {
            // Inside Room Lobby
            val room = currentRoom!!
            val myId = viewModel.multiplayerManager.getLocalPlayerId()
            val isHost = room.players.any { it.id == myId && it.isHost }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Left Column: Room info & Player List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = room.title,
                                    color = GoldPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Kode Undangan: #${room.roomCode} • Ping ${room.pingMs}ms",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Divider(color = DarkBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

                        Text(
                            text = "Prajurit Sekutu di Ruang (${room.players.size}/${room.maxPlayers}):",
                            color = TextGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(room.players) { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E2113), RoundedCornerShape(4.dp))
                                        .border(1.dp, if (p.isHost) GoldPrimary else Color(0xFF3A3A1E), RoundedCornerShape(4.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (p.isHost) Icons.Default.MilitaryTech else Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (p.isHost) GoldPrimary else AllyGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = p.name,
                                                color = if (p.id == myId) GoldSecondary else TextGold,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = if (p.isHost) "Panglima Ruang" else "Prajurit Sekutu",
                                                color = TextMuted,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(if (p.isReady) Color(0x334A9A4A) else Color(0x339A2A2A), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (p.isReady) "SIAP" else "BELUM SIAP",
                                            color = if (p.isReady) AllyGreen else EnemyRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom controls
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.multiplayerManager.toggleReady() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("toggle_ready_button"),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282A18))
                        ) {
                            Text(
                                text = "Ganti Status Siap",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (isHost) {
                            Button(
                                onClick = {
                                    if (viewModel.multiplayerManager.startMatch()) {
                                        viewModel.startNewGame(profile?.generalName ?: "Jenderal", profile?.avatarRes ?: 0)
                                        onStartMatch()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_multiplayer_match_button"),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                            ) {
                                Text(
                                    text = "MULAI PERANG SEKARANG",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Right Column: In-Lobby Chat
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                ) {
                    InGameChatDrawer(
                        messages = chatMessages,
                        room = room,
                        onClose = { /* keep open in lobby */ },
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onSendPing = { viewModel.sendTacticalPing(it) }
                    )
                }
            }
        }

        // Create Room Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.multiplayerManager.createRoom(
                                title = roomTitleInput,
                                hostName = profile?.generalName ?: "Jenderal"
                            )
                            showCreateDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("confirm_create_room_button")
                    ) {
                        Text("Buat", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Batal", color = TextGold, fontFamily = FontFamily.Monospace)
                    }
                },
                title = {
                    Text("Buat Ruang Tempur Baru", color = GoldPrimary, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                },
                text = {
                    Column {
                        Text("Beri nama ekspedisi ruang tempur:", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = roomTitleInput,
                            onValueChange = { roomTitleInput = it },
                            placeholder = { Text("Contoh: Pertempuran Galuh", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace) },
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
                    }
                },
                containerColor = Color(0xFF181C10),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, GoldPrimary, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun RoomCard(
    room: MultiplayerRoom,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFF3A3A1E), RoundedCornerShape(4.dp))
            .clickable { onJoin() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D11))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = room.title,
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Panglima: ${room.hostName} • #${room.roomCode}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${room.players.size}/${room.maxPlayers} Pemain",
                    color = GoldSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Button(
                    onClick = onJoin,
                    shape = RoundedCornerShape(3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33381B)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Gabung", color = GoldPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
