package com.danifitdev.naughtymatch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danifitdev.naughtymatch.R
import com.danifitdev.naughtymatch.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToMain: () -> Unit, loginViewModel: LoginViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val isLogged by loginViewModel.isLoggedIn.collectAsState(false)
    LaunchedEffect(isLogged) {
        delay(100)
        if (isLogged) {
            onNavigateToMain()
        } else {
            onNavigateToLogin()
        }
    }

    // Box para centrar el contenido
    Box(modifier = Modifier.fillMaxSize()
        .background(
           color = MaterialTheme.colorScheme.background
        ), contentAlignment = Alignment.Center) {
        // Cargar y mostrar la imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.logo), // Ruta a la imagen en drawable
            contentDescription = "Splash Screen Image",
            modifier = Modifier.width(300.dp).height(300.dp).clip(CircleShape),
            contentScale = ContentScale.Fit// Abarcar toda la pantalla
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(onNavigateToMain = {}, onNavigateToLogin = {})
}


