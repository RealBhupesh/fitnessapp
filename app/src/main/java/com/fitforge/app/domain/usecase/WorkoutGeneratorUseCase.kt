package com.fitforge.app.domain.usecase

import com.fitforge.app.data.model.ExerciseTemplate
import com.fitforge.app.data.model.WorkoutTemplate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Workout Generator
 *
 * Generates personalized workout programs based on:
 * - User goals (muscle gain, strength, fat loss, endurance)
 * - Training experience level
 * - Available equipment
 * - Time constraints
 * - Recovery status
 * - Performance history
 */
@Singleton
class WorkoutGeneratorUseCase @Inject constructor() {

    data class WorkoutRequest(
        val goal: FitnessGoal,
        val experienceLevel: ExperienceLevel,
        val daysPerWeek: Int,
        val sessionDuration: Int, // minutes
        val equipment: List<String>,
        val targetMuscles: List<String> = emptyList(),
        val avoidExercises: List<String> = emptyList()
    )

    enum class FitnessGoal {
        MUSCLE_GAIN,    // Hypertrophy focus
        STRENGTH,       // Powerlifting style
        FAT_LOSS,       // High volume, moderate intensity
        ENDURANCE,      // Cardio + muscular endurance
        GENERAL_FITNESS // Balanced approach
    }

    enum class ExperienceLevel {
        BEGINNER,       // < 6 months training
        INTERMEDIATE,   // 6 months - 2 years
        ADVANCED        // 2+ years
    }

    /**
     * Generates a complete weekly workout program
     */
    fun generateProgram(request: WorkoutRequest): List<WorkoutTemplate> {
        return when (request.goal) {
            FitnessGoal.MUSCLE_GAIN -> generateHypertrophyProgram(request)
            FitnessGoal.STRENGTH -> generateStrengthProgram(request)
            FitnessGoal.FAT_LOSS -> generateFatLossProgram(request)
            FitnessGoal.ENDURANCE -> generateEnduranceProgram(request)
            FitnessGoal.GENERAL_FITNESS -> generateGeneralFitnessProgram(request)
        }
    }

    private fun generateHypertrophyProgram(request: WorkoutRequest): List<WorkoutTemplate> {
        val workouts = mutableListOf<WorkoutTemplate>()

        when (request.daysPerWeek) {
            3 -> {
                // Full Body 3x/week
                workouts.add(createFullBodyWorkout(request, "A"))
                workouts.add(createFullBodyWorkout(request, "B"))
                workouts.add(createFullBodyWorkout(request, "C"))
            }
            4 -> {
                // Upper/Lower split
                workouts.add(createUpperBodyWorkout(request, focus = "push"))
                workouts.add(createLowerBodyWorkout(request, focus = "quad"))
                workouts.add(createUpperBodyWorkout(request, focus = "pull"))
                workouts.add(createLowerBodyWorkout(request, focus = "hamstring"))
            }
            5 -> {
                // Push/Pull/Legs/Upper/Lower
                workouts.add(createPushWorkout(request))
                workouts.add(createPullWorkout(request))
                workouts.add(createLegWorkout(request))
                workouts.add(createUpperBodyWorkout(request, focus = "balanced"))
                workouts.add(createLowerBodyWorkout(request, focus = "balanced"))
            }
            6 -> {
                // PPL twice per week
                workouts.add(createPushWorkout(request, variant = "A"))
                workouts.add(createPullWorkout(request, variant = "A"))
                workouts.add(createLegWorkout(request, variant = "A"))
                workouts.add(createPushWorkout(request, variant = "B"))
                workouts.add(createPullWorkout(request, variant = "B"))
                workouts.add(createLegWorkout(request, variant = "B"))
            }
            else -> {
                // Default to 4-day upper/lower
                workouts.addAll(generateHypertrophyProgram(request.copy(daysPerWeek = 4)))
            }
        }

        return workouts
    }

