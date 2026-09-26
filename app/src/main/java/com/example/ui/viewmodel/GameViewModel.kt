package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.MatchHistoryEntity
import com.example.data.database.PlayerProfileEntity
import com.example.data.model.ChatMessage
import com.example.data.model.MultiplayerRoom
import com.example.data.repository.GameRepository
import com.example.engine.GameEngine
import com.example.network.MultiplayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameUiState(
    val allyCount: Int = 50,
    val enemyCount: Int = 0,
    val capturedTerritoriesCount: Int = 0,
    val totalTerritoriesCount: Int = 20,
    val generalHpPercent: Int = 100,
    val selectedCount: Int = 0,
    val isPaused: Boolean = false,
    val isFastSpeed: Boolean = false,
    val moveMode: String = "atk",
    val formationMode: String? = null,
    val generalMode: String = "tengah",
    val toastMessage: String? = null,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val gameOverMessage: String = "",
    val isChatOpen: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val engine = GameEngine()
    private val repository: GameRepository
    val multiplayerManager: MultiplayerManager = MultiplayerManager(viewModelScope)

    val playerProfile: StateFlow<PlayerProfileEntity?>
    val matchHistory: StateFlow<List<MatchHistoryEntity>>
    val currentRoom: StateFlow<MultiplayerRoom?> = multiplayerManager.currentRoom
    val chatMessages: StateFlow<List<ChatMessage>> = multiplayerManager.chatMessages
    val publicRooms: StateFlow<List<MultiplayerRoom>> = multiplayerManager.publicRooms

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var loopJob: Job? = null
    private var matchStartTime = System.currentTimeMillis()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.gameDao())

        playerProfile = repository.playerProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerProfileEntity()
        )

        matchHistory = repository.matchHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        engine.onToastMessage = { msg ->
            _uiState.value = _uiState.value.copy(toastMessage = msg)
        }

        viewModelScope.launch {
            playerProfile.collect { profile ->
                if (profile != null) {
                    engine.playerName = profile.generalName
                    engine.playerAvatarId = profile.avatarRes
                    multiplayerManager.setLocalPlayerName(profile.generalName)
                }
            }
        }

        startGameLoop()
    }

    fun startNewGame(generalName: String, avatarId: Int) {
        engine.resetGame(generalName, avatarId)
        matchStartTime = System.currentTimeMillis()
        updateUiStateFromEngine()
    }

    private data class StateSnapshot(
        val allies: Int,
        val enemies: Int,
        val captured: Int,
        val genHpPercent: Int,
        val totalTerritories: Int,
        val selectedCount: Int
    )

    private fun startGameLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val dt = (now - lastTime) / 1_000_000_000f
                lastTime = now

                try {
                    engine.update(dt)
                    updateUiStateFromEngine()

                    if (engine.isGameOver && !_uiState.value.isGameOver) {
                        val duration = ((System.currentTimeMillis() - matchStartTime) / 1000).toInt()
                        val (alliesRemaining, enemiesDefeated, captured) = synchronized(engine.lock) {
                            val allies = engine.units.count { it.isAlive && it.team == "p" }
                            val totalEnemies = engine.units.count { it.team == "e" }
                            val aliveEnemies = engine.units.count { it.isAlive && it.team == "e" }
                            val defeated = totalEnemies - aliveEnemies
                            val cap = engine.territories.count { it.team == "p" }
                            Triple(allies, defeated, cap)
                        }
                        val mode = if (currentRoom.value != null) "Multiplayer" else "Solo"

                        repository.recordMatch(
                            result = if (engine.isVictory) "MENANG" else "KALAH",
                            alliesRemaining = alliesRemaining,
                            enemiesDefeated = enemiesDefeated,
                            territoriesCaptured = captured,
                            durationSeconds = duration,
                            mode = mode,
                            currentProfile = playerProfile.value
                        )
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("GameViewModel", "Error in game loop", e)
                }

                delay(16) // ~60 fps
            }
        }
    }

    private fun updateUiStateFromEngine() {
        try {
            val snapshot = synchronized(engine.lock) {
                val a = engine.units.count { it.isAlive && it.team == "p" }
                val e = engine.units.count { it.isAlive && it.team == "e" && !it.isHidden }
                val c = engine.territories.count { it.team == "p" }
                val pgen = engine.playerGeneral
                val hp = pgen?.let {
                    ((it.hp / it.maxHp) * 100).toInt().coerceIn(0, 100)
                } ?: 0
                val tot = engine.territories.size
                val sel = engine.selectedIndices.size
                StateSnapshot(a, e, c, hp, tot, sel)
            }

            _uiState.value = _uiState.value.copy(
                allyCount = snapshot.allies,
                enemyCount = snapshot.enemies,
                capturedTerritoriesCount = snapshot.captured,
                totalTerritoriesCount = snapshot.totalTerritories,
                generalHpPercent = snapshot.genHpPercent,
                selectedCount = snapshot.selectedCount,
                isPaused = engine.isPaused,
                isFastSpeed = engine.speed > 2f,
                moveMode = engine.moveMode,
                formationMode = engine.formMode,
                generalMode = engine.genMode,
                isGameOver = engine.isGameOver,
                isVictory = engine.isVictory,
                gameOverMessage = engine.gameOverMessage
            )
        } catch (e: Throwable) {
            android.util.Log.e("GameViewModel", "Error in updateUiStateFromEngine", e)
        }
    }

    fun togglePause() {
        engine.isPaused = !engine.isPaused
        updateUiStateFromEngine()
    }

    fun toggleSpeed() {
        engine.speed = if (engine.speed > 2f) 1.5f else 3.0f
        updateUiStateFromEngine()
    }

    fun setMoveMode(mode: String) {
        engine.moveMode = mode
        updateUiStateFromEngine()
    }

    fun setFormation(mode: String?) {
        engine.setFormation(mode)
        updateUiStateFromEngine()
    }

    fun cycleGeneralPosition() {
        engine.cycleGeneralMode()
        updateUiStateFromEngine()
    }

    fun clearSelection() {
        engine.clearSelection()
        updateUiStateFromEngine()
    }

    fun dismissToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun toggleChat() {
        _uiState.value = _uiState.value.copy(isChatOpen = !_uiState.value.isChatOpen)
    }

    fun sendChatMessage(text: String) {
        multiplayerManager.sendChat(text, isQuickPing = false)
    }

    fun sendTacticalPing(ping: String) {
        multiplayerManager.sendChat(ping, isQuickPing = true)
        engine.onToastMessage?.invoke("Sinyal Taktis: $ping")
    }

    fun saveProfile(name: String, avatarId: Int) {
        viewModelScope.launch {
            val current = playerProfile.value ?: PlayerProfileEntity()
            repository.saveProfile(current.copy(generalName = name, avatarRes = avatarId))
            engine.playerName = name
            engine.playerAvatarId = avatarId
            multiplayerManager.setLocalPlayerName(name)
        }
    }

    fun clearMatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
