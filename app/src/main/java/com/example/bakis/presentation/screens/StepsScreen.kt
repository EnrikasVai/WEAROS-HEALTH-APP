package com.example.bakis.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.example.bakis.R
import com.example.bakis.presentation.FitnessViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun StepsScreen(navController: NavController, viewModel: FitnessViewModel) {
    val listState = rememberScalingLazyListState()
    val stepsFetched by viewModel.stepCount.collectAsState()
    val stepsToday = stepsFetched.toInt()
    val distanceToday by viewModel.todayDistance.collectAsState()
    val formattedDistance = String.format("%.2f", distanceToday / 1000)
    val minutesToday by viewModel.todayMoveMinutes.collectAsState()
    val stepsGoal = 6000
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
                    painter = painterResource(R.drawable.footsteps),
                    contentDescription = "footsteps icon",
                    tint = Color.Green,
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(40.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "$stepsToday", color = Color.White, fontSize = 40.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(text = "/$stepsGoal", color = Color(0xFF808080), fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                ShowProgress(stepsToday,stepsGoal)
                Spacer(modifier = Modifier.height(10.dp))
                if(stepsToday<stepsGoal) {
                    val stepsLeft= stepsGoal-stepsToday
                    Text(text = "Till goal: $stepsLeft steps", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                else{
                    Text(text = "Goal reached", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 15.sp, color = Color(0xFF009245))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "km", color = Color(0xFF808080))
                        Text(text = formattedDistance, fontSize = 25.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Move Min", color = Color(0xFF808080))
                        Text(text = "${minutesToday.toInt()}", fontSize = 25.sp)
                    }
                }
            }
            item { 
                Spacer(modifier = Modifier.height(100.dp))
            }

        }
    }
}
@Composable
fun ShowProgress(score: Int, stepsGoal: Int) {
    val gradient = Brush.linearGradient(
        listOf(Color(0xFF009245), Color(0xFFFCEE21))
    )
    val progressFactor = remember(score, stepsGoal) {
        (score.toFloat() / stepsGoal).coerceIn(0f, 1f)
    }

    Row(modifier = Modifier
        .padding(start = 20.dp, end = 20.dp, top = 10.dp)
        .fillMaxWidth()
        .height(20.dp)
        .border(
            width = 4.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF009245),
                    Color(0xFFFCEE21)
                )
            ),
            shape = RoundedCornerShape(50.dp)
        )
        .clip(
            RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 50,
                bottomEndPercent = 50,
                bottomStartPercent = 50
            )
        )
        .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(progressFactor)
                .background(brush = gradient),
            enabled = false,
            ) {
        }
    }
}