    private fun generateStrengthProgram(request: WorkoutRequest): List<WorkoutTemplate> {
        // Powerlifting-style program focusing on compound lifts
        return listOf(
            WorkoutTemplate(
                id = UUID.randomUUID().toString(),
                name = "Heavy Squat Day",
                description = "Build leg strength with heavy squats",
                category = "strength",
                duration = request.sessionDuration,
                exercises = listOf(
                    ExerciseTemplate("barbell_squat", sets = 5, reps = "3-5", restSeconds = 180, order = 1),
                    ExerciseTemplate("romanian_deadlift", sets = 3, reps = "6-8", restSeconds = 120, order = 2),
                    ExerciseTemplate("leg_press", sets = 3, reps = "8-10", restSeconds = 90, order = 3),
                    ExerciseTemplate("leg_curl", sets = 3, reps = "10-12", restSeconds = 60, order = 4)
                ),
                intensity = "high",
                equipment = listOf("barbell", "machine"),
                targetMuscles = listOf("legs", "glutes"),
                difficulty = "advanced"
            ),
            WorkoutTemplate(
                id = UUID.randomUUID().toString(),
                name = "Heavy Bench Day",
                description = "Build chest and pressing strength",
                category = "strength",
                duration = request.sessionDuration,
                exercises = listOf(
                    ExerciseTemplate("barbell_bench", sets = 5, reps = "3-5", restSeconds = 180, order = 1),
                    ExerciseTemplate("incline_db_press", sets = 3, reps = "6-8", restSeconds = 120, order = 2),
                    ExerciseTemplate("overhead_press", sets = 3, reps = "6-8", restSeconds = 120, order = 3),
                    ExerciseTemplate("dips", sets = 3, reps = "8-10", restSeconds = 90, order = 4)
                ),
                intensity = "high",
                equipment = listOf("barbell", "dumbbell"),
                targetMuscles = listOf("chest", "shoulders", "triceps"),
                difficulty = "advanced"
            ),
            WorkoutTemplate(
                id = UUID.randomUUID().toString(),
                name = "Heavy Deadlift Day",
                description = "Build posterior chain strength",
                category = "strength",
                duration = request.sessionDuration,
                exercises = listOf(
                    ExerciseTemplate("deadlift", sets = 5, reps = "1-5", restSeconds = 240, order = 1),
                    ExerciseTemplate("barbell_row", sets = 4, reps = "6-8", restSeconds = 120, order = 2),
                    ExerciseTemplate("pullups", sets = 3, reps = "AMRAP", restSeconds = 120, order = 3),
                    ExerciseTemplate("face_pulls", sets = 3, reps = "15-20", restSeconds = 60, order = 4)
                ),
                intensity = "high",
                equipment = listOf("barbell", "cable"),
                targetMuscles = listOf("back", "hamstrings", "traps"),
                difficulty = "advanced"
            )
        )
    }

    private fun generateFatLossProgram(request: WorkoutRequest): List<WorkoutTemplate> {
        // High volume, moderate intensity, circuit-style
        return listOf(
            WorkoutTemplate(
                id = UUID.randomUUID().toString(),
                name = "Full Body Circuit",
                description = "High-intensity fat burning workout",
                category = "strength",
                duration = request.sessionDuration,
                exercises = listOf(
                    ExerciseTemplate("squats", sets = 4, reps = "12-15", restSeconds = 45, order = 1),
                    ExerciseTemplate("push_ups", sets = 4, reps = "12-15", restSeconds = 45, order = 2),
                    ExerciseTemplate("rows", sets = 4, reps = "12-15", restSeconds = 45, order = 3),
                    ExerciseTemplate("lunges", sets = 3, reps = "12-15", restSeconds = 45, order = 4),
                    ExerciseTemplate("planks", sets = 3, reps = "45-60s", restSeconds = 30, order = 5)
                ),
                intensity = "high",
                equipment = listOf("dumbbell", "bodyweight"),
                targetMuscles = listOf("full_body"),
                difficulty = "intermediate"
            )
        )
    }

