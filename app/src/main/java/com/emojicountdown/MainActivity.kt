package com.emojicountdown

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emojicountdown.ui.theme.EmojicountdownTheme
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

private val Number3 = listOf(
    0 to 0, 1 to 0, 2 to 0, 3 to 0, 3 to 1, 1 to 2, 2 to 2, 3 to 2, 3 to 3, 0 to 4, 1 to 4, 2 to 4, 3 to 4, 3 to 0, 3 to 4
)
private val Number2 = listOf(
    0 to 0, 1 to 0, 2 to 0, 3 to 0, 3 to 1, 0 to 2, 1 to 2, 2 to 2, 3 to 2, 0 to 3, 0 to 4, 1 to 4, 2 to 4, 3 to 4, 0 to 0
)
private val Number1 = listOf(
    2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4, 1 to 1, 1 to 4, 2 to 4, 3 to 4, 2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4, 2 to 2
)
private val Number0 = listOf(
    0 to 0, 1 to 0, 2 to 0, 3 to 0, 0 to 1, 3 to 1, 0 to 2, 3 to 2, 0 to 3, 3 to 3, 0 to 4, 1 to 4, 2 to 4, 3 to 4, 1 to 1
)

enum class Screen {
    VIBE_ENTRY, GAME
}

object Presets {
    fun explode(x: Double = 0.5, y: Double = 0.5): Party = Party(
        speed = 15f,
        maxSpeed = 35f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xFF4285F4.toInt(), 0xFF34A853.toInt(), 0xFFFBBC05.toInt(), 0xFFEA4335.toInt()),
        position = Position.Relative(x, y),
        emitter = Emitter(duration = 150, TimeUnit.MILLISECONDS).max(30)
    )
}

class MainActivity : ComponentActivity() {
    private var popSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            val resId = resources.getIdentifier("pop", "raw", packageName)
            if (resId != 0) {
                popSound = MediaPlayer.create(this, resId)
            }
        } catch (e: Exception) {}

        setContent {
            EmojicountdownTheme {
                EmojiGameApp(popSound)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        popSound?.release()
        popSound = null
    }
}

@Composable
fun EmojiGameApp(popSound: MediaPlayer?) {
    var currentScreen by remember { mutableStateOf(Screen.VIBE_ENTRY) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF020205)
    ) {
        when (currentScreen) {
            Screen.VIBE_ENTRY -> {
                VibeEntryScreen(onFinished = {
                    currentScreen = Screen.GAME
                })
            }
            Screen.GAME -> {
                GameScreen(popSound)
            }
        }
    }
}

