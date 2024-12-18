package com.danifitdev.naughtymatch.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danifitdev.naughtymatch.R
import com.danifitdev.naughtymatch.ui.viewmodel.LoginViewModel
import com.danifitdev.naughtymatch.showToast
import com.danifitdev.naughtymatch.ui.theme.Poppins
import com.danifitdev.naughtymatch.ui.theme.White
import com.facebook.login.LoginManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToHome:()->Unit,
    onFacebookLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by loginViewModel.isLoading.collectAsState()
    val isLogged by loginViewModel.isLoggedIn.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()

    LaunchedEffect(isLogged) {
        if (isLogged)
            onNavigateToHome()
    }

    LaunchedEffect(errorMessage) {
        if(!errorMessage.isNullOrEmpty()){
            showToast(context, errorMessage!!)
            loginViewModel.limpiarMensajeError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
         Column(Modifier.padding(horizontal = 22.dp)
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
             Image(
                 painter = painterResource(id = R.drawable.logo), // Ruta a la imagen en drawable
                 contentDescription = "Logo image",
                 modifier = Modifier.width(200.dp).height(200.dp).clip(CircleShape),
                 contentScale = ContentScale.Fit// Abarcar toda la pantalla
             )
             Text(
                 text = "Naughty Match",
                 style = MaterialTheme.typography.titleLarge,
                 color = MaterialTheme.colorScheme.onBackground,
                 modifier = Modifier.padding(bottom = 24.dp, top = 25.dp)
             )


             TextField(
                 value = email,
                 onValueChange = { email = it },
                 label = { Text("Correo electrónico", color = MaterialTheme.colorScheme.onSecondary) },
                 modifier = Modifier.fillMaxWidth(),
                 shape = RoundedCornerShape(16.dp),
                 singleLine = true,
                 keyboardOptions = KeyboardOptions.Default.copy(
                     imeAction = ImeAction.Next // Acción de siguiente en el teclado
                 ),
                 textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary,
                     fontSize = 16.sp, fontFamily = Poppins),
                 colors = TextFieldDefaults.textFieldColors(
                     containerColor = MaterialTheme.colorScheme.surface,
                     focusedIndicatorColor = Color.Transparent,
                     unfocusedIndicatorColor = Color.Transparent,
                     disabledIndicatorColor = Color.Transparent,
                 ),
             )

             Spacer(modifier = Modifier.height(8.dp))

             TextField(
                 value = password,
                 onValueChange = { password = it },
                 label = { Text("Contraseña", color = MaterialTheme.colorScheme.onSecondary) },
                 modifier = Modifier.fillMaxWidth(),
                 shape = RoundedCornerShape(16.dp),
                 singleLine = true,
                 textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary, fontSize = 16.sp,
                     fontFamily = Poppins),
                 colors = TextFieldDefaults.textFieldColors(
                     containerColor = MaterialTheme.colorScheme.surface,
                     focusedIndicatorColor = Color.Transparent,
                     unfocusedIndicatorColor = Color.Transparent,
                     disabledIndicatorColor = Color.Transparent,
                     ),
                 visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                 trailingIcon = {
                     IconButton(onClick = { passwordVisible = !passwordVisible }) {
                         Icon(
                             painter = painterResource(id = if (passwordVisible) R.drawable.icon_eye else R.drawable.icon_eye_off),
                             contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                             modifier = Modifier.size(22.dp)
                         )
                     }
                 }
             )

             Spacer(modifier = Modifier.height(16.dp))

             if (isLoading) {
                 Box(
                     contentAlignment = Alignment.Center,
                     modifier = Modifier
                         .fillMaxWidth()
                         .background(Color.Transparent)
                 ) {
                     CircularProgressIndicator()
                 }
             } else {

                 Button(
                     onClick = {
                         loginViewModel.validarDatos(email.trim(), password.trim())},
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(50.dp),
                     colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                 ) {
                     Text(text = "Iniciar sesión", color = White, fontSize = 18.sp, fontFamily = Poppins)
                 }

                 Spacer(modifier = Modifier.height(8.dp))

                 Button(
                     onClick = {
                         onFacebookLogin()
                     },
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(50.dp),
                     colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)) // Color oficial de Facebook
                 ) {
                     Text(text = "Iniciar sesión con Facebook", color = White, fontSize = 18.sp, fontFamily = Poppins)
                 }
                 Spacer(modifier = Modifier.height(16.dp))

                 TextButton(onClick = onNavigateToRegister) {
                     Text(
                         text = "¿No tienes una cuenta? Regístrate",
                         color = MaterialTheme.colorScheme.onPrimary,
                         fontSize = 15.sp,
                         fontFamily = Poppins
                     )
                 }
             }
         }
     }
}

fun performFacebookLogin(activity: ComponentActivity) {
    LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
}








