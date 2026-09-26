package com.example.data.model

data class PlayerMember(
    val id: String,
    val name: String,
    val avatarId: Int = 0,
    val isReady: Boolean = false,
    val isHost: Boolean = false,
    val team: String = "Sekutu"
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isQuickPing: Boolean = false,
    val isSystem: Boolean = false,
    val senderTeam: String = "Sekutu"
)

data class MultiplayerRoom(
    val id: String,
    val roomCode: String,
    val title: String,
    val hostName: String,
    val maxPlayers: Int = 4,
    val players: List<PlayerMember> = emptyList(),
    val isStarted: Boolean = false,
    val pingMs: Int = 32
)

object QuickPings {
    val list = listOf(
        "Maju serang!",
        "Bantu Jenderal sekarang!",
        "Mundur dan atur barisan!",
        "Pasang Formasi Globus!",
        "Pasang Formasi V-Shape!",
        "Waspada kepungan belakang!",
        "Kuasai wilayah terdekat!",
        "Pertahankan garis depan!",
        "Jenderal musuh terpojok, serbu!"
    )
}
