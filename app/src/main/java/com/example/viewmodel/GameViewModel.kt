package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MithaasDatabase
import com.example.data.PlayerStats
import com.example.model.BoardPosition
import com.example.model.MithaasSweet
import com.example.model.MithaasType
import com.example.network.GeminiRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiState {
    object Idle : AiState
    object Loading : AiState
    data class Success(val text: String) : AiState
    data class Error(val message: String) : AiState
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MithaasDatabase.getDatabase(application)
    private val statsDao = database.playerStatsDao()

    // Grid size (7x7 provides nice layouts with 48dp+ buttons)
    val numRows = 7
    val numCols = 7

    // Board State
    private val _grid = MutableStateFlow<List<List<MithaasSweet?>>>(emptyList())
    val grid: StateFlow<List<List<MithaasSweet?>>> = _grid.asStateFlow()

    // Game Variables
    var score by mutableStateOf(0)
        private set
    var highScore by mutableStateOf(0)
        private set
    var level by mutableStateOf(1)
        private set
    var movesLeft by mutableStateOf(20)
        private set
    val targetScore: Int get() = level * 800

    var isAnimating by mutableStateOf(false)
        private set
    var isGameOver by mutableStateOf(false)
        private set
    var isLevelCompleted by mutableStateOf(false)
        private set

    // Total matched counters
    var totalMatchedSweets by mutableStateOf(0)
        private set

    // Interactive selections
    var selectedPosition by mutableStateOf<BoardPosition?>(null)
    var highlightedMove by mutableStateOf<Pair<BoardPosition, BoardPosition>?>(null)

    // Room Persistent stats
    private val _playerStats = MutableStateFlow<PlayerStats?>(null)
    val playerStats: StateFlow<PlayerStats?> = _playerStats.asStateFlow()

    // AI Companion State
    var aiState by mutableStateOf<AiState>(AiState.Idle)
        private set

    // Premium Tools Unlock State and 1-Time Free Trial status
    var isAiPremiumUnlocked by mutableStateOf(false)
        private set

    var freeTrialsCount by mutableStateOf(0)
        private set

    fun unlockPremiumTools() {
        isAiPremiumUnlocked = true
        viewModelScope.launch {
            val currentStats = _playerStats.value ?: PlayerStats()
            val updatedStats = currentStats.copy(
                isPremiumUnlocked = true
            )
            statsDao.saveStats(updatedStats)
            _playerStats.value = updatedStats
        }
    }

    fun useFreeTrialAndExecute(actionType: Int) {
        freeTrialsCount = 1
        viewModelScope.launch {
            val currentStats = _playerStats.value ?: PlayerStats()
            val updatedStats = currentStats.copy(
                freeTrialsUsed = 1
            )
            statsDao.saveStats(updatedStats)
            _playerStats.value = updatedStats
            
            // Execute corresponding AI tool action post-unlock
            when (actionType) {
                0 -> requestAiMoveHint(isTrialRun = true)
                1 -> requestAiTactics(isTrialRun = true)
                2 -> requestAiRiddle(isTrialRun = true)
            }
        }
    }

    init {
        loadStatsAndStartGame()
    }

    private fun loadStatsAndStartGame() {
        viewModelScope.launch {
            val stats = statsDao.getStats() ?: PlayerStats()
            _playerStats.value = stats
            highScore = stats.highScore
            level = stats.currentLevel
            totalMatchedSweets = stats.totalSweetsMatched
            isAiPremiumUnlocked = stats.isPremiumUnlocked
            freeTrialsCount = stats.freeTrialsUsed
            startLevel(level)
        }
    }

    fun startLevel(levelNum: Int) {
        level = levelNum
        score = 0
        movesLeft = 20 + level * 2
        isGameOver = false
        isLevelCompleted = false
        selectedPosition = null
        highlightedMove = null
        aiState = AiState.Idle
        _grid.value = generateNoMatchInitialBoard()
    }

    fun resetGameProgress() {
        viewModelScope.launch {
            val freshStats = PlayerStats(
                id = 1,
                currentLevel = 1,
                highScore = 0,
                totalStars = 0,
                totalSweetsMatched = 0,
                totalGamesPlayed = 0,
                isPremiumUnlocked = false,
                freeTrialsUsed = 0
            )
            statsDao.saveStats(freshStats)
            _playerStats.value = freshStats
            highScore = 0
            isAiPremiumUnlocked = false
            freeTrialsCount = 0
            startLevel(1)
        }
    }

    // Swaps and handles Match Cascade
    fun selectCell(pos: BoardPosition) {
        if (isAnimating || isGameOver || isLevelCompleted) return

        val currentSelection = selectedPosition
        if (currentSelection == null) {
            selectedPosition = pos
        } else {
            if (currentSelection.isAdjacent(pos)) {
                // Perform swap attempt
                viewModelScope.launch {
                    val p1 = currentSelection
                    val p2 = pos
                    selectedPosition = null
                    highlightedMove = null // Clear any AI hint highlight
                    isAnimating = true

                    // Step 1: Swap sweets
                    val currentGrid = _grid.value.map { it.toMutableList() }.toMutableList()
                    val temp = currentGrid[p1.row][p1.col]
                    currentGrid[p1.row][p1.col] = currentGrid[p2.row][p2.col]
                    currentGrid[p2.row][p2.col] = temp
                    _grid.value = currentGrid
                    delay(200)

                    // Step 2: Validate if any matches formed
                    val matches = findMatches(currentGrid)
                    if (matches.isNotEmpty()) {
                        // Yes! Perfect swap. Deduct moves remaining
                        movesLeft--
                        processCascadeChain()
                    } else {
                        // No match! Swap them back immediately
                        val revertGrid = _grid.value.map { it.toMutableList() }.toMutableList()
                        val revertTemp = revertGrid[p1.row][p1.col]
                        revertGrid[p1.row][p1.col] = revertGrid[p2.row][p2.col]
                        revertGrid[p2.row][p2.col] = revertTemp
                        _grid.value = revertGrid
                        isAnimating = false
                    }
                }
            } else {
                // Redirect user selection to the secondary cell
                selectedPosition = pos
            }
        }
    }

    // Core popping, refilling, and cascading loop
    private suspend fun processCascadeChain() {
        var currentBoard = _grid.value
        var matchCascadeRound = 1

        while (true) {
            val matches = findMatches(currentBoard)
            if (matches.isEmpty()) break

            // 1. Mark and clear matching sweets
            val nextBoardWithNulls = currentBoard.map { it.toMutableList() }.toMutableList()
            var earnedPoints = 0
            for (pos in matches) {
                val sweet = nextBoardWithNulls[pos.row][pos.col]
                if (sweet != null) {
                    earnedPoints += sweet.type.points
                    nextBoardWithNulls[pos.row][pos.col] = null
                }
            }

            // Reward bonus points for cascaded chains!
            val bonusMultiplied = (earnedPoints * matchCascadeRound)
            score += bonusMultiplied
            totalMatchedSweets += matches.size

            _grid.value = nextBoardWithNulls
            delay(350) // Popping visual lag

            // 2. Collapse board cells downward
            val collapsedBoard = collapseBoard(nextBoardWithNulls)
            _grid.value = collapsedBoard
            delay(300) // Fall visual lag

            // 3. Spawns new sweets from top
            val refilledBoard = refillBoard(collapsedBoard)
            _grid.value = refilledBoard
            delay(300)

            currentBoard = refilledBoard
            matchCascadeRound++
        }

        // Validate final win/loss thresholds
        checkGameStatus()
        isAnimating = false
    }

    private fun checkGameStatus() {
        if (score >= targetScore) {
            isLevelCompleted = true
            saveStats(victory = true)
        } else if (movesLeft <= 0) {
            isGameOver = true
            saveStats(victory = false)
        }
    }

    private fun saveStats(victory: Boolean) {
        viewModelScope.launch {
            val currentStats = _playerStats.value ?: PlayerStats()
            val nextLevel = if (victory) level + 1 else level
            val extraStars = if (victory) 3 else 0
            val bestScore = maxOf(highScore, score)

            val updatedStats = currentStats.copy(
                currentLevel = nextLevel,
                highScore = bestScore,
                totalStars = currentStats.totalStars + extraStars,
                totalSweetsMatched = totalMatchedSweets,
                totalGamesPlayed = currentStats.totalGamesPlayed + 1
            )
            statsDao.saveStats(updatedStats)
            _playerStats.value = updatedStats
            highScore = bestScore
        }
    }

    // Board Collapse Logic: non-null cells elements sink to base
    private fun collapseBoard(board: List<List<MithaasSweet?>>): List<List<MithaasSweet?>> {
        val nextBoard = board.map { it.toMutableList() }.toMutableList()
        for (c in 0 until numCols) {
            val columnSweets = mutableListOf<MithaasSweet>()
            for (r in numRows - 1 downTo 0) {
                board[r][c]?.let { columnSweets.add(it) }
            }
            for (r in numRows - 1 downTo 0) {
                val idx = (numRows - 1) - r
                if (idx < columnSweets.size) {
                    nextBoard[r][c] = columnSweets[idx]
                } else {
                    nextBoard[r][c] = null
                }
            }
        }
        return nextBoard
    }

    // Refills top spaces with random sweets
    private fun refillBoard(board: List<List<MithaasSweet?>>): List<List<MithaasSweet?>> {
        val nextBoard = board.map { it.toMutableList() }.toMutableList()
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                if (nextBoard[r][c] == null) {
                    nextBoard[r][c] = MithaasSweet.random()
                }
            }
        }
        return nextBoard
    }

    // Scans for matches (match of 3+ horizontally or vertically)
    fun findMatches(board: List<List<MithaasSweet?>>): Set<BoardPosition> {
        val matchedPositions = mutableSetOf<BoardPosition>()

        // 1. Horizontal scanning
        for (r in 0 until numRows) {
            var matchCount = 1
            var matchType: MithaasType? = null
            var startIndex = 0
            for (c in 0 until numCols) {
                val sweet = board[r][c]
                val type = sweet?.type
                if (c == 0) {
                    matchType = type
                    startIndex = 0
                    matchCount = 1
                } else {
                    if (type != null && type == matchType) {
                        matchCount++
                    } else {
                        if (matchCount >= 3 && matchType != null) {
                            for (i in startIndex until c) {
                                matchedPositions.add(BoardPosition(r, i))
                            }
                        }
                        matchType = type
                        startIndex = c
                        matchCount = 1
                    }
                }
            }
            if (matchCount >= 3 && matchType != null) {
                for (i in startIndex until numCols) {
                    matchedPositions.add(BoardPosition(r, i))
                }
            }
        }

        // 2. Vertical scanning
        for (c in 0 until numCols) {
            var matchCount = 1
            var matchType: MithaasType? = null
            var startIndex = 0
            for (r in 0 until numRows) {
                val sweet = board[r][c]
                val type = sweet?.type
                if (r == 0) {
                    matchType = type
                    startIndex = 0
                    matchCount = 1
                } else {
                    if (type != null && type == matchType) {
                        matchCount++
                    } else {
                        if (matchCount >= 3 && matchType != null) {
                            for (i in startIndex until r) {
                                matchedPositions.add(BoardPosition(i, c))
                            }
                        }
                        matchType = type
                        startIndex = r
                        matchCount = 1
                    }
                }
            }
            if (matchCount >= 3 && matchType != null) {
                for (i in startIndex until numRows) {
                    matchedPositions.add(BoardPosition(i, c))
                }
            }
        }

        return matchedPositions
    }

    // Local deterministic solver to detect valid candidate swap moves
    fun findValidMoves(board: List<List<MithaasSweet?>> = _grid.value): List<Pair<BoardPosition, BoardPosition>> {
        if (board.isEmpty()) return emptyList()

        val validMoves = mutableListOf<Pair<BoardPosition, BoardPosition>>()
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val current = BoardPosition(r, c)
                // We only need to check bottom and right adjacency to cover all transitions uniquely
                val tests = listOf(BoardPosition(r + 1, c), BoardPosition(r, c + 1))
                for (adj in tests) {
                    if (adj.row in 0 until numRows && adj.col in 0 until numCols) {
                        // Trial swap on clone
                        val clone = board.map { it.toMutableList() }
                        val temp = clone[r][c]
                        clone[r][c] = clone[adj.row][adj.col]
                        clone[adj.row][adj.col] = temp

                        if (findMatches(clone).isNotEmpty()) {
                            validMoves.add(Pair(current, adj))
                        }
                    }
                }
            }
        }
        return validMoves
    }

    // Auto visual highlighting button
    fun highlightAHint() {
        val moves = findValidMoves()
        if (moves.isNotEmpty()) {
            val choice = moves.random()
            highlightedMove = choice
        } else {
            // No moves available, shuffle board automatically
            shuffleBoard()
        }
    }

    fun shuffleBoard() {
        viewModelScope.launch {
            isAnimating = true
            var attempts = 0
            var freshBoard = generateNoMatchInitialBoard()
            // Loop until we find a board with AT LEAST one possible move
            while (findValidMoves(freshBoard).isEmpty() && attempts < 10) {
                freshBoard = generateNoMatchInitialBoard()
                attempts++
            }
            _grid.value = freshBoard
            isAnimating = false
        }
    }

    // Creates an initial board with zero pre-existing matches in a single-pass O(N^2) manner
    private fun generateNoMatchInitialBoard(): List<List<MithaasSweet?>> {
        val board = MutableList(numRows) { MutableList<MithaasSweet?>(numCols) { null } }
        val allTypes = MithaasType.values()
        
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val forbidden = mutableSetOf<MithaasType>()
                
                // Do not create horizontal 3-in-a-row match
                if (c >= 2) {
                    val s1 = board[r][c - 1]?.type
                    val s2 = board[r][c - 2]?.type
                    if (s1 != null && s1 == s2) {
                        forbidden.add(s1)
                    }
                }
                
                // Do not create vertical 3-in-a-row match
                if (r >= 2) {
                    val s1 = board[r - 1][c]?.type
                    val s2 = board[r - 2][c]?.type
                    if (s1 != null && s1 == s2) {
                        forbidden.add(s1)
                    }
                }
                
                val allowedTypes = allTypes.filter { it !in forbidden }
                val chosenType = if (allowedTypes.isNotEmpty()) {
                    allowedTypes.random()
                } else {
                    allTypes.random()
                }
                board[r][c] = MithaasSweet(type = chosenType)
            }
        }
        return board
    }

    // AI API Integrations
    private fun serializeBoard(): String {
        val board = _grid.value
        if (board.isEmpty()) return "[]"
        val sb = StringBuilder()
        for (r in 0 until numRows) {
            sb.append("Row $r: ")
            val rowItems = board[r].map { sweet ->
                sweet?.let { "${it.type.emoji} (${it.type.displayName})" } ?: "Empty"
            }
            sb.append(rowItems.joinToString(", "))
            sb.append("\n")
        }
        return sb.toString()
    }

    fun requestAiMoveHint(isTrialRun: Boolean = false) {
        viewModelScope.launch {
            if (!isAiPremiumUnlocked && !isTrialRun) {
                // Return silent, UI will show premium barrier card
                return@launch
            }
            aiState = AiState.Loading
            val rawBoard = serializeBoard()
            val moves = findValidMoves()

            val movesDescription = if (moves.isNotEmpty()) {
                val topMoves = moves.take(3).mapIndexed { idx, pair ->
                    val s1 = _grid.value[pair.first.row][pair.first.col]?.type?.displayName ?: "Unknown"
                    val s2 = _grid.value[pair.second.row][pair.second.col]?.type?.displayName ?: "Unknown"
                    "Move #${idx + 1}: Swap (${pair.first.row}, ${pair.first.col}) [$s1] with (${pair.second.row}, ${pair.second.col}) [$s2]"
                }.joinToString("; ")
                "computed valid options coordinates are: $topMoves."
            } else {
                "No valid moves computed. Board needs a shuffle!"
            }

            val systemInstruction = """
                You are 'Mithaas Guru', highly strategic, witty, and extremely passionate about Indian sweets and candies.
                You are helping the user solve this Match-3 puzzle level.
                Coordinate indexing is 0-based: Row starts at 0 (top) to 6 (bottom). Column starts at 0 (left) to 6 (right).
                You MUST speak in friendly, warm Hinglish (Hindi words written in English letters + English sentences). Include exciting dessert terminologies like: "Arey waah!", "Mithai", "Laddoo pop", "Kaju Katli crack"!
            """.trimIndent()

            val prompt = """
                Current Game Board Matrix:
                $rawBoard
                
                Deterministic solver calculated possible matches:
                $movesDescription
                
                Our Status:
                - Level: $level
                - Current Score: $score / $targetScore
                - Moves Left: $movesLeft
                
                Explain which of these moves is the best or most sweet choice, how it sets up a beautiful chain reaction, and give a supportive booster quote in Hindi to brighten up their day!
            """.trimIndent()

            val response = GeminiRepo.generateResponse(prompt, systemInstruction)
            aiState = if (response.startsWith("Error")) {
                AiState.Error(response)
            } else {
                AiState.Success(response)
            }
        }
    }

    fun requestAiTactics(isTrialRun: Boolean = false) {
        viewModelScope.launch {
            if (!isAiPremiumUnlocked && !isTrialRun) {
                return@launch
            }
            aiState = AiState.Loading
            val rawBoard = serializeBoard()

            val systemInstruction = """
                You are 'Mithaas Guru', the wise, traditional dessert chef and AI expert game strategist.
                You speak in hilarious and extremely encouraging Hinglish (Hindi with English text).
                Provide gameplay secrets to help the player score more with fewer moves.
            """.trimIndent()

            val prompt = """
                I want some expert sweet matching strategy hacks.
                Grid Stats:
                - Current Level: $level
                - Target score to meet: $targetScore
                - Current Score: $score
                - Moves remaining: $movesLeft
                - Current Board Sweets Layout:
                $rawBoard

                Recommend how to prioritize sweets. For example, clearing which ones gives more bonus sweets, or creating L-shapes vs T-shapes. Explain in funny Hinglish.
            """.trimIndent()

            val response = GeminiRepo.generateResponse(prompt, systemInstruction)
            aiState = if (response.startsWith("Error")) {
                AiState.Error(response)
            } else {
                AiState.Success(response)
            }
        }
    }

    fun requestAiRiddle(isTrialRun: Boolean = false) {
        viewModelScope.launch {
            if (!isAiPremiumUnlocked && !isTrialRun) {
                return@launch
            }
            aiState = AiState.Loading

            val systemInstruction = """
                You are 'Mithaas Guru', an interactive quiz master.
                You tell tasty, funny, and engaging riddles about famous Indian sweets.
                Always speak in lively, colorful Hinglish.
            """.trimIndent()

            val prompt = """
                Generate a riddle about one of: Ladoo, Jalebi, Kaju Katli, Gulab Jamun, Barfi, or Rasgulla.
                Format: 
                - "Tasty Riddle": Explain in 2-3 fun lines.
                - "Hint": Give a funny hint.
                - "Shayari/Doha": Add a 2-line rhyming dessert shayari in Hinglish!
                - "Answer": Keep it hidden or structured nicely so they can reveal it.
            """.trimIndent()

            val response = GeminiRepo.generateResponse(prompt, systemInstruction)
            aiState = if (response.startsWith("Error")) {
                AiState.Error(response)
            } else {
                AiState.Success(response)
            }
        }
    }
}
