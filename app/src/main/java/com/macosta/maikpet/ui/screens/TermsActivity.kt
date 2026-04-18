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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.macosta.maikpet.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TermsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PetTheme {
                TermsScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
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
                        text = "Términos y Condiciones",
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
                        text = "📋",
                        fontSize = 40.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TÉRMINOS Y CONDICIONES",
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
                "1. ACEPTACIÓN DE LOS TÉRMINOS" to "Al acceder y utilizar la aplicación MaikPet, aceptas cumplir con estos términos y condiciones. Si no estás de acuerdo con alguno de estos términos, por favor no utilices la aplicación.",
                "2. USO DE LA APLICACIÓN" to "MaikPet es una plataforma para la adopción de mascotas. Los usuarios pueden:\n• Publicar mascotas para adopción\n• Buscar mascotas disponibles\n• Contactar con posibles adoptantes\n• Gestionar sus publicaciones",
                "3. RESPONSABILIDADES DEL USUARIO" to "• Eres responsable de la veracidad de la información que publicas\n• Debes garantizar el bienestar de las mascotas que ofreces en adopción\n• No puedes publicar contenido falso o engañoso\n• Debes responder a las consultas de los interesados",
                "4. PRIVACIDAD" to "Tu información personal será tratada conforme a nuestra Política de Privacidad. No compartiremos tus datos con terceros sin tu consentimiento, excepto cuando sea necesario para coordinar adopciones.",
                "5. MODIFICACIONES" to "Nos reservamos el derecho de modificar estos términos en cualquier momento. Las modificaciones entrarán en vigor desde su publicación en la aplicación.",
                "6. CONTACTO" to "Para consultas, sugerencias o problemas relacionados con la aplicación, puedes contactarnos a:\n\n📧 soporte@lmcosturas.com"
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

            // Botón de aceptación
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Al registrarte en MaikPet, confirmas haber leído y aceptar estos términos y condiciones.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}