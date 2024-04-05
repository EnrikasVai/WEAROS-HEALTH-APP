package com.example.bakis.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bakis.presentation.screens.HomeScreen
import com.example.bakis.presentation.screens.WelcomeScreen


@Composable
fun SetupNavigation(navController: NavHostController, viewmodel:FitnessViewModel) {
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            // Pass `navController` to `HomeScreen` here
            WelcomeScreen(navController)
        }
        composable("home"){
            HomeScreen(viewmodel,navController)
        }
        // Add any additional destinations here
    }
}
