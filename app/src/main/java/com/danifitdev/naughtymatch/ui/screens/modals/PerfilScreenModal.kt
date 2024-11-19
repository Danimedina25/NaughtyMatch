package com.danifitdev.naughtymatch.ui.screens.modals

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.danifitdev.naughtymatch.R
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.showToast
import com.danifitdev.naughtymatch.ui.theme.Black
import com.danifitdev.naughtymatch.ui.theme.DeepRed
import com.danifitdev.naughtymatch.ui.theme.SlateGray
import com.danifitdev.naughtymatch.ui.theme.GhostWhite
import com.danifitdev.naughtymatch.ui.theme.Poppins
import com.danifitdev.naughtymatch.ui.theme.White
import com.danifitdev.naughtymatch.ui.theme.WineRed
import com.danifitdev.naughtymatch.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PerfilScreenModal(showDialog: Boolean, onDismiss: () -> Unit, onNavigateToLogin:()->Unit, homeViewModel: HomeViewModel = hiltViewModel()) {
    val user by homeViewModel.currentUser.collectAsState()

    if (showDialog) {
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Permite ajustar el tamaño manualmente
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(8.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.background
            ) {
                Box{
                    Perfil(user!!,{}, onNavigateToLogin, homeViewModel )
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp) // Espacio alrededor del botón
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint =  MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun Perfil(
    userProfile: User,
    onEditClick: () -> Unit,
    onNavigateToLogin:()->Unit,
    homeViewModel: HomeViewModel
) {
    var nombre by remember { mutableStateOf(userProfile.nombre ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf(userProfile.fechaNac ?: "") }
    var email by remember { mutableStateOf(userProfile.correo ?: "") }
    var genero by remember { mutableStateOf(userProfile.genero ?: "") }
    val editarPerfil by homeViewModel.editarPerfil.collectAsState()
    val isLogged by homeViewModel.isLoggedIn.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val generos = listOf("Masculino", "Femenino")
    var expanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by homeViewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val registroExitoso by homeViewModel.registroExitoso.collectAsState()

    LaunchedEffect(registroExitoso){
        if(registroExitoso){
            showToast(context, "Te has registrado correctamente en Naughty Match :)")
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    actionIconContentColor = Color.Transparent,
                    navigationIconContentColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color.Transparent
                ),
                title = { Text(text = if(isLogged && editarPerfil) "Editar perfil" else if(isLogged && !editarPerfil) "Perfil" else "Registrar perfil",
                    style = MaterialTheme.typography.bodyLarge,
                   color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    if(isLogged && editarPerfil){
                        IconButton(onClick = { homeViewModel.setEditarPerfil(true) }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Editar perfil",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }else if(isLogged && !editarPerfil){
                        IconButton(onClick = { homeViewModel.setEditarPerfil(true) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar perfil",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = White)
                .verticalScroll(scrollState)
        ) {

            PerfilImagen(imageUrl = userProfile.fotoPerfil!!, selectedImageUri, {value -> selectedImageUri = value},
                if(!isLogged) "Cargar foto de perfil" else "Cambiar foto de perfil")
            if(!editarPerfil && isLogged){
                ProfileInfo(label = "Nombre completo", value = nombre)
                ProfileInfo(label = "Correo electrónico", value = email)
                ProfileInfo(label = "Fecha de nacimiento", value = fechaNacimiento)
                ProfileInfo(label = "Género", value = genero)
            }else{
                Text(
                    text = if(editarPerfil && isLogged) "Editar información"
                    else "Regístrate para guardar tus datos de juego y obtener una mejor experiencia",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp, end = 16.dp, start = 16.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 18.dp)
                ) {
                    TextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = {
                            Text(
                                "Nombre completo",
                                color = SlateGray,
                                fontFamily = Poppins
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
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
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text(
                                "Correo electrónico",
                                color = SlateGray,
                                fontFamily = Poppins
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
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
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        TextField(
                            value = genero,
                            onValueChange = { },
                            label = {
                                Text(
                                    "Género",
                                    color = SlateGray,
                                    fontFamily = Poppins
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
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
                            trailingIcon = {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            },
                            readOnly = true
                        )

                        // Menú desplegable
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            generos.forEach { gender ->
                                DropdownMenuItem(onClick = {
                                    genero = gender
                                    expanded = false
                                }) {
                                    Text(text = gender,  color = Black,
                                        fontSize = 16.sp, fontFamily = Poppins)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true } // Abre el DatePicker
                    ) {
                        CustomTextFieldDate(fechaNacimiento)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if(!isLogged){
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = {
                                Text(
                                    "Contraseña",
                                    color = SlateGray,
                                    fontFamily = Poppins
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
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
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = {
                                Text(
                                    "Confirmar contraseña",
                                    color = SlateGray,
                                    fontFamily = Poppins
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
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
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    if(isLoading){
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                        ) {
                            CircularProgressIndicator()
                        }
                    }else{
                        if(editarPerfil) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = homeViewModel.actualizarInformacion(
                                            selectedImageUri, User(
                                                userProfile.androidId, nombre, email, "",
                                                "", fechaNacimiento, genero
                                            )
                                        )
                                        if (success) {
                                            showToast(
                                                context,
                                                "Información actualizada exitosamente"
                                            )
                                        } else {
                                            showToast(
                                                context,
                                                "Ocurrió un error al intentar actualizar tus datos"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "Guardar",
                                    color = White,
                                    fontSize = 18.sp,
                                    fontFamily = Poppins
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { homeViewModel.setEditarPerfil(false) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SlateGray)
                            ) {
                                Text(
                                    text = "Cancelar",
                                    color = White,
                                    fontSize = 18.sp,
                                    fontFamily = Poppins
                                )
                            }
                        }else if(!isLogged){
                            Button(
                                onClick = {
                                    homeViewModel.validarDatosRegistro(selectedImageUri, nombre, email, genero, fechaNacimiento,
                                        password, confirmPassword)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "Registrar",
                                    color = White,
                                    fontSize = 18.sp,
                                    fontFamily = Poppins
                                )
                            }
                            TextButton(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToLogin) {
                                Text(
                                    text = "¿Ya tienes una cuenta? Inicia sesión",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 15.sp,
                                    fontFamily = Poppins
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        val instant = Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                        val localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate()
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val formattedDate = localDate.format(formatter)
                        fechaNacimiento = formattedDate
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK", fontFamily = Poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", fontFamily = Poppins) }
            }
        ) {
            // The verticalScroll will allow scrolling to show the entire month in case there is not
            // enough horizontal space (for example, when in landscape mode).
            // Note that it's still currently recommended to use a DisplayMode.Input at the state in
            // those cases.
            DatePicker(
                state = datePickerState,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
fun PerfilImagen(imageUrl: String, selectedImageUri: Uri?, onValueChange:(Uri?)-> Unit, textButton: String) {
    var showDialogCambiarFotoPerfil by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        //selectedImageUri = uri
        onValueChange(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            onValueChange(Uri.parse(
                MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    bitmap,
                    "CapturedImage",
                    null
                )))
        }
    }
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .padding(top = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(selectedImageUri != null){
            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberImagePainter(data = uri),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp)
                        .size(150.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }else{
            AsyncImage(
                model = imageUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
        }
        TextButton(onClick = { showDialogCambiarFotoPerfil = true }) {
            Text(
                text = textButton,
                color = WineRed,
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (showDialogCambiarFotoPerfil) {
            AlertDialog(
                shape = MaterialTheme.shapes.large,
                onDismissRequest = { showDialogCambiarFotoPerfil = false },
                buttons = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.Transparent)
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = {
                                showDialogCambiarFotoPerfil = false
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.gallery),
                                contentDescription = "Gallery",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Galería", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                showDialogCambiarFotoPerfil = false
                                cameraLauncher.launch(null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.camera),
                                contentDescription = "Camera",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cámara", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 18.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            color = Black
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = DeepRed
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextFieldDate(
    value: String,
) {
    TextField(
        enabled = false,
        value = value,
        onValueChange = { },
        label = {
            Text(
                "Fecha de nacimiento",
                color = SlateGray,
                fontFamily = Poppins
            )
        },
        modifier = Modifier.fillMaxWidth(),
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
    Spacer(modifier = Modifier.height(5.dp))
}


