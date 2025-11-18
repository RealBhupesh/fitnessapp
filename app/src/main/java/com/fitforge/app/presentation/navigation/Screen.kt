package com.fitforge.app.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workouts : Screen("workouts")
    object Nutrition : Screen("nutrition")
    object Progress : Screen("progress")
    object Profile : Screen("profile")

    // Detail screens
    object WorkoutDetail : Screen("workout/{workoutId}") {
        fun createRoute(workoutId: String) = "workout/$workoutId"
    }
    object ActiveWorkout : Screen("active_workout/{workoutId}") {
        fun createRoute(workoutId: String) = "active_workout/$workoutId"
    }
    object MealDetail : Screen("meal/{mealId}") {
        fun createRoute(mealId: String) = "meal/$mealId"
    }
    object ScanFood : Screen("scan_food")
}
