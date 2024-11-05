package com.danifitdev.naughtymatch.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.danifitdev.naughtymatch.R
import com.danifitdev.naughtymatch.ui.viewmodel.HomeViewModel
import androidx.compose.material3.MaterialTheme
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.ui.screens.modals.ProfileScreenModal
import com.danifitdev.naughtymatch.ui.theme.White

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController,
               onNavigateToLogin:()->Unit, homeViewModel: HomeViewModel = hiltViewModel()) {
    val isLogged by homeViewModel.isLoggedIn.collectAsState(initial = true)
    val user by homeViewModel.currentUser.collectAsState()

    LaunchedEffect(isLogged) {
        if(!isLogged){
            onNavigateToLogin()
        }
    }
    Scaffold(
        topBar = {
            TopBar(homeViewModel, user!!)
        }
    ) { innerPadding ->
         ProfileSection(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.statusBars.asPaddingValues()), homeViewModel,
             user!!
        )
    }
}

@Composable
fun ProfileSection(modifier: Modifier, homeViewModel: HomeViewModel, user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Perfil",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(user.nombre!!, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TopBar(homeViewmodel: HomeViewModel, user: User) {
    var expanded by remember { mutableStateOf(false) }
    var showDialogPerfil by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        TopAppBar(
            title = {

                // Box para centrar el título
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Bienvenido", style = MaterialTheme.typography.titleLarge, color =  MaterialTheme.colorScheme.onPrimary)
                }
            },
            navigationIcon = {

                IconButton(onClick = {showDialogPerfil = true}) {
                    Icon(Icons.Filled.Person, contentDescription = "Perfil", tint =  MaterialTheme.colorScheme.onPrimary)
                }
            },
            actions = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Configuraciones", tint =  MaterialTheme.colorScheme.onPrimary)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        homeViewmodel.logout()
                    }) {
                        Text("Cerrar sesión")
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        // Configuración de notificaciones
                    }) {
                        Text("Notificaciones")
                    }
                }
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
        ProfileScreenModal(showDialog = showDialogPerfil, onDismiss = { showDialogPerfil = false }, user)
    }
}


