package com.danifitdev.naughtymatch.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.danifitdev.naughtymatch.R
import com.danifitdev.naughtymatch.ui.viewmodel.HomeViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.showToast
import com.danifitdev.naughtymatch.ui.screens.modals.PerfilScreenModal
import com.danifitdev.naughtymatch.ui.theme.White
import androidx.compose.material3.Button;
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import com.danifitdev.naughtymatch.ui.theme.Black
import com.danifitdev.naughtymatch.ui.theme.DarkGold
import com.danifitdev.naughtymatch.ui.theme.DeepRed
import com.danifitdev.naughtymatch.ui.theme.GhostWhite
import com.danifitdev.naughtymatch.ui.theme.Poppins
import com.danifitdev.naughtymatch.ui.theme.SlateGray
import com.danifitdev.naughtymatch.ui.theme.WineRed

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController,
               onNavigateToLogin:()->Unit, homeViewModel: HomeViewModel = hiltViewModel()) {
    val isLogged by homeViewModel.isLoggedIn.collectAsState(initial = true)
    val user by homeViewModel.currentUser.collectAsState()
    val emparejado by homeViewModel.emparejado.collectAsState()

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
         ProfileBody(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.statusBars.asPaddingValues()), homeViewModel,
             user!!,
             emparejado
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBody(modifier: Modifier, homeViewModel: HomeViewModel, user: User, emparejado :Boolean) {
    var mostrarOpcionGenerarCodigo by remember { mutableStateOf(false) }
    var mostrarOpcionIngresarCodigo by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text("Bienvenido ${user.nombre}", fontSize = 18.sp, style = MaterialTheme.typography.bodyLarge, fontFamily = Poppins)
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.Center) {
            if(emparejado){
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.full_corazon),
                        contentDescription = "no match",
                        modifier = Modifier.width(30.dp).height(30.dp).clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        "Emparejado", fontSize = 12.sp, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp),
                        fontFamily = Poppins
                    )
                }
            }else{
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.half_corazon), // Ruta a la imagen en drawable
                        contentDescription = "no match",
                        modifier = Modifier.width(30.dp).height(30.dp).clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        "Sin match", fontSize = 12.sp, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp),
                        fontFamily = Poppins
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Card(
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                   mostrarOpcionGenerarCodigo = true
                    mostrarOpcionIngresarCodigo = false
                },
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            backgroundColor = Color.White
        ) {
            Text(
                "Quiero generar un código para compartirlo con mi pareja",
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 50.dp, vertical = 20.dp),
                textAlign = TextAlign.Center,
                fontFamily = Poppins
            )
        }
        if(mostrarOpcionGenerarCodigo){
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGold)
            ) {
                androidx.compose.material3.Text(text = "Obtener código", color = White, fontSize = 18.sp, fontFamily = Poppins)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("", fontSize = 20.sp,
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 50.dp),
                textAlign = TextAlign.Center, fontFamily = Poppins )
        }
        Card(
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    mostrarOpcionGenerarCodigo = false
                    mostrarOpcionIngresarCodigo = true
                },
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            backgroundColor = Color.White
        ) {
            Text(
                "Ya tengo un código que me compartió mi pareja",
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 50.dp, vertical = 30.dp),
                textAlign = TextAlign.Center,
                fontFamily = Poppins
            )
        }

        if(mostrarOpcionIngresarCodigo){
            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.material3.TextField(
                value = "",
                onValueChange = {  },
                label = {
                    androidx.compose.material3.Text(
                        "Ingresa código",
                        color = SlateGray,
                        fontFamily = Poppins
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = TextStyle(
                    color = Black,
                    fontSize = 16.sp
                ),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = GhostWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepRed)
            ) {
                androidx.compose.material3.Text(text = "Hacer match", color = White, fontSize = 18.sp, fontFamily = Poppins)
            }
        }
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
                    Text(text = "NaughtyMatch", style = MaterialTheme.typography.titleLarge,
                        color =  MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, )
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
                        Text("Cerrar sesión", fontFamily = Poppins)
                    }
                    DropdownMenuItem(onClick = {
                        expanded = false
                        // Configuración de notificaciones
                    }) {
                        Text("Notificaciones", fontFamily = Poppins)
                    }
                }
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
        PerfilScreenModal(showDialog = showDialogPerfil, onDismiss = { showDialogPerfil = false }, user)
    }
}


