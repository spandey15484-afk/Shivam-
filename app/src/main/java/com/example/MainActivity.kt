package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BoardPosition
import com.example.model.MithaasSweet
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AiState
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF7F2FA)
                ) { innerPadding ->
                    BentoMithaasAppScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BentoMithaasAppScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    var selectedBottomTab by remember { mutableStateOf(0) }
    
    // Dialog states for purchase
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var checkoutProcessing by remember { mutableStateOf(false) }
    var checkoutSuccess by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F2FA))
    ) {
        // Main Content Area based on Selected Bottom Nav Tab
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedBottomTab) {
                0 -> GamePlayTabContent(
                    viewModel = viewModel,
                    onTriggerPurchase = { showCheckoutDialog = true }
                )
                1 -> ForgeSkinsTabContent(
                    viewModel = viewModel,
                    onTriggerPurchase = { showCheckoutDialog = true }
                )
                2 -> EventsLeaderboardTabContent(
                    viewModel = viewModel
                )
                3 -> ArtificialLabTabContent(
                    viewModel = viewModel
                )
            }
        }

        // Beautiful Material 3 Styled Bottom Navigation Bar
        BentoBottomNav(
            selectedTab = selectedBottomTab,
            onTabSelected = { selectedBottomTab = it }
        )
    }

    // Google Play/Checkout Emulated Dialog
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!checkoutProcessing) {
                    showCheckoutDialog = false
                    checkoutSuccess = false
                }
            },
            confirmButton = {
                if (checkoutSuccess) {
                    Button(
                        onClick = {
                            viewModel.unlockPremiumTools()
                            showCheckoutDialog = false
                            checkoutSuccess = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text("Shuru Karein! 🎉", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                checkoutProcessing = true
                                delay(1200) // Simulate processing lag
                                checkoutProcessing = false
                                checkoutSuccess = true
                            }
                        },
                        enabled = !checkoutProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        if (checkoutProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Pay $2.00 & Unlock 💳", color = Color.White)
                        }
                    }
                }
            },
            dismissButton = {
                if (!checkoutSuccess && !checkoutProcessing) {
                    TextButton(onClick = { showCheckoutDialog = false }) {
                        Text("Cancel", color = Color(0xFF6750A4))
                    }
                }
            },
            title = {
                Text(
                    text = if (checkoutSuccess) "✨ Premium Activated!" else "💳 Safe Secure Checkout",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (checkoutSuccess) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aapka $2.00 payment successful raha! Premium Mithaas AI Tools aur skins ab lifetime unlock ho gaye hain.",
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1D1B20),
                            fontSize = 14.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Store",
                            tint = Color(0xFF6750A4),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Mithaas Match AI Premium Package",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Unlock Karo Lifetime Access to AI Strategist, Riddles & Custom Visual Skins Builder!",
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1D1B20).copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount:", fontSize = 13.sp, color = Color(0xFF21005D), fontWeight = FontWeight.SemiBold)
                                Text("$2.00 USD (Approx. ₹166)", fontSize = 16.sp, color = Color(0xFF21005D), fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🔒 Payment is fully mocked. Click mock pay to unlock assets immediately.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

// ==========================================
// PLAY TAB: Play-3 Game & AI Strategist
// ==========================================
@Composable
fun GamePlayTabContent(
    viewModel: GameViewModel,
    onTriggerPurchase: () -> Unit
) {
    val gridState by viewModel.grid.collectAsState()
    val statsState by viewModel.playerStats.collectAsState()
    val selectedPos = viewModel.selectedPosition
    val highlightedMove = viewModel.highlightedMove

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bento Page Header
        BentoHeader(
            subheading = "AI Playground",
            title = "Candy AI Quest"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Brand Identity Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)), // Light festive rose tint
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFFFD3D5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Mithaas Match Logo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Mithaas Match AI",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF880E4F)
                    )
                    Text(
                        text = "Swaadist Jalebi & Ladoo Match Quest! 🍬✨",
                        fontSize = 11.sp,
                        color = Color(0xFFAD1457),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Large Prime Bento Card: The Match-3 active grid board
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, Color(0xFFE6E1E5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header of Board containing level tag and status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFFFDE8FF))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE LEVEL: ${statsState?.currentLevel ?: viewModel.level}",
                            color = Color(0xFFD04BD3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Saffron Heart count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Hearts",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "5/5",
                            color = Color(0xFF1D1B20),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Interactive 7x7 Grid Nest
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.04f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF8FF))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (gridState.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (r in 0 until viewModel.numRows) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (c in 0 until viewModel.numCols) {
                                        val cellPos = BoardPosition(r, c)
                                        val sweet = gridState[r][c]

                                        val isCellSelected = selectedPos == cellPos
                                        val isCellHighlighted = highlightedMove != null &&
                                                (highlightedMove.first == cellPos || highlightedMove.second == cellPos)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            BentoSweetCell(
                                                sweet = sweet,
                                                isSelected = isCellSelected,
                                                isHighlighted = isCellHighlighted,
                                                onClick = { viewModel.selectCell(cellPos) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        CircularProgressIndicator(color = Color(0xFF6750A4))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Board Quick Manual actions inside main page
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.shuffleBoard() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E5F5)),
                        shape = RoundedCornerShape(99.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shuffle", color = Color(0xFF7B1FA2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.highlightAHint() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        shape = RoundedCornerShape(99.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY LEVEL HINT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row of Stats: Moves Card & level target details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bento Stats 1: Moves Left
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(95.dp)
                    .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Moves Left",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "${viewModel.movesLeft}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (viewModel.movesLeft <= 5) Color(0xFFFF5252) else Color(0xFF6750A4)
                    )
                }
            }

            // Bento Stats 2: Active Score Progress
            Card(
                modifier = Modifier
                    .weight(1.8f)
                    .height(95.dp)
                    .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Score: ${viewModel.score}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        Text("Goal: ${viewModel.targetScore}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val ratio = (viewModel.score.toFloat() / viewModel.targetScore.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Color(0xFFD04BD3),
                        trackColor = Color(0xFFF3E5F5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (viewModel.score >= viewModel.targetScore) "Sweet Win Achieved!" else "${maxOf(0, viewModel.targetScore - viewModel.score)} points remaining",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Premium Checker & Gemini strategics Bento Card
        BentoAiStrategistCard(
            viewModel = viewModel,
            onPurchaseClicked = onTriggerPurchase
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Victory/Loss popups
    if (viewModel.isLevelCompleted) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = { viewModel.startLevel(viewModel.level + 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("Agla Meetha Level! ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("🎉 Swaad-tastic Victory!", fontWeight = FontWeight.Bold) },
            text = { Text("Mithaas AI ne humein level bypass karwaya! Aapka score ${viewModel.score} target completed hai. 3 Golden Stars standard achievements me record ho gaye!") },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (viewModel.isGameOver) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = { viewModel.startLevel(viewModel.level) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("Retry Level 🔄", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.startLevel(1) }) {
                    Text("Reset to level 1", color = Color.Gray)
                }
            },
            title = { Text("😞 Target adhura reh gaya!", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252)) },
            text = { Text("Sugar structure broke down before goals could be met! AI companion parameters update karke firse play karein.") },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ==========================================
// INTERACTIVE AI BENTO STRATEGIST COMPANION
// ==========================================
@Composable
fun BentoAiStrategistCard(
    viewModel: GameViewModel,
    onPurchaseClicked: () -> Unit
) {
    var activeAiTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("💡 Hints", "🧠 Tactics", "🍬 Riddles")

    // Check if locked
    val isPremium = viewModel.isAiPremiumUnlocked
    val isTrialUnlocked = viewModel.freeTrialsCount > 0
    val hasUnusedTrial = !isPremium && !isTrialUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)), // Light Purple Bento Item
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "AI face",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mithaas strategist",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                }

                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFF21005D))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("👑 LIFETIME UNLOCKED", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else if (isTrialUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFF7B1FA2))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("⚡ FREE TRIAL ENLIGHTENED", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFFE91E63))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("🔒 PREMIUM $2", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isActive = activeAiTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isActive) Color(0xFF21005D) else Color(0x1F21005D))
                            .clickable {
                                activeAiTab = index
                                // Automatically pull if unlocked
                                if (isPremium || isTrialUnlocked) {
                                    when (index) {
                                        0 -> viewModel.requestAiMoveHint(isTrialRun = isTrialUnlocked)
                                        1 -> viewModel.requestAiTactics(isTrialRun = isTrialUnlocked)
                                        2 -> viewModel.requestAiRiddle(isTrialRun = isTrialUnlocked)
                                    }
                                }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isActive) Color.White else Color(0xFF21005D).copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body rendering (either Paywall or AI Output)
            if (!isPremium && !isTrialUnlocked) {
                // PAYWALL INTERACTIVE DISPLAY CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💡 Unlock AI strategics Room!",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF21005D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Expert Hinglish guides, board candy highlights aur customized Indian desserts ki Riddles open karein deep AI modules se.",
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Free Trial button
                            if (hasUnusedTrial) {
                                Button(
                                    onClick = { viewModel.useFreeTrialAndExecute(activeAiTab) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F6750A4)),
                                    border = BorderStroke(1.dp, Color(0xFF6750A4)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("Free Trial 🎁", color = Color(0xFF6750A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 2$ Lifetime Purchase Button
                            Button(
                                onClick = onPurchaseClicked,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1.2f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Premium Unlock ($2)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // AI COMPANION ACTIVE COMPONENT DISPLAY
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .padding(12.dp)
                    ) {
                        when (val state = viewModel.aiState) {
                            is AiState.Idle -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = {
                                            when (activeAiTab) {
                                                0 -> viewModel.requestAiMoveHint(isTrialRun = isTrialUnlocked)
                                                1 -> viewModel.requestAiTactics(isTrialRun = isTrialUnlocked)
                                                2 -> viewModel.requestAiRiddle(isTrialRun = isTrialUnlocked)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Consult Gemini Mithaas Guru", color = Color.White, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Click karkey Indian dessert strategist ki guidance lein!",
                                        fontSize = 10.sp,
                                        color = Color.LightGray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            is AiState.Loading -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF6750A4))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Generating spicy dessert tactics...", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            is AiState.Success -> {
                                val textScroll = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(textScroll)
                                ) {
                                    Text(
                                        text = state.text,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF1D1B20)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                            }
                            is AiState.Error -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Riddle/Tactic fetching hit a snag.", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    Text(state.message, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// FORGE TAB CONTENT: Skins Editor & Visual Builder
// ==========================================
@Composable
fun ForgeSkinsTabContent(
    viewModel: GameViewModel,
    onTriggerPurchase: () -> Unit
) {
    var customPrompt by remember { mutableStateOf("") }
    var generationState by remember { mutableStateOf<String?>(null) }
    var makingSkin by remember { mutableStateOf(false) }

    val coroutine = rememberCoroutineScope()
    val isPremium = viewModel.isAiPremiumUnlocked

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BentoHeader(subheading = "Personalization Studio", title = "AI Skin Forge")

        // Main dark Bento segment: The Forge Studio
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)), // Dark Bento color
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFFEADDFF).copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Stable Diffusion Sweet Gen", color = Color(0xFFD0BCFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Aapke manpasand sweets aur shapes design karein. Input your favorite flavour and watch Gemini AI construct sweet textures!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!isPremium) {
                    // Paywall blocking custom text input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Skins Forge requires Premium!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Get custom skin design capability for $2.00 only.", color = Color.White.copy(0.6f), fontSize = 10.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onTriggerPurchase,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Text("Unlock Skins Forge ($2.00)", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    // TEXT PROMPT INPUT FOR PREMIUM USERS
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        placeholder = { Text("E.g. Rose Petal Kaju Katli, Golden Rasgulla", color = Color.White.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (customPrompt.isNotBlank()) {
                                coroutine.launch {
                                    makingSkin = true
                                    delay(1500) // Mock stable diffusion lag
                                    generationState = "Generated: Premium Skin Texture for \"${customPrompt}\" initialized on Board! 💎🍬"
                                    makingSkin = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        if (makingSkin) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                        } else {
                            Text("FORGE SKIN ASSETS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                if (generationState != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF).copy(0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = generationState ?: "",
                            color = Color(0xFFD0BCFF),
                            modifier = Modifier.padding(10.dp),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset items
        Text(
            text = "PRESET SKINS DISCOVER",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(vertical = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                "🌸 Royal Rose" to "ffffd8e4",
                "🥇 Gold Foil" to "fffff9c4",
                "❄️ Ice Menthol" to "ffe0f2f1"
            ).forEach { (name, hex) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            generationState = "Active Theme: $name successfully deployed on grid sweets!"
                        }
                        .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(android.graphics.Color.parseColor("#$hex")))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1D1B20))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ==========================================
// EVENTS TAB CONTENT: Leaderboards & Stats
// ==========================================
@Composable
fun EventsLeaderboardTabContent(
    viewModel: GameViewModel
) {
    val statsState by viewModel.playerStats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BentoHeader(subheading = "Real-time Metrics", title = "Events & Leaderboards")

        // 2 Col Stats Bento Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rank", fontSize = 10.sp, color = Color(0xFF21005D), fontWeight = FontWeight.Bold)
                        Text("#12", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF21005D))
                    }
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF21005D), modifier = Modifier.size(24.dp))
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE1BEE7), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Stars", fontSize = 10.sp, color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold)
                        Text("${statsState?.totalStars ?: 0}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF7B1FA2))
                    }
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Event card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFFB300), RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔴 LIVE SPEED QUEST", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text("Ends in 3h", color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Mithaas Holi Fiesta Challenge", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1D1B20))
                Text("Pop 100 Gulab Jamuns globally to receive premium skin designs aur visual badge frames directly!", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                val popCount = statsState?.totalSweetsMatched ?: 0
                val progress = (popCount.toFloat() / 100f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF6750A4),
                    trackColor = Color(0x1F6750A4)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$popCount/100 sweets popped", fontSize = 10.sp, color = Color.Gray)
                    Text("${(progress * 100).toInt()}% Done", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// LAB TAB CONTENT: Advanced Sandbox
// ==========================================
@Composable
fun ArtificialLabTabContent(
    viewModel: GameViewModel
) {
    var textMessage by remember { mutableStateOf<String?>(null) }
    var levelMultiplier by remember { mutableStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BentoHeader(subheading = "AI Engine Devs", title = "AI Lab & Configs")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE6E1E5), RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Developer Tweak Sandbox", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF6750A4))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Change difficulty rates aur state models dynamic parameters immediately inside the AI match engine layers.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Level Difficulty Mutator: x${String.format("%.1f", levelMultiplier)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                Slider(
                    value = levelMultiplier,
                    onValueChange = { levelMultiplier = it },
                    valueRange = 1f..5f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF6750A4),
                        activeTrackColor = Color(0xFF6750A4)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        textMessage = "Custom Lab Level initialized with x${String.format("%.1f", levelMultiplier)} multiplier! Target score synchronized."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Mutator on Board", color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.resetGameProgress()
                        textMessage = "DB cleared! Premium status AND all active levels reset back."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Full Clean DB Reset", color = Color.White)
                }

                if (textMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            textMessage ?: "",
                            color = Color(0xFF7B1FA2),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// REUSABLE SUB-COMPONENTS
// ==========================================
@Composable
fun BentoHeader(
    subheading: String,
    title: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp)
    ) {
        Text(
            text = subheading.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6750A4),
            letterSpacing = 1.5.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1D1B20)
            )
            // Custom App Brand Logo
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Mithaas Match Logo",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFFD0BCFF), CircleShape)
            )
        }
    }
}

