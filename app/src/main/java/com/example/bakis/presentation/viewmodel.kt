package com.example.bakis.presentation


import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val _stepCount = MutableStateFlow("0")
    val stepCount = _stepCount.asStateFlow()

    init {
        fetchStepCount()
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

