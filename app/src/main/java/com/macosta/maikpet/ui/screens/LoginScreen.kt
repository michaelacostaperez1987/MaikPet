package com.macosta.maikpet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.macosta.maikpet.ui.components.InfoCard
import com.macosta.maikpet.ui.theme.*

@Composable
fun LoginScreen(
    isLoading: Boolean,
    error: String?,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (nombre: String, direccion: String, telefono: String, email: String, password: String, edad: Int) -> Unit,
    onViewPrivacy: () -> Unit = {},
    onViewTerms: () -> Unit = {},
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoginMode by remember { mutableStateOf(true) }
    
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    
    var regNombre by remember { mutableStateOf("") }
    var regDireccion by remember { mutableStateOf("") }
    var regTelefono by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regEdad by remember { mutableStateOf("") }
    var aceptaTyC by remember { mutableStateOf(false) }
    var showAceptaError by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 80.dp)
            .verticalScroll(scrollState)
    ) {
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mi Cuenta",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "❌ $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoginMode) {
            InfoCard {
                Text(
                    text = "Iniciar Sesion",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = loginEmail,
                    onValueChange = { loginEmail = it; onClearError() },
                    label = { Text("Email") },
                    placeholder = { Text("tu@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it; onClearError() },
                    label = { Text("Contrasena") },
                    placeholder = { Text("******") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { onLogin(loginEmail, loginPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading && loginEmail.isNotBlank() && loginPassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OnPrimary)
                    } else {
                        Text(
                            text = "Iniciar Sesion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(color = Border)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿No tienes cuenta? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    TextButton(onClick = {
                        isLoginMode = false
                        onClearError()
                    }) {
                        Text(
                            text = "Registrate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary
                        )
                    }
                }
            }
        } else {
            InfoCard {
                Text(
                    text = "Registrarse",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = regNombre,
                    onValueChange = { regNombre = it; onClearError() },
                    label = { Text("Nombre completo") },
                    placeholder = { Text("Juan Perez") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = regDireccion,
                    onValueChange = { regDireccion = it; onClearError() },
                    label = { Text("Direccion") },
                    placeholder = { Text("Tu direccion") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = regTelefono,
                    onValueChange = { regTelefono = it; onClearError() },
                    label = { Text("Telefono") },
                    placeholder = { Text("099 123 456") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = regEdad,
                    onValueChange = { 
                        if (it.all { c -> c.isDigit() }) regEdad = it
                        onClearError()
                    },
                    label = { Text("Edad") },
                    placeholder = { Text("18") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (regEdad.isNotBlank() && (regEdad.toIntOrNull() ?: 0) < 18) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Debes tener al menos 18 años para Registrarte",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it; onClearError() },
                    label = { Text("Email") },
                    placeholder = { Text("tu@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { regPassword = it; onClearError() },
                    label = { Text("Contrasena") },
                    placeholder = { Text("******") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Antes de registrarte, debes aceptar los siguientes documentos:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "📄 Términos y Condiciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onViewTerms() }
                        )
                        Text(
                            text = "📄 Política de Privacidad",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onViewPrivacy() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = aceptaTyC,
                        onCheckedChange = {
                            aceptaTyC = it
                            showAceptaError = !it && regNombre.isNotBlank()
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "He leído y acepto los Términos y Condiciones y la Política de Privacidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackground
                    )
                }

                if (showAceptaError) {
                    Text(
                        text = "Debes aceptar los Términos y Condiciones y la Política de Privacidad para registrarte",
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val edad = regEdad.toIntOrNull() ?: 0
                        if (!aceptaTyC) {
                            showAceptaError = true
                            return@Button
                        }
                        if (edad < 18) {
                            return@Button
                        }
                        onRegister(regNombre, regDireccion, regTelefono, regEmail, regPassword, edad)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading && regNombre.isNotBlank() && regDireccion.isNotBlank() &&
                            regTelefono.isNotBlank() && regEmail.isNotBlank() && regPassword.isNotBlank() &&
                            aceptaTyC && (regEdad.toIntOrNull() ?: 0) >= 18,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OnPrimary)
                    } else {
                        Text(
                            text = "Registrarse",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(color = Border)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = {
                        isLoginMode = true
                        onClearError()
                    }) {
                        Text(
                            text = "<- Volver a Iniciar Sesion",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = Border,
    focusedLabelColor = Primary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = Primary,
    focusedTextColor = OnBackground,
    unfocusedTextColor = OnBackground
)
