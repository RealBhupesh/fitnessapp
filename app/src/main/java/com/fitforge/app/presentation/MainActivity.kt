package com.fitforge.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitforge.app.presentation.navigation.BottomNavigationBar
import com.fitforge.app.presentation.navigation.Screen
import com.fitforge.app.presentation.screens.home.HomeScreen
import com.fitforge.app.presentation.screens.nutrition.NutritionScreen
import com.fitforge.app.presentation.screens.profile.ProfileScreen
import com.fitforge.app.presentation.screens.progress.ProgressScreen
import com.fitforge.app.presentation.screens.workout.WorkoutsScreen
import com.fitforge.app.presentation.theme.FitForgeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FitForgeApp()
                }
            }
        }
    }
}

@Composable
fun FitForgeApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToWorkout = { workoutId ->
                        // Navigate to workout detail
                    },
                    onNavigateToNutrition = {
                        navController.navigate(Screen.Nutrition.route)
                    }
                )
            }
            composable(Screen.Workouts.route) {
                WorkoutsScreen()
            }
            composable(Screen.Nutrition.route) {
                NutritionScreen()
            }
            composable(Screen.Progress.route) {
                ProgressScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
