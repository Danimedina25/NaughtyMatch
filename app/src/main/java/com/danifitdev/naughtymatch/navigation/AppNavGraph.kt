package com.danifitdev.naughtymatch.navigation

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.danifitdev.naughtymatch.ui.screens.HomeScreen
import com.danifitdev.naughtymatch.ui.screens.LoginScreen
import com.danifitdev.naughtymatch.ui.screens.RegisterScreen
import com.danifitdev.naughtymatch.ui.screens.SplashScreen
import com.danifitdev.naughtymatch.ui.screens.performFacebookLogin
import com.facebook.CallbackManager


sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")
    object LoginScreen : Screen("login_screen")
    object RegisterScreen: Screen("register_screen")
    object HomeScreen: Screen("home_screen")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController,  activity: ComponentActivity, callbackManager: CallbackManager) {
    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
        composable(Screen.SplashScreen.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.SplashScreen.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.SplashScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.HomeScreen.route){
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
                onFacebookLogin = {
                    performFacebookLogin(activity)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.RegisterScreen.route){
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
            )
        }
        composable(route = Screen.RegisterScreen.route){ backStackEntry ->
            RegisterScreen(
                {
                    navController.navigate(Screen.LoginScreen.route){
                        popUpTo(Screen.RegisterScreen.route) { inclusive = true }
                    }
                })
        }
        composable(route = Screen.HomeScreen.route){
            HomeScreen(navController, {
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
            })
        }
    }
}
