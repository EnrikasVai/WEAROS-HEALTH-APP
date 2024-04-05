package com.example.bakis.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.wear.compose.material.CircularProgressIndicator
import kotlinx.coroutines.delay

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun WelcomeScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(true) }
    val minLoadingTime = 1000L // Minimum loading time in milliseconds

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF333333)),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }
    }

    LaunchedEffect(key1 = isLoading) {
        delay(minLoadingTime)
        isLoading = false

        navController.navigate("home") { popUpTo("welcome") { inclusive = true } }
    }
}