package com.example.bakis.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.SensorRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GoogleFitDataHandler(private val context: Context) {


    interface StepDataListener {
        fun onStepDataReceived(stepCount: Int)
        fun onError(e: Exception)
    }
    fun readStepData(listener: StepDataListener) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = startCalendar.timeInMillis - 1

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val dataSet = response.buckets.flatMap { it.dataSets }.flatMap { it.dataPoints }
                val totalSteps = dataSet.sumOf { it.getValue(Field.FIELD_STEPS).asInt() }
                listener.onStepDataReceived(totalSteps)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFit", "There was a problem reading the data.", e)
                listener.onError(e)
            }
    }
    interface SleepDataListener {
        fun onSleepDataReceived(totalSleepMinutes: Int)
        fun onError(e: Exception)
    }

    fun readSleepData(listener: SleepDataListener) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = startCalendar.timeInMillis - 1

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_SLEEP_SEGMENT)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var totalSleepMinutes = 0
                response.dataSets.flatMap { it.dataPoints }.forEach { dataPoint ->
                    val startTime = dataPoint.getStartTime(TimeUnit.MINUTES)
                    val endTime = dataPoint.getEndTime(TimeUnit.MINUTES)
                    totalSleepMinutes += (endTime - startTime).toInt()
                }
                listener.onSleepDataReceived(totalSleepMinutes)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitSleep", "There was a problem reading the sleep data.", e)
                listener.onError(e)
            }
    }
    //Read Sensors Real time:
    interface HeartRateDataListener {
        fun onHeartRateDataReceived(bpm: Float)
        fun onError(e: Exception)
    }
    fun subscribeToHeartRate(listener: HeartRateDataListener) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account == null) {
            listener.onError(Exception("Not signed in to Google Fit."))
            return
        }

        val request = SensorRequest.Builder()
            .setDataType(DataType.TYPE_HEART_RATE_BPM)
            .setSamplingRate(1, TimeUnit.SECONDS)
            .build()

        Fitness.getSensorsClient(context, account)
            .add(request) { dataPoint ->
                val bpm = dataPoint.getValue(Field.FIELD_BPM).asFloat()
                listener.onHeartRateDataReceived(bpm)
            }
            .addOnSuccessListener {
                Log.d("GoogleFitDataHandler", "Successfully subscribed to heart rate sensor.")
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitDataHandler", "There was a problem subscribing to the heart rate sensor.", e)
                listener.onError(e)
            }
    }
    fun writeHeartRateData(bpm: Float, timestamp: Long) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account == null) {
            Log.e("GoogleFitDataHandler", "Not signed in to Google Fit.")
            return
        }

        // Define the data source
        val dataSource = DataSource.Builder()
            .setAppPackageName(context)
            .setDataType(DataType.TYPE_HEART_RATE_BPM)
            .setType(DataSource.TYPE_RAW)
            .build()

        // Create a data point for the heart rate
        val dataPoint = DataPoint.builder(dataSource)
            .setField(Field.FIELD_BPM, bpm)
            .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
            .build()

        // Create a data set
        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()

        // Insert the data set into the user's Google Fit store
        Fitness.getHistoryClient(context, account)
            .insertData(dataSet)
            .addOnSuccessListener {
                Log.d("GoogleFitDataHandler", "Successfully wrote heart rate data to Google Fit.")
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitDataHandler", "There was a problem writing the heart rate data to Google Fit.", e)
            }
    }
    //last bpm reading and time
    interface LastHeartRateDataListener {
        fun onHeartRateDataReceived(bpm: Float, timeString: String)
        fun onError(e: Exception)
    }

    fun readLastHeartRateData(listener: LastHeartRateDataListener) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .setLimit(1)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val dataPoints = response.dataSets.flatMap { it.dataPoints }
                if (dataPoints.isNotEmpty()) {
                    val lastDataPoint = dataPoints.last()
                    val bpm = lastDataPoint.getValue(Field.FIELD_BPM).asFloat()
                    val timestamp = lastDataPoint.getTimestamp(TimeUnit.MILLISECONDS)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val timeString = timeFormat.format(Date(timestamp))
                    listener.onHeartRateDataReceived(bpm, timeString)
                } else {
                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFit", "There was a problem reading the heart rate data.", e)
                listener.onError(e)
            }
    }
    //todays all bpm readings
    interface HeartRateDataListenerToday {
        fun onHeartRateDataReceived(readings: List<Pair<String, Float>>) // Pair of time string and BPM
        fun onError(e: Exception)
    }
    fun readTodaysHeartRateData(listener: HeartRateDataListenerToday) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val readings = response.dataSets.flatMap { dataSet ->
                    dataSet.dataPoints.map { dataPoint ->
                        val bpm = dataPoint.getValue(Field.FIELD_BPM).asFloat()
                        val timestamp = dataPoint.getTimestamp(TimeUnit.MILLISECONDS)
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeString = timeFormat.format(Date(timestamp))
                        Pair(timeString, bpm)
                    }
                }
                if (readings.isNotEmpty()) {
                    listener.onHeartRateDataReceived(readings)
                } else {
                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFit", "There was a problem reading the heart rate data.", e)
                listener.onError(e)
            }
    }
    //todays min max bpm value
    interface MinMaxHeartRateListener {
        fun onMinMaxHeartRateFound(minBpm: Float, maxBpm: Float)
        fun onError(e: Exception)
    }

    fun fetchMinMaxHeartRateForToday(listener: MinMaxHeartRateListener) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endTime = System.currentTimeMillis()
        val startTime = startCalendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_HEART_RATE_BPM)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val buckets = response.buckets
                var minBpm = Float.MAX_VALUE
                var maxBpm = Float.MIN_VALUE

                for (bucket in buckets) {
                    val dataSet = bucket.getDataSet(DataType.TYPE_HEART_RATE_BPM)
                    if (dataSet != null) {
                        for (dataPoint in dataSet.dataPoints) {
                            val bpm = dataPoint.getValue(Field.FIELD_BPM).asFloat()
                            if (bpm < minBpm) minBpm = bpm
                            if (bpm > maxBpm) maxBpm = bpm
                        }
                    }
                }
                if (minBpm != Float.MAX_VALUE && maxBpm != Float.MIN_VALUE) {
                    listener.onMinMaxHeartRateFound(minBpm, maxBpm)
                } else {
                    listener.onError(Exception("No heart rate data available for today."))
                }
            }
            .addOnFailureListener { e ->
                listener.onError(e)
            }
    }
    fun readFitnessData(listener: TodayDataListener) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = startCalendar.timeInMillis - 1

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .aggregate(DataType.TYPE_MOVE_MINUTES, DataType.AGGREGATE_MOVE_MINUTES)
            .aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val buckets = response.buckets
                var totalDistance = 0.0
                var moveMinutes = 0.0
                var averageSpeed = 0.0
                buckets.forEach { bucket ->
                    bucket.dataSets.forEach { dataSet ->
                        when (dataSet.dataType) {
                            DataType.TYPE_DISTANCE_DELTA -> totalDistance += dataSet.dataPoints.sumOf { it.getValue(Field.FIELD_DISTANCE).asFloat().toDouble() }
                            DataType.TYPE_MOVE_MINUTES -> moveMinutes += dataSet.dataPoints.sumOf { it.getValue(Field.FIELD_DURATION).asInt().toLong() }
                        }
                    }
                }
                listener.onStepDataReceived(totalDistance, moveMinutes, averageSpeed)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFit", "There was a problem reading the data.", e)
                listener.onError(e)
            }
    }
    interface TodayDataListener {
        fun onStepDataReceived(distance: Double, moveMinutes: Double, averageSpeed: Double)
        fun onError(e: Exception)
    }
    interface SleepSegmentListener {
        fun onSleepSegmentReceived(sleepSegments: List<SleepSegment>)
        fun onError(e: Exception)
    }

    data class SleepSegment(val startTime: String, val endTime: String, val type: Int)

    fun readSleepSegments(listener: SleepSegmentListener) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = startCalendar.timeInMillis - 1

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_SLEEP_SEGMENT)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val sleepSegments = response.dataSets.flatMap { dataSet ->
                    dataSet.dataPoints.map { dataPoint ->
                        val segmentStart = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                        val segmentEnd = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
                        val sleepType = dataPoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()
                        val startFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(segmentStart))
                        val endFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(segmentEnd))
                        SleepSegment(startFormat, endFormat, sleepType)
                    }
                }
                if (sleepSegments.isNotEmpty()) {
                    listener.onSleepSegmentReceived(sleepSegments)
                } else {
                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitSleep", "There was a problem reading the sleep segment data.", e)
                listener.onError(e)
            }
    }
    //calories eaten
    //TODAYS CALORIES EATEN
    interface TodayCaloriesListener {
        fun onCaloriesDataReceived(calories: Double)
        fun onError(e: Exception)
    }

    fun readTodayCaloriesData(listener: TodayCaloriesListener) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = startCalendar.timeInMillis - 1

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_NUTRITION)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var totalCalories = 0.0
                response.dataSets.forEach { dataSet ->
                    dataSet.dataPoints.forEach { dataPoint ->
                        val calories = dataPoint.getValue(Field.FIELD_NUTRIENTS).getKeyValue(Field.NUTRIENT_CALORIES)
                        if (calories != null) {
                            totalCalories += calories
                        }
                    }
                }
                listener.onCaloriesDataReceived(totalCalories)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitCalories", "There was a problem reading the nutrition data.", e)
                listener.onError(e)
            }
    }
    //today burned calories
    interface CaloriesDataListener {
        fun onCalDataReceived(calCount: Int)
        fun onError(e: Exception)
    }
    fun readCaloriesData(listener: CaloriesDataListener) {
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = startCalendar.timeInMillis

        startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = startCalendar.timeInMillis - 1

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.TYPE_CALORIES_EXPENDED)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var totalCalories = 0.0
                response.buckets.flatMap { it.dataSets }.flatMap { it.dataPoints }.forEach { dataPoint ->
                    totalCalories += dataPoint.getValue(Field.FIELD_CALORIES).asFloat()
                }
                listener.onCalDataReceived(totalCalories.toInt())
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitCalories", "There was a problem reading the calories data.", e)
                listener.onError(e)
            }
    }
    fun subscribeToStepData() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account == null) {
            Log.e("GoogleFitDataHandler", "Not signed in to Google Fit.")
            return
        }
        // Subscribe to recording step count data
        Fitness.getRecordingClient(context, account)
            .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .addOnSuccessListener {
                Log.i("GoogleFitDataHandler", "Successfully subscribed to record step data.")
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFitDataHandler", "Failed to subscribe to record step data.", e)
            }
    }
    interface StepDataRealTimeListener {
        fun onStepDataReceived(steps: Int)
        fun onError(e: Exception)
    }

    fun subscribeToStepData(listener: StepDataRealTimeListener) {

        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account == null) {
            Log.e("GoogleFitDataHandler", "Not signed in to Google Fit.")
            return
        }

            Fitness.getHistoryClient(context, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { result ->
                    val totalSteps = result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                    listener.onStepDataReceived(totalSteps)
                    Log.d("GoogleFitDataHandler", "Successfully retrieved daily total steps.")
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleFitDataHandler", "There was a problem getting daily total steps.", e)
                    listener.onError(e)
                }
        }


}