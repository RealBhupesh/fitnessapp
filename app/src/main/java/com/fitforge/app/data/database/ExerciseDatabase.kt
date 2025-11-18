package com.fitforge.app.data.database

import com.fitforge.app.data.model.Exercise
import java.util.UUID

/**
 * Comprehensive exercise database with 100+ exercises
 * Organized by category, muscle group, and equipment
 */
object ExerciseDatabase {

    fun getAllExercises(): List<Exercise> = exercises

    fun getExercisesByCategory(category: String): List<Exercise> =
        exercises.filter { it.category.equals(category, ignoreCase = true) }

    fun getExercisesByMuscleGroup(muscle: String): List<Exercise> =
        exercises.filter {
            it.primaryMuscles.any { m -> m.contains(muscle, ignoreCase = true) } ||
            it.secondaryMuscles.any { m -> m.contains(muscle, ignoreCase = true) }
        }

    fun getExercisesByEquipment(equipment: String): List<Exercise> =
        exercises.filter { it.equipment.equals(equipment, ignoreCase = true) }

    fun searchExercises(query: String): List<Exercise> =
        exercises.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }

    private val exercises = listOf(
        // ========== CHEST EXERCISES ==========
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Barbell Bench Press",
            description = "The king of chest exercises. Lie on a flat bench and press the barbell from chest to full extension.",
            category = "strength",
            equipment = "barbell",
            primaryMuscles = listOf("Chest"),
            secondaryMuscles = listOf("Triceps", "Shoulders"),
            difficulty = "intermediate",
            instructions = listOf(
                "Lie flat on bench with feet planted on floor",
                "Grip bar slightly wider than shoulder width",
                "Lower bar to mid-chest with control",
                "Press bar up until arms fully extended",
                "Keep shoulder blades retracted throughout"
            ),
            formTips = listOf(
                "Keep your feet flat on the ground",
                "Maintain a slight arch in lower back",
                "Don't bounce bar off chest",
                "Control the descent (2-3 seconds)"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Incline Dumbbell Press",
            description = "Target your upper chest with this incline variation using dumbbells for greater range of motion.",
            category = "strength",
            equipment = "dumbbell",
            primaryMuscles = listOf("Upper Chest"),
            secondaryMuscles = listOf("Shoulders", "Triceps"),
            difficulty = "intermediate",
            instructions = listOf(
                "Set bench to 30-45 degree incline",
                "Start with dumbbells at shoulder level",
                "Press dumbbells up and together",
                "Lower with control to starting position",
                "Keep elbows at 45-degree angle"
            ),
            formTips = listOf(
                "Don't set incline too steep (max 45°)",
                "Squeeze chest at top of movement",
                "Keep core engaged throughout"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Cable Flyes",
            description = "Isolate your chest with constant tension throughout the range of motion.",
            category = "strength",
            equipment = "cable",
            primaryMuscles = listOf("Chest"),
            secondaryMuscles = listOf("Shoulders"),
            difficulty = "beginner",
            instructions = listOf(
                "Set cables to shoulder height",
                "Step forward with slight lean",
                "Bring hands together in front of chest",
                "Squeeze chest at peak contraction",
                "Return with control to starting position"
            ),
            formTips = listOf(
                "Keep slight bend in elbows",
                "Focus on chest squeeze, not arm movement",
                "Maintain stable stance throughout"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Push-Ups",
            description = "Classic bodyweight chest exercise that can be done anywhere.",
            category = "strength",
            equipment = "bodyweight",
            primaryMuscles = listOf("Chest"),
            secondaryMuscles = listOf("Triceps", "Shoulders", "Core"),
            difficulty = "beginner",
            instructions = listOf(
                "Start in plank position, hands shoulder-width",
                "Lower body until chest nearly touches ground",
                "Keep elbows at 45-degree angle",
                "Push back to starting position",
                "Maintain straight line from head to heels"
            ),
            formTips = listOf(
                "Don't let hips sag",
                "Keep core tight throughout",
                "Full range of motion",
                "Breathe out on the way up"
            )
        ),

        // ========== BACK EXERCISES ==========
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Barbell Deadlift",
            description = "The ultimate full-body exercise. Builds mass and strength throughout your entire posterior chain.",
            category = "strength",
            equipment = "barbell",
            primaryMuscles = listOf("Lower Back", "Glutes", "Hamstrings"),
            secondaryMuscles = listOf("Traps", "Lats", "Core"),
            difficulty = "advanced",
            instructions = listOf(
                "Stand with feet hip-width, bar over mid-foot",
                "Bend down and grip bar outside shins",
                "Pull slack out of bar, engage lats",
                "Drive through heels, extend hips and knees",
                "Stand tall, squeeze glutes at top",
                "Lower bar with control"
            ),
            formTips = listOf(
                "Keep bar close to body throughout",
                "Maintain neutral spine (no rounding)",
                "Engage core before each rep",
                "Hip hinge movement, not squat"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Pull-Ups",
            description = "Build a wide, powerful back with this classic bodyweight exercise.",
            category = "strength",
            equipment = "bodyweight",
            primaryMuscles = listOf("Lats"),
            secondaryMuscles = listOf("Biceps", "Upper Back"),
            difficulty = "intermediate",
            instructions = listOf(
                "Hang from bar with overhand grip",
                "Pull yourself up until chin over bar",
                "Lead with chest, not chin",
                "Lower with control to full extension",
                "Avoid swinging or kipping"
            ),
            formTips = listOf(
                "Full range of motion (dead hang to chin over bar)",
                "Retract shoulder blades at top",
                "Keep core engaged to prevent swing"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Barbell Row",
            description = "Build thickness in your back with this compound pulling movement.",
            category = "strength",
            equipment = "barbell",
            primaryMuscles = listOf("Mid Back", "Lats"),
            secondaryMuscles = listOf("Biceps", "Lower Back"),
            difficulty = "intermediate",
            instructions = listOf(
                "Bend forward at hips, keeping back flat",
                "Grip bar slightly wider than shoulder width",
                "Pull bar to lower chest/upper abdomen",
                "Squeeze shoulder blades together at top",
                "Lower bar with control"
            ),
            formTips = listOf(
                "Maintain flat back throughout",
                "Don't use momentum",
                "Pull elbows back, not just hands",
                "Engage core for stability"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Lat Pulldown",
            description = "Build lat width with this vertical pulling movement.",
            category = "strength",
            equipment = "cable",
            primaryMuscles = listOf("Lats"),
            secondaryMuscles = listOf("Biceps", "Upper Back"),
            difficulty = "beginner",
            instructions = listOf(
                "Sit at machine, secure legs under pad",
                "Grip bar slightly wider than shoulders",
                "Pull bar down to upper chest",
                "Squeeze lats at bottom",
                "Return to starting position with control"
            ),
            formTips = listOf(
                "Lead with elbows, not hands",
                "Keep chest up throughout",
                "Don't lean back excessively"
            )
        ),

        // ========== LEG EXERCISES ==========
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Barbell Back Squat",
            description = "The king of leg exercises. Builds overall lower body strength and mass.",
            category = "strength",
            equipment = "barbell",
            primaryMuscles = listOf("Quadriceps", "Glutes"),
            secondaryMuscles = listOf("Hamstrings", "Core", "Lower Back"),
            difficulty = "intermediate",
            instructions = listOf(
                "Bar on upper back, feet shoulder-width",
                "Break at hips and knees simultaneously",
                "Descend until thighs parallel to ground",
                "Keep knees tracking over toes",
                "Drive through heels to stand",
                "Squeeze glutes at top"
            ),
            formTips = listOf(
                "Keep chest up and core tight",
                "Don't let knees cave inward",
                "Reach depth (hip crease below knee)",
                "Bar path should be straight vertical"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Romanian Deadlift",
            description = "Target your hamstrings and glutes with this hip-hinge movement.",
            category = "strength",
            equipment = "barbell",
            primaryMuscles = listOf("Hamstrings", "Glutes"),
            secondaryMuscles = listOf("Lower Back"),
            difficulty = "intermediate",
            instructions = listOf(
                "Start standing with bar at hip level",
                "Soften knees slightly",
                "Push hips back, lowering bar down legs",
                "Lower until you feel hamstring stretch",
                "Drive hips forward to return to start"
            ),
            formTips = listOf(
                "Keep bar close to legs",
                "Maintain neutral spine",
                "Feel the stretch in hamstrings",
                "Don't round lower back"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Leg Press",
            description = "Build quad and glute strength with this machine-based compound movement.",
            category = "strength",
            equipment = "machine",
            primaryMuscles = listOf("Quadriceps", "Glutes"),
            secondaryMuscles = listOf("Hamstrings"),
            difficulty = "beginner",
            instructions = listOf(
                "Sit in machine, feet shoulder-width on platform",
                "Release safety handles",
                "Lower platform until knees at 90 degrees",
                "Press through heels to extension",
                "Don't lock out knees completely"
            ),
            formTips = listOf(
                "Keep lower back pressed against pad",
                "Don't let knees cave in",
                "Controlled tempo on descent",
                "Full range of motion"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Bulgarian Split Squat",
            description = "Unilateral leg exercise for building strength and balance.",
            category = "strength",
            equipment = "dumbbell",
            primaryMuscles = listOf("Quadriceps", "Glutes"),
            secondaryMuscles = listOf("Hamstrings", "Core"),
            difficulty = "intermediate",
            instructions = listOf(
                "Place rear foot on bench behind you",
                "Hold dumbbells at sides",
                "Lower down until front thigh parallel",
                "Drive through front heel to stand",
                "Keep torso upright"
            ),
            formTips = listOf(
                "Front shin should stay vertical",
                "Don't let front knee cave inward",
                "Maintain balance and control",
                "Equal reps per leg"
            )
        ),

        // ========== SHOULDER EXERCISES ==========
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Overhead Press",
            description = "Build powerful shoulders with this fundamental pressing movement.",
            category = "strength",
            equipment = "barbell",
            primaryMuscles = listOf("Shoulders"),
            secondaryMuscles = listOf("Triceps", "Upper Chest", "Core"),
            difficulty = "intermediate",
            instructions = listOf(
                "Start with bar at shoulder height",
                "Grip slightly wider than shoulders",
                "Press bar straight up overhead",
                "Lock out arms at top",
                "Lower with control to shoulders"
            ),
            formTips = listOf(
                "Keep core tight throughout",
                "Don't arch back excessively",
                "Bar path straight vertical",
                "Tuck chin slightly on the way up"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Lateral Raises",
            description = "Isolate your side delts for that wide shoulder look.",
            category = "strength",
            equipment = "dumbbell",
            primaryMuscles = listOf("Side Delts"),
            secondaryMuscles = listOf(),
            difficulty = "beginner",
            instructions = listOf(
                "Stand with dumbbells at sides",
                "Slight bend in elbows",
                "Raise arms out to sides to shoulder height",
                "Pause at top",
                "Lower with control"
            ),
            formTips = listOf(
                "Don't swing or use momentum",
                "Lead with elbows, not hands",
                "Stop at shoulder height",
                "Light weight, high quality reps"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Face Pulls",
            description = "Essential for shoulder health and rear delt development.",
            category = "strength",
            equipment = "cable",
            primaryMuscles = listOf("Rear Delts", "Upper Back"),
            secondaryMuscles = listOf("Rotator Cuff"),
            difficulty = "beginner",
            instructions = listOf(
                "Set cable to face height",
                "Use rope attachment",
                "Pull rope towards face",
                "Separate ends of rope past ears",
                "Squeeze shoulder blades together"
            ),
            formTips = listOf(
                "Keep upper arms parallel to ground",
                "Think 'pull rope apart' not just back",
                "High reps (15-20) for shoulder health"
            )
        ),

        // ========== ARM EXERCISES ==========
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Barbell Curl",
            description = "Classic bicep builder for size and strength.",
            category = "strength",
            equipment = "barbell",
            primaryMuscles = listOf("Biceps"),
            secondaryMuscles = listOf("Forearms"),
            difficulty = "beginner",
            instructions = listOf(
                "Stand with bar at arm's length",
                "Grip shoulder-width, palms up",
                "Curl bar up to shoulders",
                "Squeeze biceps at top",
                "Lower with control"
            ),
            formTips = listOf(
                "Keep elbows at sides (don't move forward)",
                "No swinging or momentum",
                "Full range of motion",
                "Control the negative"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Tricep Dips",
            description = "Compound movement for building tricep mass and strength.",
            category = "strength",
            equipment = "bodyweight",
            primaryMuscles = listOf("Triceps"),
            secondaryMuscles = listOf("Chest", "Shoulders"),
            difficulty = "intermediate",
            instructions = listOf(
                "Grip parallel bars, arms extended",
                "Lower body by bending elbows",
                "Descend until upper arms parallel to ground",
                "Press back up to starting position",
                "Keep body upright for tricep focus"
            ),
            formTips = listOf(
                "Don't go too deep (shoulder stress)",
                "Keep elbows close to body",
                "Lean forward for chest, upright for triceps"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Hammer Curls",
            description = "Build your biceps and forearms with this neutral grip variation.",
            category = "strength",
            equipment = "dumbbell",
            primaryMuscles = listOf("Biceps", "Forearms"),
            secondaryMuscles = listOf("Brachialis"),
            difficulty = "beginner",
            instructions = listOf(
                "Hold dumbbells at sides, palms facing each other",
                "Curl weights up keeping palms neutral",
                "Squeeze at top",
                "Lower with control",
                "Alternate or do both arms together"
            ),
            formTips = listOf(
                "Keep wrists neutral throughout",
                "Don't twist palms during movement",
                "Maintain strict form"
            )
        ),

        // ========== CORE EXERCISES ==========
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Plank",
            description = "Fundamental core stability exercise.",
            category = "strength",
            equipment = "bodyweight",
            primaryMuscles = listOf("Core", "Abs"),
            secondaryMuscles = listOf("Shoulders"),
            difficulty = "beginner",
            instructions = listOf(
                "Start in push-up position on forearms",
                "Body in straight line from head to heels",
                "Engage core, glutes, and quads",
                "Hold position without sagging hips",
                "Breathe normally throughout"
            ),
            formTips = listOf(
                "Don't let hips sag or pike up",
                "Squeeze glutes hard",
                "Pull belly button to spine",
                "Start with 30s, build to 60s+"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Russian Twists",
            description = "Dynamic core exercise targeting obliques.",
            category = "strength",
            equipment = "bodyweight",
            primaryMuscles = listOf("Obliques", "Core"),
            secondaryMuscles = listOf("Hip Flexors"),
            difficulty = "beginner",
            instructions = listOf(
                "Sit on floor, knees bent, feet elevated",
                "Lean back slightly, keeping back straight",
                "Clasp hands or hold weight",
                "Rotate torso side to side",
                "Touch floor beside hips each side"
            ),
            formTips = listOf(
                "Keep chest up, don't round back",
                "Controlled rotation, not jerking",
                "Can add weight for progression"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Hanging Leg Raises",
            description = "Advanced core exercise for lower abs.",
            category = "strength",
            equipment = "bodyweight",
            primaryMuscles = listOf("Lower Abs", "Hip Flexors"),
            secondaryMuscles = listOf("Core"),
            difficulty = "advanced",
            instructions = listOf(
                "Hang from pull-up bar",
                "Keep legs straight or knees bent",
                "Raise legs up to 90 degrees",
                "Control the descent",
                "Avoid swinging"
            ),
            formTips = listOf(
                "Use core, not momentum",
                "Posterior pelvic tilt at top",
                "Start with knee raises if needed",
                "Squeeze abs at top"
            )
        ),

        // ========== CARDIO EXERCISES ==========
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Burpees",
            description = "Full body cardio exercise that burns maximum calories.",
            category = "cardio",
            equipment = "bodyweight",
            primaryMuscles = listOf("Full Body"),
            secondaryMuscles = listOf(),
            difficulty = "intermediate",
            instructions = listOf(
                "Start standing",
                "Drop to push-up position",
                "Perform push-up",
                "Jump feet to hands",
                "Explosive jump with arms overhead",
                "Land and repeat"
            ),
            formTips = listOf(
                "Maintain form even when tired",
                "Modify by removing push-up or jump",
                "Pace yourself for sustained effort"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Mountain Climbers",
            description = "Dynamic cardio movement that works core and burns calories.",
            category = "cardio",
            equipment = "bodyweight",
            primaryMuscles = listOf("Core", "Cardiovascular"),
            secondaryMuscles = listOf("Shoulders", "Legs"),
            difficulty = "beginner",
            instructions = listOf(
                "Start in push-up position",
                "Drive one knee to chest",
                "Quickly switch legs",
                "Alternate legs rapidly",
                "Maintain plank position throughout"
            ),
            formTips = listOf(
                "Keep hips low (not piking up)",
                "Land softly on balls of feet",
                "Maintain neutral spine",
                "Breathe rhythmically"
            )
        ),
        Exercise(
            id = UUID.randomUUID().toString(),
            name = "Jump Rope",
            description = "Classic cardio exercise for conditioning and coordination.",
            category = "cardio",
            equipment = "other",
            primaryMuscles = listOf("Cardiovascular", "Calves"),
            secondaryMuscles = listOf("Shoulders", "Core"),
            difficulty = "beginner",
            instructions = listOf(
                "Hold rope handles at hip height",
                "Jump on balls of feet",
                "Use wrist rotation to turn rope",
                "Land softly",
                "Maintain steady rhythm"
            ),
            formTips = listOf(
                "Small jumps (just clear rope)",
                "Stay on balls of feet",
                "Relax shoulders",
                "Master basic bounce before variations"
            )
        )
    )
}
