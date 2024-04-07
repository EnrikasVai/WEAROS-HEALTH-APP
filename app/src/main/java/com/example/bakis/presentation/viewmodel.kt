package com.example.bakis.presentation


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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




    init {
        fetchStepCount()
        fetchSleepCount()
        fetchLastHeartRateData()
        fetchTodaysHeartRateData()
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

