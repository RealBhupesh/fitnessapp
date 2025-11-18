package com.fitforge.app.presentation.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitforge.app.data.database.ExerciseDatabase
import com.fitforge.app.data.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutLibraryViewModel @Inject constructor() : ViewModel() {

    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    init {
        loadExercises()
        observeFilters()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _allExercises.value = ExerciseDatabase.getAllExercises()
            filterExercises()
        }
    }

    private fun observeFilters() {
        viewModelScope.launch {
            combine(
                _allExercises,
                _searchQuery,
                _selectedCategory
            ) { exercises, query, category ->
                filterExercisesList(exercises, query, category)
            }.collect { filtered ->
                _exercises.value = filtered
            }
        }
    }

    private fun filterExercisesList(
        exercises: List<Exercise>,
        query: String,
        category: String
    ): List<Exercise> {
        var filtered = exercises

        // Filter by category
        if (category != "All") {
            filtered = filtered.filter {
                it.category.equals(category, ignoreCase = true)
            }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.primaryMuscles.any { muscle -> muscle.contains(query, ignoreCase = true) }
            }
        }

        return filtered
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    private fun filterExercises() {
        _exercises.value = filterExercisesList(
            _allExercises.value,
            _searchQuery.value,
            _selectedCategory.value
        )
    }
}
