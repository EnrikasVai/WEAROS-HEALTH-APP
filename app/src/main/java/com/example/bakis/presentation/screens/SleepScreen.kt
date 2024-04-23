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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
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
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.example.bakis.R
import com.example.bakis.presentation.FitnessViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun SleepScreen(navController: NavController, viewModel: FitnessViewModel){
    val listState = rememberScalingLazyListState()
    val sleepToday by viewModel.sleepCount.collectAsState()
    //format sleep data
    val hours = sleepToday.toInt() / 60
    val remainingMinutes = sleepToday.toInt() % 60

    val sleepSegments by viewModel.sleepSegments.observeAsState(initial = emptyList())
    val overallStartSleepTime = sleepSegments.firstOrNull()?.startTime
    val overallEndSleepTime = sleepSegments.lastOrNull()?.endTime

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val startTime: Date? = overallStartSleepTime?.let { timeFormat.parse(it) }
    val endTime: Date? = overallEndSleepTime?.let { timeFormat.parse(it) }

    val durationMinutes = if (startTime != null && endTime != null) {
        val duration = if (endTime.before(startTime)) {
            endTime.time + TimeUnit.DAYS.toMillis(1) - startTime.time
        } else {
            endTime.time - startTime.time
        }
        TimeUnit.MILLISECONDS.toMinutes(duration)
    } else {
        0L
    }

    val sleepMinutes = sleepToday.toInt()
    val durationMinutesFloat = durationMinutes.toFloat()
    val sleepEff = (sleepMinutes / durationMinutesFloat) * 100

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
                    painter = painterResource(R.drawable.bed),
                    contentDescription = "Bed icon",
                    tint = Color(0xFF09bfe8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(30.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(text = "${hours}h ${remainingMinutes}min", color = Color.White, fontSize = 40.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(text = "$overallStartSleepTime - $overallEndSleepTime", color = Color(0xFF808080), fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Sleep ef", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,color = Color(0xFF808080))
                val sleepEffFormatted = String.format("%.0f", sleepEff)
                Text(text = "$sleepEffFormatted%", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}