@Composable
fun VibeEntryScreen(onFinished: () -> Unit) {
    var countdownValue by remember { mutableIntStateOf(3) }
    val infiniteTransition = rememberInfiniteTransition(label = "vibe")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )

    val driftState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Restart),
        label = "globalDrift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020205))
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Arc Background - Matching the Logo Image
        Box(modifier = Modifier.fillMaxSize()) {
            val colors = listOf(Color(0xFFFFD54F), Color(0xFFE53935), Color(0xFF4285F4), Color(0xFF4CAF50), Color.White)
            repeat(18) { i ->
                val angle = (i * 20) - 170.0 // Semi-circle arc
                val radius = 180.dp
                val xOffset = sin(angle * PI / 180.0).toFloat() * radius.value
                val yOffset = -cos(angle * PI / 180.0).toFloat() * radius.value - 60f
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = xOffset.dp, y = yOffset.dp)
                        .size(16.dp, 10.dp)
                        .graphicsLayer { 
                            rotationZ = angle.toFloat() + 90f
                            alpha = 0.7f
                        }
                        .background(colors[i % colors.size])
                )
            }
        }

        // Logo Layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            // Sunglasses 😎 + Star + Heart
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 0.dp)) {
                // Radial Glow behind Emoji
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            alpha = 0.5f
                        }
                        .drawBehind {
                            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFD54F).copy(0.8f), Color.Transparent)))
                        }
                )
                Text(
                    "😎",
                    fontSize = 150.sp,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = sin(driftState.value.toDouble()).toFloat() * 6f
                    }
                )
                // Star and Heart positioned like the image
                Text("⭐", fontSize = 55.sp, modifier = Modifier.align(Alignment.CenterStart).offset(x = (-95).dp, y = 25.dp))
                Text("❤️", fontSize = 55.sp, modifier = Modifier.align(Alignment.CenterEnd).offset(x = 95.dp, y = 25.dp))
            }

            Text(
                "EMOJI",
                color = Color(0xFFFFEB3B),
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                fontSize = 90.sp,
                modifier = Modifier.offset(y = (-15).dp)
            )

            // COUNTDOWN in Green Banner Style
            Surface(
                color = Color(0xFF43A047),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(5.dp, Color(0xFF1B5E20)),
                shadowElevation = 12.dp,
                modifier = Modifier.offset(y = (-25).dp)
            ) {
                Text(
                    "COUNTDOWN",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Particles
            Box(modifier = Modifier.size(220.dp, 160.dp), contentAlignment = Alignment.Center) {
                val currentPoints = when (countdownValue) {
                    3 -> Number3
                    2 -> Number2
                    1 -> Number1
                    else -> Number0
                }
                repeat(15) { index ->
                    val point = currentPoints[index]
                    VibeParticle(
                        emoji = "✨",
                        targetX = (point.first * 44).dp,
                        targetY = (point.second * 44).dp,
                        isGlowing = true,
                        index = index,
                        driftState = driftState
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(
                if (countdownValue > 0) "GET READY!" else "GO!",
                color = if (countdownValue > 0) Color.White.copy(0.6f) else Color(0xFFFFEB3B),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )
        }

        LaunchedEffect(Unit) {
            delay(500)
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            delay(500)
            onFinished()
        }
    }
}


@Composable
fun VibeParticle(
    emoji: String,
    targetX: Dp,
    targetY: Dp,
    isGlowing: Boolean,
    index: Int,
    driftState: State<Float>
) {
    val scale by animateFloatAsState(
        targetValue = if (isGlowing) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), label = "scale"
    )
    val animX by animateDpAsState(targetX, spring(stiffness = Spring.StiffnessLow), label = "x")
    val animY by animateDpAsState(targetY, spring(stiffness = Spring.StiffnessLow), label = "y")

    Box(
        modifier = Modifier
            .graphicsLayer {
                val d = driftState.value
                val drift = if (d.isNaN()) 0f else d
                val tx = animX.toPx() + sin((drift + index).toDouble()).toFloat() * 10.dp.toPx()
                val ty = animY.toPx() + sin(((drift + index) * 1.1f).toDouble()).toFloat() * 10.dp.toPx()
                translationX = if (tx.isNaN()) 0f else tx
                translationY = if (ty.isNaN()) 0f else ty
                val s = if (scale.isNaN()) 1f else scale.coerceAtLeast(0.01f)
                scaleX = s
                scaleY = s
            }
    ) {
        Text(text = emoji, fontSize = 40.sp)
    }
}

@Composable
fun GameScreen(popSound: MediaPlayer?) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var gameSession by remember { mutableIntStateOf(0) }
    
    val gameEmojis = listOf("😎", "🍕", "💩", "💣", "👻", "❤️", "😘", "😂", "⭐", "🔥")
    
    fun generateRound(): Triple<List<String>, List<String>, List<String>> {
        val shuffled = gameEmojis.shuffled()
        val roundTargets = shuffled.take(3)
        // Ensure at least one target is in options
        val correctEmoji = roundTargets.random()
        val otherOptions = (gameEmojis - roundTargets).shuffled().take(2)
        val roundOptions = (otherOptions + correctEmoji).shuffled()
        val roundFloating = List(6) { gameEmojis.random() }
        return Triple(roundTargets, roundOptions, roundFloating)
    }

    var roundData by remember { mutableStateOf(generateRound()) }
    val targets by remember { derivedStateOf { roundData.first } }
    val options by remember { derivedStateOf { roundData.second } }
    val floatingEmojis by remember { derivedStateOf { roundData.third } }
    var showFeedback by remember { mutableStateOf<String?>(null) }
    var parties by remember { mutableStateOf<List<Party>>(emptyList()) }

    val infiniteTransition = rememberInfiniteTransition(label = "game")
    val skyColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF4FC3F7),
        targetValue = Color(0xFF81D4FA),
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "sky"
    )

    // Clock Logic - Keyed to gameSession to restart correctly
    LaunchedEffect(gameSession) {
        timeLeft = 30
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    LaunchedEffect(showFeedback) {
        if (showFeedback != null) {
            delay(1000)
            showFeedback = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(skyColor, Color(0xFFE1F5FE), Color(0xFFC8E6C9), Color(0xFF81C784))
                )
            )
    ) {
        // 1. Confetti Layer (Behind UI to prevent touch blocking)
        KonfettiView(modifier = Modifier.fillMaxSize(), parties = parties)

        // 2. Environment Layer
        Box(modifier = Modifier.fillMaxSize()) {
            Text("☁️", fontSize = 60.sp, modifier = Modifier.offset(x = 40.dp, y = 100.dp).alpha(0.6f))
            Text("☁️", fontSize = 80.sp, modifier = Modifier.offset(x = 250.dp, y = 150.dp).alpha(0.5f))
            Text("⛰️", fontSize = 120.sp, modifier = Modifier.align(Alignment.BottomStart).offset(y = (-140).dp).alpha(0.3f))
            Text("🌳", fontSize = 60.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-20).dp, y = (-140).dp).alpha(0.4f))
        }

        // 3. UI Layer
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF2196F3), shadowElevation = 6.dp) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("$score", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
                Surface(shape = RoundedCornerShape(24.dp), color = if (timeLeft < 10) Color(0xFFD32F2F) else Color(0xFFE53935), shadowElevation = 6.dp) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏱️", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("$timeLeft", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(4.dp, Color.White.copy(alpha = 0.6f)),
                shadowElevation = 8.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                    Text("FIND THESE!", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        targets.forEach { emoji -> Text(emoji, fontSize = 36.sp) }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (timeLeft > 0) {
                    floatingEmojis.forEachIndexed { index, emoji ->
                        val animOffset by infiniteTransition.animateFloat(
                            initialValue = 0f, targetValue = 30f,
                            animationSpec = infiniteRepeatable(tween(1200 + index * 180), RepeatMode.Reverse),
                            label = "float"
                        )
                        Text(
                            emoji, fontSize = 58.sp,
                            modifier = Modifier.offset {
                                val tx = (40 + (index % 3) * 110).dp.toPx()
                                val ty = (60 + (index / 3) * 150).dp.toPx() + animOffset.dp.toPx()
                                IntOffset(
                                    if (tx.isNaN()) 0 else tx.toInt(),
                                    if (ty.isNaN()) 0 else ty.toInt()
                                )
                            }
                        )
                    }
                }

                if (showFeedback != null) {
                    val isCorrect = targets.contains(showFeedback)
                    val scale by animateFloatAsState(targetValue = 1.3f, animationSpec = spring(dampingRatio = 0.4f), label = "fb")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center).graphicsLayer {
                            scaleX = if (scale.isNaN()) 1f else scale
                            scaleY = if (scale.isNaN()) 1f else scale
                        }
                    ) {
                        Surface(shape = CircleShape, color = Color.White, shadowElevation = 12.dp, modifier = Modifier.size(110.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(showFeedback!!, fontSize = 65.sp) }
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp), 
                            color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD32F2F), 
                            modifier = Modifier.padding(top = 12.dp), 
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                if (isCorrect) "GREAT!" else "WRONG!", 
                                color = Color.White, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 28.sp, 
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (timeLeft <= 0) {
                Surface(shape = RoundedCornerShape(24.dp), color = Color.Black.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 20.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("GAME OVER 🎉", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                        Text("Score: $score", color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                score = 0
                                gameSession++ // Restarts timer and state
                                roundData = generateRound()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)), shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("PLAY AGAIN", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Wooden Bottom Shelf
            Surface(
                modifier = Modifier.fillMaxWidth().height(170.dp).drawBehind {
                    // Rich wood gradient
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFF3E2723))))
                    
                    // Procedural wood grain
                    for (i in 0..20) {
                        val yPos = i * 25f
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, yPos)
                            quadraticTo(size.width * 0.5f, yPos + (if (i % 2 == 0) 15f else -15f), size.width, yPos + 5f)
                        }
                        drawPath(
                            path = path,
                            color = Color.Black.copy(alpha = 0.12f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                    }
                    
                    // Bevel effect
                    drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, 4f), Offset(size.width, 4f), 6f)
                    drawLine(Color.Black.copy(alpha = 0.4f), Offset(0f, 0f), Offset(size.width, 0f), 2f)
                },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), color = Color.Transparent
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 25.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        options.forEach { emoji ->
                            Surface(
                                onClick = {
                                    if (timeLeft > 0) {
                                        showFeedback = emoji
                                        if (targets.contains(emoji)) {
                                            score += 50
                                            try { popSound?.start() } catch (e: Exception) {}
                                            parties = parties + Presets.explode(0.5, 0.85)
                                            roundData = generateRound()
                                        } else {
                                            score = (score - 20).coerceAtLeast(0)
                                            // Optional: play a different sound for "wrong"
                                        }
                                    }
                                },
                                shape = CircleShape, 
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.size(85.dp),
                                shadowElevation = 10.dp,
                                border = BorderStroke(4.dp, Color(0xFFF57F17))
                            ) { Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 52.sp) } }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1B5E20).copy(alpha = 0.85f)) {
                        Text("MATCH TO SCORE!", color = Color.White, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(10.dp).fillMaxWidth(), letterSpacing = 3.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VibePreview() {
    EmojicountdownTheme { EmojiGameApp(null) }
}
