package com.macosta.maikpet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.macosta.maikpet.data.model.Mascota
import com.macosta.maikpet.ui.components.AdBanner
import com.macosta.maikpet.ui.components.InfoCard
import com.macosta.maikpet.ui.components.LoadingIndicator
import com.macosta.maikpet.ui.components.MascotaCard
import com.macosta.maikpet.ui.theme.*

@Composable
fun MisMascotasScreen(
    mascotas: List<Mascota>,
    isLoggedIn: Boolean,
    isLoading: Boolean,
    onDeleteMascota: (Int) -> Unit,
    onEditMascota: (Mascota) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            InfoCard {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Mis Mascotas",
                            style = MaterialTheme.typography.titleLarge,
                            color = OnBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Las publicaciones expiran automaticamente a los 30 dias",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Border)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (!isLoggedIn) {
            item {
                InfoCard {
                    Text(
                        text = "⚠️ Inicia sesion para ver tus mascotas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else if (isLoading) {
            item {
                LoadingIndicator("Cargando tus mascotas...")
            }
        } else if (mascotas.isEmpty()) {
            item {
                InfoCard {
                    Text(
                        text = "No tienes mascotas publicadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(mascotas, key = { it.id }) { mascota ->
                MascotaCard(
                    mascota = mascota,
                    showDelete = true,
                    showEdit = true,
                    showExpireInfo = true,
                    onDelete = { onDeleteMascota(mascota.id) },
                    onEdit = { onEditMascota(mascota) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AdBanner()
            }
        }
    }
}
