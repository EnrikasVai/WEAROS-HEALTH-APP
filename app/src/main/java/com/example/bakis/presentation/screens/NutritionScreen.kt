package com.example.bakis.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import com.example.bakis.R
import com.example.bakis.presentation.FitnessViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun NutritionScreen(navController: NavController, viewModel: FitnessViewModel) {
    val todayNutrition by viewModel.todayCalories.collectAsState()

    val listState = rememberScalingLazyListState()
    LaunchedEffect(key1 = true) {
        viewModel.fetchTodaysNutrition()
    }
    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {

        val focusRequester = rememberActiveFocusRequester()
        val coroutineScope = rememberCoroutineScope()

        LazyColumn(
            modifier = Modifier
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        listState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            verticalArrangement = Arrangement.spacedBy((-10).dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    painter = painterResource(R.drawable.calories_svgrepo_com),
                    contentDescription = "calories icon",
                    tint = Color(0xFFf52749),
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(40.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = String.format("%.0f", todayNutrition), color = Color.White, fontSize = 40.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(text = "Eaten today", color = Color(0xFF808080), fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 36.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.DarkGray)
                            .clickable { navController.navigate("nutritionAdd") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add kcal")
                    }
                }
            }
        }
    }
}
@Composable
fun AddCaloriesScreen(navController: NavController, viewModel: FitnessViewModel) {
    val calorieOptions = remember { (50..2000 step 50).map { "$it kcal" } }
    val pickerState = rememberPickerState(
        initialNumberOfOptions = calorieOptions.size,
        repeatItems = false
    )
    val contentDescription by remember {
        derivedStateOf { "Selected ${pickerState.selectedOption + 1} which is ${calorieOptions[pickerState.selectedOption]}" }
    }
    val context = LocalContext.current
    val currentTime = System.currentTimeMillis()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Picker(
                modifier = Modifier
                    .size(160.dp, 140.dp)
                    .padding(16.dp),
                state = pickerState,
                contentDescription = contentDescription,
            ) {
                Text(
                    text = calorieOptions[it],
                    fontSize = if (it == pickerState.selectedOption) 22.sp else 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.DarkGray)
                    .clickable {
                        val selectedCalories = calorieOptions[pickerState.selectedOption].filter { it.isDigit() }.toFloat()
                        viewModel.addCalories(context, selectedCalories, currentTime, currentTime)
                        navController.navigateUp()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Add kcal")
            }
        }
    }
}










