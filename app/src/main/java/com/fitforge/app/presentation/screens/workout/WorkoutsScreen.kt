package com.fitforge.app.presentation.screens.workout

import androidx.compose.runtime.Composable

@Composable
fun WorkoutsScreen(onExerciseClick: (String) -> Unit = {}) {
    WorkoutLibraryScreen(onExerciseClick = onExerciseClick)
}