@Composable
fun BentoBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF3EDF7),
        modifier = Modifier.height(80.dp)
    ) {
        listOf(
            Triple(0, "Play", Icons.Default.PlayArrow),
            Triple(1, "Forge", Icons.Default.Add),
            Triple(2, "Events", Icons.Default.List),
            Triple(3, "Lab", Icons.Default.Settings)
        ).forEach { (idx, label, icon) ->
            val active = selectedTab == idx
            NavigationBarItem(
                selected = active,
                onClick = { onTabSelected(idx) },
                icon = {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) Color(0xFFE8DEF8) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = label, tint = if (active) Color(0xFF1D192B) else Color.Gray)
                    }
                },
                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (active) Color(0xFF1D192B) else Color.Gray) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun BentoSweetCell(
    sweet: MithaasSweet?,
    isSelected: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    if (sweet == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF8FF).copy(0.4f))
        )
        return
    }

    val type = sweet.type
    val defaultBaseColor = Color(type.colorCode)

    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f)
    )

    val customBorderBrush = if (isSelected) {
        Brush.sweepGradient(listOf(Color(0xFFD04BD3), Color(0xFFE91E63), Color(0xFFD04BD3)))
    } else if (isHighlighted) {
        Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF), Color(0xFF00E5FF)))
    } else {
        null
    }

    val borderWidth = if (isSelected || isHighlighted) 3.dp else 1.dp
    val borderColor = if (isSelected) Color(0xFFD04BD3) else if (isHighlighted) Color(0xFF00E5FF) else Color(0x3B6750A4)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scaleAnim)
            .shadow(
                elevation = if (isSelected) 6.dp else 0.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(defaultBaseColor.copy(alpha = 0.2f))
            .border(
                width = borderWidth,
                brush = customBorderBrush ?: Brush.linearGradient(listOf(borderColor, borderColor)),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = type.emoji,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = type.displayName,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20).copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