    private fun generateEnduranceProgram(request: WorkoutRequest): List<WorkoutTemplate> {
        // Focus on muscular endurance and cardiovascular fitness
        return listOf(
            WorkoutTemplate(
                id = UUID.randomUUID().toString(),
                name = "Endurance Builder",
                description = "Build stamina and muscular endurance",
                category = "cardio",
                duration = request.sessionDuration,
                exercises = listOf(
                    ExerciseTemplate("burpees", sets = 4, reps = "15-20", restSeconds = 30, order = 1),
                    ExerciseTemplate("mountain_climbers", sets = 4, reps = "30-40", restSeconds = 30, order = 2),
                    ExerciseTemplate("jump_squats", sets = 4, reps = "15-20", restSeconds = 30, order = 3),
                    ExerciseTemplate("push_ups", sets = 4, reps = "20-30", restSeconds = 30, order = 4)
                ),
                intensity = "high",
                equipment = listOf("bodyweight"),
                targetMuscles = listOf("full_body"),
                difficulty = "intermediate"
            )
        )
    }

    private fun generateGeneralFitnessProgram(request: WorkoutRequest): List<WorkoutTemplate> {
        // Balanced approach to strength, endurance, and overall fitness
        return when (request.daysPerWeek) {
            3 -> listOf(
                createFullBodyWorkout(request, "A"),
                createFullBodyWorkout(request, "B"),
                createFullBodyWorkout(request, "C")
            )
            else -> listOf(
                createUpperBodyWorkout(request, "balanced"),
                createLowerBodyWorkout(request, "balanced"),
                createFullBodyWorkout(request, "cardio_focused")
            )
        }
    }

    // Helper methods to create specific workout types
    private fun createPushWorkout(request: WorkoutRequest, variant: String = "A"): WorkoutTemplate {
        return WorkoutTemplate(
            id = UUID.randomUUID().toString(),
            name = "Push Day $variant",
            description = "Chest, shoulders, and triceps workout",
            category = "strength",
            duration = request.sessionDuration,
            exercises = listOf(
                ExerciseTemplate("barbell_bench", sets = 4, reps = "8-12", restSeconds = 90, order = 1),
                ExerciseTemplate("overhead_press", sets = 3, reps = "8-12", restSeconds = 90, order = 2),
                ExerciseTemplate("incline_db_press", sets = 3, reps = "10-12", restSeconds = 75, order = 3),
                ExerciseTemplate("lateral_raises", sets = 3, reps = "12-15", restSeconds = 60, order = 4),
                ExerciseTemplate("tricep_dips", sets = 3, reps = "10-12", restSeconds = 60, order = 5)
            ),
            intensity = "high",
            equipment = listOf("barbell", "dumbbell"),
            targetMuscles = listOf("chest", "shoulders", "triceps"),
            difficulty = request.experienceLevel.name.lowercase()
        )
    }

    private fun createPullWorkout(request: WorkoutRequest, variant: String = "A"): WorkoutTemplate {
        return WorkoutTemplate(
            id = UUID.randomUUID().toString(),
            name = "Pull Day $variant",
            description = "Back and biceps workout",
            category = "strength",
            duration = request.sessionDuration,
            exercises = listOf(
                ExerciseTemplate("deadlift", sets = 4, reps = "6-8", restSeconds = 120, order = 1),
                ExerciseTemplate("pullups", sets = 3, reps = "AMRAP", restSeconds = 90, order = 2),
                ExerciseTemplate("barbell_row", sets = 3, reps = "8-12", restSeconds = 90, order = 3),
                ExerciseTemplate("face_pulls", sets = 3, reps = "15-20", restSeconds = 60, order = 4),
                ExerciseTemplate("barbell_curl", sets = 3, reps = "10-12", restSeconds = 60, order = 5)
            ),
            intensity = "high",
            equipment = listOf("barbell", "cable"),
            targetMuscles = listOf("back", "biceps"),
            difficulty = request.experienceLevel.name.lowercase()
        )
    }

