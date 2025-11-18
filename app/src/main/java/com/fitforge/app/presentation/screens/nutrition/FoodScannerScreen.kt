package com.fitforge.app.presentation.screens.nutrition

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitforge.app.presentation.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodScannerScreen(
    onFoodScanned: (FoodScanResult) -> Unit = {},
    onClose: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<FoodScanResult?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            // Camera Preview would go here
            CameraPreviewPlaceholder()

            // Scanning Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar
                TopAppBar(
                    title = { Text("Scan Food") },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close", tint = PureWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = PureWhite
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Scanning Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(androidx.compose.ui.graphics.Color.Transparent)
                ) {
                    // Corner indicators
                    ScanningCorners()

                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = ForgeRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Instructions
                Text(
                    text = "Point camera at your food",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Include entire plate for best results",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Capture Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = {
                            isScanning = true
                            // Simulate scan
                            scanResult = FoodScanResult(
                                foods = listOf(
                                    DetectedFood("Grilled Chicken Breast", 8f, 368, 69f, 0f, 8f),
                                    DetectedFood("Brown Rice", 1.5f, 324, 7f, 68f, 3f),
                                    DetectedFood("Broccoli", 1f, 55, 4f, 11f, 0f)
                                )
                            )
                            isScanning = false
                        },
                        modifier = Modifier.size(80.dp),
                        containerColor = ForgeRed
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Capture",
                            modifier = Modifier.size(36.dp),
                            tint = PureWhite
                        )
                    }
                }
            }

            // Scan Result Bottom Sheet
            scanResult?.let { result ->
                ScanResultBottomSheet(
                    result = result,
                    onConfirm = {
                        onFoodScanned(result)
                        onClose()
                    },
                    onDismiss = { scanResult = null }
                )
            }
        } else {
            // Permission Request
            CameraPermissionRequest(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }
    }
}

@Composable
fun CameraPreviewPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IronBlack)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = PureWhite.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Camera Preview",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite.copy(alpha = 0.5f)
            )
            Text(
                "(CameraX integration would go here)",
                style = MaterialTheme.typography.bodyMedium,
                color = PureWhite.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ScanningCorners() {
    // Corner indicators for scanning frame
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left corner
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(40.dp, 4.dp),
            color = ForgeRed
        ) {}
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(4.dp, 40.dp),
            color = ForgeRed
        ) {}

        // Top-right corner
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp, 4.dp),
            color = ForgeRed
        ) {}
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(4.dp, 40.dp),
            color = ForgeRed
        ) {}

        // Bottom-left corner
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(40.dp, 4.dp),
            color = ForgeRed
        ) {}
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(4.dp, 40.dp),
            color = ForgeRed
        ) {}

        // Bottom-right corner
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp, 4.dp),
            color = ForgeRed
        ) {}
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(4.dp, 40.dp),
            color = ForgeRed
        ) {}
    }
}

@Composable
fun CameraPermissionRequest(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = ForgeRed
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "FitForge needs camera access to scan your food and provide nutrition information",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ForgeRed)
            ) {
                Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultBottomSheet(
    result: FoodScanResult,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Detected Foods",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.Check, "Success", tint = Success)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Food items
            result.foods.forEach { food ->
                FoodItemCard(food)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Totals
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ForgeRed.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "MEAL TOTALS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ForgeRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${result.totalCalories} calories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${result.totalProtein}g P • ${result.totalCarbs}g C • ${result.totalFat}g F",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Success)
            ) {
                Text("LOG MEAL", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retake Photo")
            }
        }
    }
}

@Composable
fun FoodItemCard(food: DetectedFood) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${food.servingSize} oz • ${food.calories} cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${food.protein.toInt()}g P",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${food.carbs.toInt()}g C • ${food.fat.toInt()}g F",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

data class FoodScanResult(
    val foods: List<DetectedFood>
) {
    val totalCalories: Int get() = foods.sumOf { it.calories }
    val totalProtein: Int get() = foods.sumOf { it.protein.toInt() }
    val totalCarbs: Int get() = foods.sumOf { it.carbs.toInt() }
    val totalFat: Int get() = foods.sumOf { it.fat.toInt() }
}

data class DetectedFood(
    val name: String,
    val servingSize: Float,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)
