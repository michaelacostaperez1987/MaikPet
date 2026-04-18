package com.macosta.maikpet.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.macosta.maikpet.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PrivacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PetTheme {
                PrivacyScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Volver", 
                            tint = OnPrimary
                        )
                    }
                    Text(
                        text = "Política de Privacidad",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Título principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 40.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "POLÍTICA DE PRIVACIDAD",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Última actualización: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contenido
            val sections = listOf(
                "1. INFORMACIÓN QUE RECOPILAMOS" to "Recopilamos la siguiente información:\n\n• Datos de registro: nombre, email, teléfono, dirección\n• Contenido que publicas: fotos y descripción de mascotas\n• Información de uso: cómo interactúas con la app",
                "2. USO DE LA INFORMACIÓN" to "Utilizamos tu información para:\n\n• Gestionar tu cuenta de usuario\n• Publicar mascotas en adopción\n• Contactarte sobre posibles adoptantes\n• Mejorar nuestros servicios\n• Resolver problemas técnicos",
                "3. COMPARTIR INFORMACIÓN" to "No vendemos ni compartimos tu información personal con terceros, EXCEPTO cuando sea necesario para:\n\n• Coordinar adopciones (mostrar tu contacto a interesados)\n• Cumplir con requerimientos legales\n• Proteger nuestros derechos y seguridad",
                "4. SEGURIDAD" to "Implementamos medidas de seguridad para proteger tu información:\n\n• Conexiones cifradas (SSL)\n• Almacenamiento seguro de datos\n• Acceso restringido a información personal\n• Monitoreo continuo de seguridad",
                "5. TUS DERECHOS" to "Tienes derecho a:\n\n• Acceder a tus datos personales\n• Corregir información inexacta\n• Solicitar la eliminación de tu cuenta\n• Exportar tus datos\n• Opt-out de comunicaciones\n\nPara ejercer estos derechos, contactanos a soporte@lmcosturas.com",
                "6. CONTACTO" to "Si tienes preguntas sobre esta Política de Privacidad, puedes contactarnos:\n\n📧 soporte@lmcosturas.com\n\nResponderemos a tu consulta en un plazo de 48 horas."
            )

            sections.forEach { (title, content) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackground,
                            textAlign = TextAlign.Justify
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Al utilizar MaikPet, confirmas haber leído y entendido esta Política de Privacidad.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}