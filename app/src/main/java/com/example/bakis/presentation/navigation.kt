package com.example.bakis.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bakis.presentation.screens.AddCaloriesScreen
import com.example.bakis.presentation.screens.CaloriesScreen
import com.example.bakis.presentation.screens.HeartRateCalculator
import com.example.bakis.presentation.screens.HeartRateScreen
import com.example.bakis.presentation.screens.HomeScreen
import com.example.bakis.presentation.screens.NutritionScreen
import com.example.bakis.presentation.screens.SleepScreen
import com.example.bakis.presentation.screens.StepsScreen
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
        composable("bpmCalculator"){
            HeartRateScreen(viewmodel,navController)
        }
        composable("bpmTest"){
            HeartRateCalculator(viewmodel,navController)
        }
        composable("steps"){
            StepsScreen(navController,viewmodel)
        }
        composable("sleep"){
            SleepScreen(navController,viewmodel)
        }
        composable("nutrition"){
            NutritionScreen(navController,viewmodel)
        }
        composable("nutritionAdd"){
            AddCaloriesScreen(navController,viewmodel)
        }
        composable("burnedCalories"){
            CaloriesScreen(navController,viewmodel)
        }
        // Add any additional destinations here
    }
}
