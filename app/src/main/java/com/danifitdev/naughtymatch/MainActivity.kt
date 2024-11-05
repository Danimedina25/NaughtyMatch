package com.danifitdev.naughtymatch

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.danifitdev.naughtymatch.ui.viewmodel.LoginViewModel
import com.danifitdev.naughtymatch.ui.theme.JuegoDeParejasAppTheme
import com.danifitdev.naughtymatch.navigation.AppNavGraph
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.gson.Gson
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var callbackManager: CallbackManager
    private val loginViewModel: LoginViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                loginViewModel.setLoading(true)
                val accessToken = result?.accessToken
                //val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
                loginViewModel.loginWithFacebook(accessToken!!.token)
            }
            override fun onCancel() {
               // loginViewModel.setLoading(false)
            }
            override fun onError(error: FacebookException) {
                //loginViewModel.setLoading(false)
            }
        })
        setContent {
            JuegoDeParejasAppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController, this, callbackManager)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JuegoDeParejasAppTheme {
    }
}