package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
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
import com.example.data.model.ChatMessage
import com.example.data.model.MultiplayerRoom
import com.example.data.model.QuickPings
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun InGameChatDrawer(
    messages: List<ChatMessage>,
    room: MultiplayerRoom?,
    onClose: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendPing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Color(0xF50D0F08))
            .border(1.5.dp, Color(0xFF3A3A1E))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "OBROLAN TAKTIK",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (room != null) "Ruang #${room.roomCode} (${room.players.size} Pemain - ${room.pingMs}ms)" else "Mode Skuad Lokal",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp).testTag("close_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Chat",
                        tint = TextGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = Color(0xFF3A3A1E), thickness = 1.dp)

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
            }

            // Quick Tactical Pings
            Text(
                text = "Panggilan Taktis Cepat:",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (ping in QuickPings.list) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF222615))
                            .border(1.dp, Color(0xFF4A4A2A), RoundedCornerShape(4.dp))
                            .clickable {
                                onSendPing(ping)
                                scope.launch {
                                    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = ping,
                            color = GoldSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Ketik instruksi...",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("chat_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF4A4A2A),
                        focusedTextColor = TextGold,
                        unfocusedTextColor = TextGold,
                        cursorColor = GoldPrimary,
                        focusedContainerColor = Color(0xFF14170D),
                        unfocusedContainerColor = Color(0xFF14170D)
                    )
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                            scope.launch {
                                if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF33381B), RoundedCornerShape(4.dp))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(4.dp))
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Kirim Pesan",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (message.isSystem) Color(0x33C44040) else if (message.isQuickPing) Color(0x33D4A832) else Color(0x33282A18))
            .border(
                1.dp,
                if (message.isSystem) Color(0x66C44040) else if (message.isQuickPing) Color(0x66D4A832) else Color(0xFF3A3A1E),
                RoundedCornerShape(4.dp)
            )
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message.senderName,
                color = if (message.isSystem) EnemyRed else GoldPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (message.isQuickPing) {
                Text(
                    text = "[PING]",
                    color = GoldSecondary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = message.text,
            color = if (message.isSystem) Color(0xFFFFB0B0) else TextGold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
