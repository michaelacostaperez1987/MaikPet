package com.macosta.maikpet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.macosta.maikpet.ui.screens.LoginScreen
import com.macosta.maikpet.ui.screens.TermsActivity
import com.macosta.maikpet.ui.screens.PrivacyActivity
import com.macosta.maikpet.ui.theme.PetTheme
import com.macosta.maikpet.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PetTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                
                // Verificar si ya hay sesión
                LaunchedEffect(uiState.currentUser) {
                    if (uiState.currentUser != null) {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
                
                LoginScreen(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onLogin = { email, password ->
                        viewModel.login(email, password)
                    },
                    onRegister = { nombre, direccion, telefono, email, password, edad ->
                        viewModel.register(nombre, direccion, telefono, email, password, edad)
                    },
                    onViewTerms = { startActivity(Intent(this, TermsActivity::class.java)) },
                    onViewPrivacy = { startActivity(Intent(this, PrivacyActivity::class.java)) },
                    onClearError = { viewModel.clearError() }
                )
            }
        }
    }
}