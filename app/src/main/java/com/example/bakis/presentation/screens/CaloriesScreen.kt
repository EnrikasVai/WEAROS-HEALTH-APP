package com.example.bakis.presentation.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
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
fun CaloriesScreen(viewModel: FitnessViewModel) {
    val listState = rememberScalingLazyListState()
    val caloriesBurned by viewModel.calCount.collectAsState()
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
                    contentDescription = "footsteps icon",
                    tint = Color(0xFFf52749),
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(40.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "$caloriesBurned kcal", color = Color.White, fontSize = 40.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(text = "Burned today", color = Color(0xFF808080), fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}