    private fun createLegWorkout(request: WorkoutRequest, variant: String = "A"): WorkoutTemplate {
        return WorkoutTemplate(
            id = UUID.randomUUID().toString(),
            name = "Leg Day $variant",
            description = "Complete lower body workout",
            category = "strength",
            duration = request.sessionDuration,
            exercises = listOf(
                ExerciseTemplate("barbell_squat", sets = 4, reps = "8-12", restSeconds = 120, order = 1),
                ExerciseTemplate("romanian_deadlift", sets = 3, reps = "10-12", restSeconds = 90, order = 2),
                ExerciseTemplate("leg_press", sets = 3, reps = "12-15", restSeconds = 90, order = 3),
                ExerciseTemplate("leg_curl", sets = 3, reps = "12-15", restSeconds = 60, order = 4),
                ExerciseTemplate("calf_raises", sets = 4, reps = "15-20", restSeconds = 45, order = 5)
            ),
            intensity = "high",
            equipment = listOf("barbell", "machine"),
            targetMuscles = listOf("quadriceps", "hamstrings", "glutes", "calves"),
            difficulty = request.experienceLevel.name.lowercase()
        )
    }

    private fun createFullBodyWorkout(request: WorkoutRequest, variant: String): WorkoutTemplate {
        return WorkoutTemplate(
            id = UUID.randomUUID().toString(),
            name = "Full Body $variant",
            description = "Complete full body workout",
            category = "strength",
            duration = request.sessionDuration,
            exercises = listOf(
                ExerciseTemplate("barbell_squat", sets = 3, reps = "8-12", restSeconds = 90, order = 1),
                ExerciseTemplate("barbell_bench", sets = 3, reps = "8-12", restSeconds = 90, order = 2),
                ExerciseTemplate("barbell_row", sets = 3, reps = "8-12", restSeconds = 90, order = 3),
                ExerciseTemplate("overhead_press", sets = 3, reps = "8-12", restSeconds = 75, order = 4),
                ExerciseTemplate("romanian_deadlift", sets = 2, reps = "10-12", restSeconds = 75, order = 5)
            ),
            intensity = "medium",
            equipment = listOf("barbell"),
            targetMuscles = listOf("full_body"),
            difficulty = request.experienceLevel.name.lowercase()
        )
    }

    private fun createUpperBodyWorkout(request: WorkoutRequest, focus: String): WorkoutTemplate {
        return WorkoutTemplate(
            id = UUID.randomUUID().toString(),
            name = "Upper Body ($focus focus)",
            description = "Upper body strength and hypertrophy",
            category = "strength",
            duration = request.sessionDuration,
            exercises = listOf(
                ExerciseTemplate("barbell_bench", sets = 4, reps = "8-10", restSeconds = 90, order = 1),
                ExerciseTemplate("barbell_row", sets = 4, reps = "8-10", restSeconds = 90, order = 2),
                ExerciseTemplate("overhead_press", sets = 3, reps = "8-12", restSeconds = 75, order = 3),
                ExerciseTemplate("pullups", sets = 3, reps = "AMRAP", restSeconds = 75, order = 4),
                ExerciseTemplate("dips", sets = 3, reps = "10-12", restSeconds = 60, order = 5)
            ),
            intensity = "high",
            equipment = listOf("barbell", "bodyweight"),
            targetMuscles = listOf("chest", "back", "shoulders", "arms"),
            difficulty = request.experienceLevel.name.lowercase()
        )
    }

    private fun createLowerBodyWorkout(request: WorkoutRequest, focus: String): WorkoutTemplate {
        return createLegWorkout(request, focus)
    }

    /**
     * Recommends next workout based on recovery and history
     */
    fun recommendNextWorkout(
        completedWorkouts: List<String>,
        recoveryScore: Int,
        daysSinceLastLegDay: Int
    ): String {
        return when {
            recoveryScore < 60 -> "Active Recovery (Light Cardio & Stretching)"
            daysSinceLastLegDay >= 4 -> "Leg Day"
            "push" !in completedWorkouts.takeLast(3) -> "Push Day"
            "pull" !in completedWorkouts.takeLast(3) -> "Pull Day"
            else -> "Full Body or Active Recovery"
        }
    }
}
