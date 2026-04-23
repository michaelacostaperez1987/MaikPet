package com.macosta.maikpet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.macosta.maikpet.ui.theme.*
import kotlinx.coroutines.delay

class IntroActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PetTheme {
                IntroScreen()
            }
        }
    }
}

@Composable
fun IntroScreen() {
    var showContent by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(0.8f) }
    var opacity by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Animación de entrada
    LaunchedEffect(Unit) {
        // Animación de escala y rotación inicial
        scale = 0.8f
        opacity = 0f
        rotation = -10f
        
        // Esperar un momento antes de animar
        delay(300)
        
        // Animación de entrada con bounce
        scale = 1.15f
        opacity = 1f
        rotation = 5f
        delay(150)
        
        scale = 0.95f
        rotation = -2f
        delay(100)
        
        scale = 1f
        rotation = 0f
        delay(100)
        
        // Mostrar contenido completo
        showContent = true
        
        // Animación de progreso
        val progressDuration = 1500L // 1.5 segundos para la barra de progreso
        val steps = 30
        val stepDelay = progressDuration / steps
        
        repeat(steps) { step ->
            progress = (step + 1) / steps.toFloat()
            delay(stepDelay)
        }
        
        // Pequeña pausa antes de navegar
        delay(300)
        
        // Navegar a LoginActivity
        context.startActivity(Intent(context, LoginActivity::class.java))
        (context as? IntroActivity)?.finish()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF007AFF),  // iOS Blue
                        Color(0xFF34C759),  // iOS Green
                        Color(0xFF5AC8FA)   // Light Blue
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Fondo con elementos decorativos animados
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Elementos decorativos circulares animados
            AnimatedCircles()
            
            // Partículas flotantes
            FloatingParticles()
        }
        
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/icono animado con brillo
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.8f)
                            ),
                            center = Offset(0.3f, 0.3f),
                            radius = 150f
                        )
                    )
                    .scale(scale)
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                // Brillo interno
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = Offset(0.7f, 0.3f),
                                radius = 100f
                            )
                        )
                )
                
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "MaikPet Logo",
                    modifier = Modifier.size(140.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Título con animación de fade in y sombra
            Text(
                text = "MaikPet",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 56.sp,
                modifier = Modifier
                    .alpha(opacity)
                    .scale(scale)
                    .graphicsLayer {
                        shadowElevation = 8.dp.toPx()
                        shape = androidx.compose.ui.graphics.RectangleShape
                        clip = true
                    }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Subtítulo con animación escalonada
            if (showContent) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Encuentra a tu compañero perfecto",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.95f),
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Adopciones responsables 🐾",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Indicador de carga animado
            if (showContent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color.White.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Cargando...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        
        // Texto inferior con animación
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 30 }),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Text(
                    text = "Conectando corazones con patitas",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Text(
                            text = "🐾",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Extensión para alpha (transparencia) - versión simplificada
fun Modifier.alpha(alpha: Float): Modifier = this.graphicsLayer { this.alpha = alpha }

@Composable
fun AnimatedCircles() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val circle1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val circle2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val circle3Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Círculo 1 - arriba derecha
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size((300.dp.value * circle1Scale).dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
        
        // Círculo 2 - abajo izquierda
        Box(
            modifier = Modifier
                .size((200.dp.value * circle2Scale).dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 100.dp)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )
        
        // Círculo 3 - centro izquierda
        Box(
            modifier = Modifier
                .size((150.dp.value * circle3Scale).dp)
                .align(Alignment.CenterStart)
                .offset(x = (-80).dp, y = (-50).dp)
                .background(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun FloatingParticles() {
    val particles = listOf(
        Particle(offset = Offset(0.2f, 0.3f), size = 8.dp, duration = 4000L),
        Particle(offset = Offset(0.7f, 0.1f), size = 12.dp, duration = 5000L),
        Particle(offset = Offset(0.4f, 0.8f), size = 6.dp, duration = 3500L),
        Particle(offset = Offset(0.9f, 0.6f), size = 10.dp, duration = 4500L),
        Particle(offset = Offset(0.1f, 0.9f), size = 7.dp, duration = 3800L),
    )
    
    particles.forEach { particle ->
        FloatingParticle(particle = particle)
    }
}

data class Particle(
    val offset: Offset,
    val size: androidx.compose.ui.unit.Dp,
    val duration: Long
)

@Composable
fun FloatingParticle(particle: Particle) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(particle.duration.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween((particle.duration / 2).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .offset(
                x = (particle.offset.x * LocalConfiguration.current.screenWidthDp).dp,
                y = (particle.offset.y * LocalConfiguration.current.screenHeightDp).dp + yOffset.dp
            )
            .size(particle.size)
            .background(
                color = Color.White.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}