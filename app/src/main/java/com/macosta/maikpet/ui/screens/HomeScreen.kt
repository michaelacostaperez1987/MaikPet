package com.macosta.maikpet.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.macosta.maikpet.ui.components.AdBannerSmall
import com.macosta.maikpet.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onStartClick: () -> Unit
) {
    var currentBanner by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentBanner = (currentBanner + 1) % 4
        }
    }
    
    val features = listOf(
        FeatureItem(
            icon = Icons.Default.Map,
            title = "Mapa Interactivo",
            description = "Encuentra mascotas cerca de ti en tiempo real"
        ),
        FeatureItem(
            icon = Icons.Default.Pets,
            title = "Adopción Gratuita",
            description = "Conecta con personas que buscan dar amor a sus mascotas"
        ),
        FeatureItem(
            icon = Icons.Default.Chat,
            title = "Contacto Directo",
            description = "Chatea por WhatsApp con los adoptantes"
        ),
        FeatureItem(
            icon = Icons.Default.Security,
            title = "100% Seguro",
            description = "Validado contra ventas y cruzas"
        )
    )
    
    val banners = listOf(
        BannerItem("🐕", "Encuentra tu compañero", "Perros adorables buscando un hogar", Color(0xFF4CAF50)),
        BannerItem("🐈", "Gatos en adopción", "Ronroneos esperándote", Color(0xFF2196F3)),
        BannerItem("❤️", "Dale una segunda oportunidad", "Cada mascota merece amor", Color(0xFFE91E63)),
        BannerItem("🏠", "Adopta, no compres", "Un nuevo amigo te espera", Color(0xFFFF9800))
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Background, Surface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary, PrimaryVariant)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐾", fontSize = 60.sp)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "MaikPet",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            
            Text(
                text = "Adopciones",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    banners[currentBanner].color,
                                    banners[currentBanner].color.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = banners[currentBanner].emoji,
                            fontSize = 80.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = banners[currentBanner].title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = banners[currentBanner].subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (currentBanner == index) 10.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentBanner == index) Primary
                                else TextSecondary.copy(alpha = 0.5f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "¿Cómo funciona?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            features.forEachIndexed { index, feature ->
                FeatureCard(feature = feature, index = index)
                if (index < features.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    onStartClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = OnPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comenzar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "🎉 Adopciones 100% gratuitas",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Banner publicitario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                AdBannerSmall()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureCard(feature: FeatureItem, index: Int) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = feature.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

private data class FeatureItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)

private data class BannerItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val color: Color
)
