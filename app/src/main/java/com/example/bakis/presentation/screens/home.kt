package com.example.bakis.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.example.bakis.R
import com.example.bakis.presentation.FitnessViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun HomeScreen(viewModel: FitnessViewModel, navController: NavController) {
    val stepsToday by viewModel.stepCount.collectAsState()
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {

        val focusRequester = rememberActiveFocusRequester()
        val coroutineScope = rememberCoroutineScope()

        ScalingLazyColumn(
            modifier = Modifier
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        listState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            state = listState
        ) {
            item {
                Spacer(modifier = Modifier.height(50.dp))
                Text(text = "Health Application", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
            }
            // Steps Today
            item {
                NavigationBox(
                    navController = navController,
                    navigateTo = "home",
                    titleText = "Steps today:",
                    valueText = stepsToday, // Assuming stepsToday is a String
                    iconColor = Color.Green,
                    iconResId = R.drawable.footsteps, // Ensure this is the correct drawable ID
                    iconSize = 30
                )
            }
            // Sleep Today
            item {
                NavigationBox(
                    navController = navController,
                    navigateTo = "home",
                    titleText = "Sleep today:",
                    valueText = stepsToday, // Replace with the correct value
                    iconColor = Color(0xFF09bfe8),
                    iconResId = R.drawable.bed, // Ensure this is the correct drawable ID
                    iconSize = 30
                )
            }
            // BPM
            item {
                NavigationBox(
                    navController = navController,
                    navigateTo = "home",
                    titleText = "Bpm:",
                    valueText = stepsToday, // Replace with the correct value
                    iconColor = Color(0xFFFF3131),
                    iconResId = R.drawable.heart_beat, // Ensure this is the correct drawable ID
                    iconSize = 30
                )
            }
            // Calories
            item {
                NavigationBox(
                    navController = navController,
                    navigateTo = "home",
                    titleText = "Calories:",
                    valueText = stepsToday, // Replace with the correct value
                    iconColor = Color(0xFFf52749),
                    iconResId = R.drawable.calories_svgrepo_com, // Ensure this is the correct drawable ID
                    iconSize = 30
                )
            }
        }
    }
}

@Composable
fun NavigationBox(
    navController: NavController,
    navigateTo: String,
    iconResId: Int,
    titleText: String,
    valueText: String,
    iconColor: Color,
    iconSize: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(10.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color.DarkGray)
            .clickable { navController.navigate(navigateTo) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            Icon(
                painter = painterResource(id = iconResId), // Use the dynamic icon resource ID
                contentDescription = "Navigation icon",
                tint = iconColor,
                modifier = Modifier.size(iconSize.dp) // Set the icon size as desired
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = titleText, fontSize = 14.sp)
                Text(text = valueText, fontSize = 25.sp, color = Color.White) // Assuming you want the text color to be white
            }
        }
    }
}