package com.fitforge.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.model.Workout
import com.fitforge.app.data.repository.NutritionRepository
import com.fitforge.app.data.repository.UserRepository
import com.fitforge.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "Alex",
    val currentStreak: Int = 47,
    val todaysWorkout: Workout? = null,
    val caloriesConsumed: Int = 1847,
    val caloriesTarget: Int = 2400,
    val proteinConsumed: Float = 142f,
    val proteinTarget: Float = 180f,
    val carbsConsumed: Float = 198f,
    val carbsTarget: Float = 270f,
    val fatsConsumed: Float = 51f,
    val fatsTarget: Float = 67f,
    val currentWeight: Float = 178.4f,
    val weeklyWeightChange: Float = -2.1f,
    val bodyFatPercentage: Float = 16.2f,
    val muscleMass: Float = 149.6f,
    val weeklyWorkoutsCompleted: Int = 4,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                // Load user data, today's workout, nutrition, etc.
                // This is placeholder data - in real app would load from repositories
                _uiState.value = HomeUiState(
                    userName = "Alex",
                    currentStreak = 47
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
