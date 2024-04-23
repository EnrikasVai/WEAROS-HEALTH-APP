package com.example.bakis.presentation


import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class MinMaxHeartRate(val minBpm: Float, val maxBpm: Float)
class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val _stepCount = MutableStateFlow("0")
    val stepCount = _stepCount.asStateFlow()

    private val _sleepCount = MutableStateFlow("0")
    val sleepCount = _sleepCount.asStateFlow()

    private val _heartRate = MutableLiveData<String>()
    val heartRate: LiveData<String> = _heartRate

    private val _todaysHeartRateData = MutableLiveData<List<HeartRateData>>()
    val todaysHeartRateData: LiveData<List<HeartRateData>> = _todaysHeartRateData

    private val _minMaxHeartRate = MutableLiveData<MinMaxHeartRate?>()
    val minMaxHeartRate: LiveData<MinMaxHeartRate?> = _minMaxHeartRate

    private val _bpmValues = mutableListOf<Float>()
    private val _bpmReady = MutableLiveData<Boolean>(false)
    val bpmReady: LiveData<Boolean> = _bpmReady
    private val _bpmValue = MutableLiveData<Float>()
    val bpmValue: LiveData<Float> = _bpmValue
    data class HeartRateData(val bpm: Float, val timeString: String)
    private val _lastHeartRateData = MutableLiveData<HeartRateData?>()
    val lastHeartRateData: LiveData<HeartRateData?> = _lastHeartRateData

    private val _todayDistance = MutableStateFlow(0.0)
    val todayDistance = _todayDistance.asStateFlow()
    private val _todayMoveMinutes = MutableStateFlow(0.0)
    val todayMoveMinutes = _todayMoveMinutes.asStateFlow()
    private val _todayAverageSpeed = MutableStateFlow(0.0)
    val todayAverageSpeed = _todayAverageSpeed.asStateFlow()

    data class SleepSegmentData(val startTime: String, val endTime: String, val type: Int)
    private val _sleepSegments = MutableLiveData<List<SleepSegmentData>>()
    val sleepSegments: LiveData<List<SleepSegmentData>> = _sleepSegments

    private val _todayCalories = MutableStateFlow(0.0)
    val todayCalories = _todayCalories.asStateFlow()

    private val _calCount = MutableStateFlow("0")
    val calCount = _calCount.asStateFlow()


    fun fetchSleepSegments() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.readSleepSegments(object : GoogleFitDataHandler.SleepSegmentListener {
            override fun onSleepSegmentReceived(sleepSegments: List<GoogleFitDataHandler.SleepSegment>) {
                val sleepSegmentDataList = sleepSegments.map { segment ->
                    SleepSegmentData(segment.startTime, segment.endTime, segment.type)
                }
                _sleepSegments.postValue(sleepSegmentDataList)
            }

            override fun onError(e: Exception) {
                Log.e("FitnessViewModel", "Error fetching sleep segment data", e)
            }
        })
    }


    fun fetchLastHeartRateData() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.readLastHeartRateData(object : GoogleFitDataHandler.LastHeartRateDataListener {
            override fun onHeartRateDataReceived(bpm: Float, timeString: String) {
                _lastHeartRateData.postValue(HeartRateData(bpm, timeString))
            }

            override fun onError(e: Exception) {
                Log.e("FitnessViewModel", "Error fetching heart rate data", e)
            }
        })
    }
    fun fetchTodaysHeartRateData() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.readTodaysHeartRateData(object : GoogleFitDataHandler.HeartRateDataListenerToday {
            override fun onHeartRateDataReceived(readings: List<Pair<String, Float>>) {
                val heartRateDataList = readings.map { (timeString, bpm) ->
                    HeartRateData(bpm, timeString)
                }
                _todaysHeartRateData.postValue(heartRateDataList)
            }
            override fun onError(e: Exception) {
                Log.e("FitnessViewModel", "Error fetching today's heart rate data", e)
            }
        })
    }
    fun fetchMinMaxHeartRateToday() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.fetchMinMaxHeartRateForToday(object :
            GoogleFitDataHandler.MinMaxHeartRateListener {
            override fun onMinMaxHeartRateFound(minBpm: Float, maxBpm: Float) {
                _minMaxHeartRate.postValue(MinMaxHeartRate(minBpm, maxBpm))
            }
            override fun onError(e: Exception) {
                Log.e("FitnessViewModel", "Error fetching min/max heart rate", e)
            }
        })
    }
    fun fetchTodaysNutrition() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.readTodayCaloriesData(object : GoogleFitDataHandler.TodayCaloriesListener {
            override fun onCaloriesDataReceived(calories: Double) {
                viewModelScope.launch {
                    _todayCalories.value = calories
                }
            }
            override fun onError(e: Exception) {
                Log.e("FitnessViewModel", "Error fetching fitness data", e)
            }
        })
    }




    init {
        fetchStepCount()
        fetchSleepCount()
        fetchLastHeartRateData()
        fetchTodaysHeartRateData()
        fetchFitnessData()
        fetchSleepSegments()
        fetchTodaysNutrition()
        fetchCalCount()
    }
    fun fetchFitnessData() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())

        googleFitDataHandler.readFitnessData(object : GoogleFitDataHandler.TodayDataListener {
            override fun onStepDataReceived(distance: Double, moveMinutes: Double, averageSpeed: Double) {
                viewModelScope.launch {
                    _todayDistance.value = distance
                    _todayMoveMinutes.value = moveMinutes
                    _todayAverageSpeed.value = averageSpeed
                }
            }
            override fun onError(e: Exception) {
                Log.e("FitnessViewModel", "Error fetching fitness data", e)
            }
        })
    }

    fun fetchStepCount() {
        // Use `getApplication<Application>()` to get the application context
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.readStepData(object : GoogleFitDataHandler.StepDataListener {
            override fun onStepDataReceived(stepCount: Int) {
                _stepCount.value = stepCount.toString()
            }

            override fun onError(e: Exception) {
                Log.e("HomeViewModel", "Error fetching step count", e)
            }
        })
    }
    fun fetchSleepCount() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.readSleepData(object : GoogleFitDataHandler.SleepDataListener {
            override fun onSleepDataReceived(sleepCount: Int) {
                _sleepCount.value = sleepCount.toString()
            }

            override fun onError(e: Exception) {
                Log.e("HomeViewModel", "Error fetching sleep count", e)
            }
        })
    }
    //bpm calculator
    fun collectHeartRateFor30Seconds() {
        _bpmValues.clear() // Clear previous BPM values
        _bpmReady.value = false // Reset the BPM ready state
        viewModelScope.launch {
            repeat(10) { // Collect data for 30 seconds
                delay(1000) // Delay for a second between each data collection
                // Assume subscribeToHeartRateData() triggers a single data collection
                subscribeToHeartRateData()
            }
            calculateAndShowBPM()
        }
    }
    private fun calculateAndShowBPM() {
        if (_bpmValues.isNotEmpty()) {
            val averageBpm = _bpmValues.average().toFloat()
            _bpmValue.postValue(averageBpm)
            _bpmReady.postValue(true)

            val currentTimeMillis = System.currentTimeMillis()
            val googleFitDataHandler = GoogleFitDataHandler(getApplication())
            googleFitDataHandler.writeHeartRateData(averageBpm, currentTimeMillis)
        }
    }


    fun subscribeToHeartRateData() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.subscribeToHeartRate(object : GoogleFitDataHandler.HeartRateDataListener {
            override fun onHeartRateDataReceived(bpm: Float) {
                _bpmValues.add(bpm) // Collect each BPM value
            }

            override fun onError(e: Exception) {
                Log.e("FitnessViewModel", "Error subscribing to heart rate data", e)
            }
        })
    }
    fun addCaloriesToGoogleFit(context: Context, calories: Float, startTime: Long, endTime: Long) {
        val dataSource = DataSource.Builder()
            .setAppPackageName(context)
            .setDataType(DataType.TYPE_NUTRITION)
            .setType(DataSource.TYPE_RAW)
            .build()

        val nutrientsMap = mapOf(Field.NUTRIENT_CALORIES to calories)

        // Create the data point with specific start and end times
        val dataPoint = DataPoint.builder(dataSource)
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .setField(Field.FIELD_NUTRIENTS, nutrientsMap)
            .build()

        // Create the data set
        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()

        // Insert the data set into Google Fit
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .insertData(dataSet)
            .addOnSuccessListener {
                // Data insert was successful
                Log.i("GoogleFitCalories", "Successfully added calories to Google Fit.")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("GoogleFitCalories", "Failed to add calories to Google Fit.", e)
            }
    }
    fun addCalories(context: Context, calories: Float, startTime: Long, endTime: Long) {
        viewModelScope.launch {
            try {
                addCaloriesToGoogleFit(context, calories, startTime, endTime)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error adding calories to Google Fit", e)
            }
        }
    }
    fun fetchCalCount() {
        val googleFitDataHandler = GoogleFitDataHandler(getApplication())
        googleFitDataHandler.readCaloriesData(object : GoogleFitDataHandler.CaloriesDataListener {
            override fun onCalDataReceived(calCount: Int) {
                _calCount.value = calCount.toString()
            }

            override fun onError(e: Exception) {
                Log.e("HomeViewModel", "Error fetching calorie count", e)
            }
        })
    }
}
class FitnessViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

