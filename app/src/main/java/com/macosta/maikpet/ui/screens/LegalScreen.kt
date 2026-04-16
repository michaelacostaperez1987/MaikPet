package com.macosta.maikpet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.macosta.maikpet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    onBack: () -> Unit = {},
    showAsInitial: Boolean = false,
    onAccept: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var mostrarTyC by remember { mutableStateOf(true) }
    var mostrarPrivacidad by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            mostrarPrivacidad -> "Política de Privacidad"
                            mostrarTyC -> "Términos y Condiciones"
                            else -> "Información Legal"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (mostrarTyC || mostrarPrivacidad) {
                            if (showAsInitial) {
                                mostrarTyC = false
                                mostrarPrivacidad = false
                            } else {
                                onBack()
                            }
                        }
                    }) {
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
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!mostrarTyC && !mostrarPrivacidad) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Documentos Legales",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Antes de usar MaikPet, debes leer y aceptar nuestros documentos legales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarTyC = true },
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Términos y Condiciones",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnBackground,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Normas de uso de la aplicación",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarPrivacidad = true },
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Política de Privacidad",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = OnBackground,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cómo protegemos tus datos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (onAccept != null) {
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { onAccept() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Text(
                                text = "Continuar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (mostrarTyC) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    TyCContent()
                }
            } else if (mostrarPrivacidad) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    PrivacidadContent()
                }
            }
        }
    }
}

@Composable
private fun TyCContent() {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TÉRMINOS Y CONDICIONES",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Última actualización: 16/04/2026",
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

                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    "1. Uso de la app" to "MaikPet es una plataforma para facilitar la adopción responsable de animales. Uso indebido resulta en suspensión.",
                    "2. Registro" to "Debés proporcionar información veraz. Sos responsable de tu cuenta.",
                    "3. Publicación de mascotas" to "Solo podés publicar animales que sean tuyos o de tu cuidado.",
                    "4. Conducta" to "Respetá a otros usuarios. No se permiten animales en adopción irregular.",
                    "5. Responsabilidad" to "MaikPet actúa como intermediario. No garantizamos el comportamiento de terceros.",
                    "6. Datos y privacidad" to "Tratamos tus datos según la Política de Privacidad vigente.",
                    "7. Modificaciones" to "Podemos modificar estos términos. Te notificaremos de cambios importantes."
                ).forEach { (titulo, desc) ->
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun PrivacidadContent() {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "POLÍTICA DE PRIVACIDAD",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Última actualización: 16/04/2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    "Datos que recolectamos" to "Nombre, email, teléfono, dirección, edad e interacciones con la app.",
                    "Finalidad" to "Conectar adopters con animales en adopción. Gestionar tu cuenta.",
                    "Retención" to "Datos se eliminan automáticamente después de 90 días desde el registro.",
                    "Tus derechos" to "Podés solicitar eliminación de tus datos en cualquier momento.",
                    "Seguridad" to "Usamos medidas de seguridad estándar de la industria.",
                    "Contacto" to "Soporte: soporte@maikpet.com"
                ).forEach { (titulo, desc) ->
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}