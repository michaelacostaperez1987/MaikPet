package com.example.pet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Términos y Condiciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = OnBackground
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "TÉRMINOS Y CONDICIONES DE USO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Última actualización: 9/4/2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Border)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Bienvenido a MaikPet. Al utilizar esta app, aceptás los siguientes términos:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnBackground
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    TerminoItem(
                        numero = "1",
                        titulo = "Objeto",
                        contenido = "La aplicación tiene como finalidad conectar personas interesadas en dar en adopción o adoptar animales. La plataforma actúa únicamente como intermediaria entre usuarios."
                    )
                    
                    TerminoItem(
                        numero = "2",
                        titulo = "Responsabilidad",
                        contenido = "La aplicación no participa en el proceso de adopción ni garantiza:\n• El estado de salud del animal\n• La veracidad de las publicaciones\n• El comportamiento de los usuarios\n\nCada usuario es responsable de verificar la información antes de concreto una adopción."
                    )
                    
                    TerminoItem(
                        numero = "3",
                        titulo = "Publicaciones",
                        contenido = "El usuario se compromete a:\n• Publicar información veraz\n• No ofrecer animales en venta (solo adopción)\n• No publicar contenido ilegal o engañoso\n\nLa app se reserva el derecho de eliminar publicaciones sin previo aviso."
                    )
                    
                    TerminoItem(
                        numero = "4",
                        titulo = "Conducta del usuario",
                        contenido = "Queda prohibido:\n• Usar la app para estafas o actividades ilegales\n• Maltratar animales\n• Publicar contenido ofensivo"
                    )
                    
                    TerminoItem(
                        numero = "5",
                        titulo = "Eliminación de cuentas",
                        contenido = "La app puede suspender o eliminar cuentas que incumplan estos términos."
                    )
                    
                    TerminoItem(
                        numero = "6",
                        titulo = "Modificaciones",
                        contenido = "Los términos pueden cambiar en cualquier momento. Se recomienda revisarlos periódicamente."
                    )
                    
                    TerminoItem(
                        numero = "7",
                        titulo = "Aceptación",
                        contenido = "El uso de la aplicación implica la aceptación de estos términos."
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Border)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "🐾 Adopta, no compres 🐾",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun TerminoItem(
    numero: String,
    titulo: String,
    contenido: String
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Primary, RoundedCornerShape(14.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = numero,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contenido,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}
