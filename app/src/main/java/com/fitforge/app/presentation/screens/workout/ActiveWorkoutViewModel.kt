package com.fitforge.app.presentation.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()

    private val _restTimeRemaining = MutableStateFlow(0)
    val restTimeRemaining: StateFlow<Int> = _restTimeRemaining.asStateFlow()

    private var restTimerJob: Job? = null

    fun updateReps(reps: Int) {
        _uiState.value = _uiState.value.copy(currentReps = reps)
    }

    fun updateWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(currentWeight = weight)
    }

    fun completeSet() {
        // Log the set
        // Start rest timer
        startRestTimer(90) // 90 seconds rest
    }

    fun startRestTimer(seconds: Int) {
        _isResting.value = true
        _restTimeRemaining.value = seconds

        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_restTimeRemaining.value > 0) {
                delay(1000)
                _restTimeRemaining.value = _restTimeRemaining.value - 1
            }
            _isResting.value = false
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _isResting.value = false
        _restTimeRemaining.value = 0
    }

    fun addRestTime(seconds: Int) {
        _restTimeRemaining.value = _restTimeRemaining.value + seconds
    }

    fun nextExercise() {
        // Move to next exercise
    }

    override fun onCleared() {
        super.onCleared()
        restTimerJob?.cancel()
    }
}
