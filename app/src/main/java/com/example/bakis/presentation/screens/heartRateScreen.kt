package com.example.bakis.presentation.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.example.bakis.R
import com.example.bakis.presentation.FitnessViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun HeartRateScreen(viewModel: FitnessViewModel, navController: NavController) {
    LaunchedEffect(Unit) {
        viewModel.fetchLastHeartRateData()
        viewModel.fetchTodaysHeartRateData()
    }
    val heartRateDataList by viewModel.todaysHeartRateData.observeAsState(initial = emptyList())
    val minHeartRate = heartRateDataList.minByOrNull { it.bpm }?.bpm
    val maxHeartRate = heartRateDataList.maxByOrNull { it.bpm }?.bpm
    val listState = rememberScalingLazyListState()
    val lastBpmReading by viewModel.lastHeartRateData.observeAsState()
    val bpm: Float? = lastBpmReading?.bpm
    val timeString: String? = lastBpmReading?.timeString
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
            verticalArrangement = Arrangement.spacedBy(-10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth() // Ensure the Row fills the max width to center its children
                ) {
                    Icon(
                        painter = painterResource(R.drawable.heart_beat),
                        contentDescription = "bpm icon",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$timeString",
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(35.dp))
                Text(text = "${bpm?.toInt()}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 50.sp)
                Text(text = "bpm", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.LightGray)
                Spacer(modifier = Modifier.height(25.dp))
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
                            .clickable { navController.navigate("bpmTest") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Measure")
                    }
                }
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
                        Text(text = "Low", color = Color.DarkGray)
                        Text(text = "${minHeartRate?.toInt()}", fontSize = 25.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "High", color = Color.DarkGray)
                        Text(text = "${maxHeartRate?.toInt()}", fontSize = 25.sp)
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "Today's Readings", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                HeartRateList(heartRateData = heartRateDataList)

                Spacer(modifier = Modifier.height(50.dp))

            }
        }
    }
}


@Composable
fun HeartRateList(heartRateData: List<FitnessViewModel.HeartRateData>) {
    Column {
        val lastHeartRateData = heartRateData.takeLast(10)
        lastHeartRateData.forEach { data ->
            HeartRateRow(data)
        }
    }
}

@Composable
fun HeartRateRow(data: FitnessViewModel.HeartRateData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "${data.bpm.toInt()} bpm", style = MaterialTheme.typography.body1)
        Text(text = data.timeString, style = MaterialTheme.typography.body1, color = Color.LightGray)
    }
}

@Composable
fun HeartRateCalculator(viewModel: FitnessViewModel, navController: NavController) {
    var progress by remember { mutableStateOf(0f) }
    var animateHeart by remember { mutableStateOf(true) }
    val animatedProgress = animateFloatAsState(
        targetValue = if (animateHeart) progress else 1f
    )
    val scale = animateFloatAsState(
        targetValue = if (animateHeart) 1.0f + 0.2f * animatedProgress.value else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = { it }),
            repeatMode = RepeatMode.Reverse
        )
    )

    val bpmReady by viewModel.bpmReady.observeAsState()
    val bpmValue by viewModel.bpmValue.observeAsState()

    LaunchedEffect(key1 = true) {
        viewModel.collectHeartRateFor30Seconds() // Start collecting data
        for (i in 1..100) {
            delay(300) // Adjust duration to simulate progress
            progress = i / 100f
        }
        animateHeart = false // Stop animation after data collection
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.heart_icon),
            contentDescription = "Heart",
            modifier = Modifier.size(100.dp * scale.value),
            colorFilter = ColorFilter.tint(Color.Red),
            contentScale = ContentScale.Fit
        )
        if (animateHeart) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 18.sp,
                color = Color.White
            )
        } else if (bpmReady == true) {
            animateHeart = false
            Text(
                text = "BPM: ${bpmValue ?: "Calculating..."}",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

/*

@SuppressLint("SuspiciousIndentation")
@Composable
fun HeartRateCalculator(viewModel: FitnessViewModel, navController: NavController) {
    // State to manage progress
    var progress by remember { mutableStateOf(0f) } // 0 to 1
    val animatedProgress = animateFloatAsState(targetValue = progress)

    // Observe changes to the BPM ready state and value
    val bpmReady by viewModel.bpmReady.observeAsState()
    val bpmValue by viewModel.bpmValue.observeAsState()

    BackHandler {
        navController.navigateUp()
    }

    LaunchedEffect(key1 = true) {
        viewModel.collectHeartRateFor30Seconds() // Start collecting data
        // Simulate progress update alongside data collection
        for (i in 1..100) {
            delay(100) // 30 seconds total for 1 to 100, adjust as needed
            progress = i / 100f
        }
    }

    // Displaying the progress indicator
    if(progress != 1f)
        CircularProgressIndicator(progress = animatedProgress.value)

    // Display the BPM value if ready
    if (bpmReady == true) {
        Text(text = "BPM: ${bpmValue ?: "Calculating..."}")
    }
}
*/