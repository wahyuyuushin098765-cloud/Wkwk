package com.example.network

import com.example.data.model.ChatMessage
import com.example.data.model.MultiplayerRoom
import com.example.data.model.PlayerMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class MultiplayerManager(private val scope: CoroutineScope) {

    private val _currentRoom = MutableStateFlow<MultiplayerRoom?>(null)
    val currentRoom: StateFlow<MultiplayerRoom?> = _currentRoom.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _publicRooms = MutableStateFlow<List<MultiplayerRoom>>(generateInitialRooms())
    val publicRooms: StateFlow<List<MultiplayerRoom>> = _publicRooms.asStateFlow()

    private var localPlayerId: String = UUID.randomUUID().toString().take(8)
    private var localPlayerName: String = "Jenderal"

    fun setLocalPlayerName(name: String) {
        localPlayerName = name
    }

    fun getLocalPlayerId(): String = localPlayerId

    fun createRoom(title: String, hostName: String = localPlayerName): String {
        localPlayerName = hostName
        val code = (100000 + Random.nextInt(900000)).toString()
        val hostMember = PlayerMember(
            id = localPlayerId,
            name = hostName,
            isReady = true,
            isHost = true,
            team = "Sekutu"
        )
        val room = MultiplayerRoom(
            id = UUID.randomUUID().toString(),
            roomCode = code,
            title = if (title.isBlank()) "Pertempuran $code" else title,
            hostName = hostName,
            maxPlayers = 4,
            players = listOf(hostMember),
            isStarted = false,
            pingMs = Random.nextInt(24, 48)
        )
        _currentRoom.value = room
        _chatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = "SISTEM",
                text = "Ruang tempur $code dibuat. Menunggu sekutu bergabung...",
                isSystem = true
            )
        )
        val list = _publicRooms.value.toMutableList()
        list.add(0, room)
        _publicRooms.value = list
        return code
    }

    fun joinRoom(code: String, playerName: String = localPlayerName): Boolean {
        localPlayerName = playerName
        val trimmed = code.trim()
        val room = _publicRooms.value.find { it.roomCode == trimmed } ?: run {
            // If code not in public sample list, create or join room dynamically
            if (trimmed.length >= 4) {
                val hostMock = PlayerMember(
                    id = "host_${Random.nextInt(100, 999)}",
                    name = "Panglima Jaya",
                    isReady = true,
                    isHost = true,
                    team = "Sekutu"
                )
                val newRoom = MultiplayerRoom(
                    id = UUID.randomUUID().toString(),
                    roomCode = trimmed,
                    title = "Ekspedisi $trimmed",
                    hostName = "Panglima Jaya",
                    players = listOf(hostMock)
                )
                newRoom
            } else return false
        }

        val myMember = PlayerMember(
            id = localPlayerId,
            name = playerName,
            isReady = false,
            isHost = false,
            team = "Sekutu"
        )
        val updatedPlayers = room.players.filter { it.id != localPlayerId } + myMember
        val updatedRoom = room.copy(players = updatedPlayers)
        _currentRoom.value = updatedRoom
        _chatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = "SISTEM",
                text = "$playerName bergabung ke dalam ruang tempur ${room.roomCode}.",
                isSystem = true
            )
        )
        return true
    }

    fun toggleReady() {
        val room = _currentRoom.value ?: return
        val updatedPlayers = room.players.map {
            if (it.id == localPlayerId) it.copy(isReady = !it.isReady) else it
        }
        _currentRoom.value = room.copy(players = updatedPlayers)
    }

    fun startMatch(): Boolean {
        val room = _currentRoom.value ?: return false
        _currentRoom.value = room.copy(isStarted = true)
        postSystemMessage("Pertempuran dimulai! Pimpin pasukanmu menuju kemenangan!")
        return true
    }

    fun leaveRoom() {
        val room = _currentRoom.value ?: return
        val remaining = room.players.filter { it.id != localPlayerId }
        if (remaining.isEmpty()) {
            _publicRooms.value = _publicRooms.value.filter { it.id != room.id }
        }
        _currentRoom.value = null
    }

    fun sendChat(text: String, isQuickPing: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val room = _currentRoom.value
        val name = room?.players?.find { it.id == localPlayerId }?.name ?: localPlayerName
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = name,
            text = trimmed,
            isQuickPing = isQuickPing,
            senderTeam = "Sekutu"
        )
        _chatMessages.value = _chatMessages.value + msg

        // Simulate ally online teammate response if in multiplayer room
        if (room != null && room.players.size > 1) {
            val ally = room.players.firstOrNull { it.id != localPlayerId }
            if (ally != null) {
                simulateAllyResponse(ally.name, trimmed, isQuickPing)
            }
        }
    }

    private fun simulateAllyResponse(allyName: String, messageText: String, isQuickPing: Boolean) {
        scope.launch(Dispatchers.Default) {
            kotlinx.coroutines.delay(Random.nextLong(1200, 2600))
            if (_currentRoom.value == null) return@launch
            val responseText = if (isQuickPing) {
                when {
                    messageText.contains("Maju", ignoreCase = true) -> "Siap! Sayap kanan maju bersama!"
                    messageText.contains("Jenderal", ignoreCase = true) -> "Segera merapat mengawal Jenderal!"
                    messageText.contains("Mundur", ignoreCase = true) -> "Atur barisan, jangan terpisah!"
                    messageText.contains("Globus", ignoreCase = true) -> "Formasi Globus terpasang rapi!"
                    messageText.contains("V-Shape", ignoreCase = true) -> "Ujung panah V siap menembus musuh!"
                    else -> "Laksanakan perintah, Panglima!"
                }
            } else {
                when {
                    messageText.contains("halo", ignoreCase = true) || messageText.contains("hi", ignoreCase = true) ->
                        "Salam, mari rebut tanah Sunda!"
                    messageText.contains("bantu", ignoreCase = true) -> "Pasukan tambahan segera meluncur!"
                    else -> "Dimengerti, koordinasi sayap terjaga."
                }
            }
            val reply = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = allyName,
                text = responseText,
                isQuickPing = isQuickPing,
                senderTeam = "Sekutu"
            )
            _chatMessages.value = _chatMessages.value + reply
        }
    }

    fun postSystemMessage(text: String) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "SISTEM",
            text = text,
            isSystem = true
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    private fun generateInitialRooms(): List<MultiplayerRoom> {
        return listOf(
            MultiplayerRoom(
                id = "room_1",
                roomCode = "882104",
                title = "Perang Sancang Terbuka",
                hostName = "Raden Patih",
                maxPlayers = 4,
                players = listOf(
                    PlayerMember("host_1", "Raden Patih", isReady = true, isHost = true)
                ),
                pingMs = 28
            ),
            MultiplayerRoom(
                id = "room_2",
                roomCode = "519342",
                title = "Gempur Pakuan Pajajaran",
                hostName = "Arya Kamuning",
                maxPlayers = 4,
                players = listOf(
                    PlayerMember("host_2", "Arya Kamuning", isReady = true, isHost = true),
                    PlayerMember("guest_2", "Pangeran Senopati", isReady = true, isHost = false)
                ),
                pingMs = 35
            ),
            MultiplayerRoom(
                id = "room_3",
                roomCode = "773901",
                title = "Taktik Formasi Acies",
                hostName = "Ki Buyut Sancang",
                maxPlayers = 4,
                players = listOf(
                    PlayerMember("host_3", "Ki Buyut Sancang", isReady = true, isHost = true)
                ),
                pingMs = 42
            )
        )
    }
}
