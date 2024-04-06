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


class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val _stepCount = MutableStateFlow("0")
    val stepCount = _stepCount.asStateFlow()

    private val _sleepCount = MutableStateFlow("0")
    val sleepCount = _sleepCount.asStateFlow()

    private val _heartRate = MutableLiveData<String>()
    val heartRate: LiveData<String> = _heartRate

    private val _bpmValues = mutableListOf<Float>()
    private val _bpmReady = MutableLiveData<Boolean>(false)
    val bpmReady: LiveData<Boolean> = _bpmReady
    private val _bpmValue = MutableLiveData<Float>()
    val bpmValue: LiveData<Float> = _bpmValue

    init {
        fetchStepCount()
        fetchSleepCount()
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
            repeat(30) { // Collect data for 30 seconds
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

