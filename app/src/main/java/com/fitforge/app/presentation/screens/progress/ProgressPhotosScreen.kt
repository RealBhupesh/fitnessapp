package com.fitforge.app.presentation.screens.progress

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitforge.app.presentation.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotosScreen(
    onClose: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showCamera by remember { mutableStateOf(false) }
    var selectedComparison by remember { mutableStateOf<Pair<ProgressPhoto, ProgressPhoto>?>(null) }

    // Sample photos
    val photos = remember {
        listOf(
            ProgressPhoto(
                id = "1",
                date = LocalDate.now().minusWeeks(12),
                angle = "Front",
                weight = 185.2f,
                bodyFat = 22f
            ),
            ProgressPhoto(
                id = "2",
                date = LocalDate.now().minusWeeks(8),
                angle = "Front",
                weight = 181.4f,
                bodyFat = 19.5f
            ),
            ProgressPhoto(
                id = "3",
                date = LocalDate.now().minusWeeks(4),
                angle = "Front",
                weight = 179.8f,
                bodyFat = 17.8f
            ),
            ProgressPhoto(
                id = "4",
                date = LocalDate.now(),
                angle = "Front",
                weight = 178.4f,
                bodyFat = 16.2f
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    "Progress Photos",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = { showCamera = true }) {
                    Icon(Icons.Default.CameraAlt, "Take Photo")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ForgeRed,
                titleContentColor = PureWhite,
                navigationIconContentColor = PureWhite,
                actionIconContentColor = PureWhite
            )
        )

        // Before/After Comparison
        if (photos.size >= 2) {
            BeforeAfterComparison(
                before = photos.first(),
                after = photos.last(),
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Photo Timeline
        Text(
            "Your Journey",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(photos) { photo ->
                PhotoCard(photo = photo)
            }

            item {
                AddPhotoCard(onClick = { showCamera = true })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        StatsSection(photos)

        Spacer(modifier = Modifier.height(24.dp))

        // Tips Card
        TipsCard()
    }

    // Camera Sheet
    if (showCamera && cameraPermissionState.status.isGranted) {
        ProgressPhotoCameraSheet(
            onDismiss = { showCamera = false },
            onPhotoTaken = {
                showCamera = false
                // Save photo
            }
        )
    } else if (showCamera) {
        LaunchedEffect(Unit) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
fun BeforeAfterComparison(
    before: ProgressPhoto,
    after: ProgressPhoto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "YOUR TRANSFORMATION",
                style = MaterialTheme.typography.labelLarge,
                color = ForgeRed,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Before Photo
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = TextSecondary.copy(alpha = 0.3f)
                            )
                            Text(
                                "Before",
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        before.date.format(DateTimeFormatter.ofPattern("MMM d")),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${before.weight} lbs • ${before.bodyFat}% BF",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // After Photo
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = ForgeRed.copy(alpha = 0.5f)
                            )
                            Text(
                                "After",
                                color = ForgeRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        after.date.format(DateTimeFormatter.ofPattern("MMM d")),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${after.weight} lbs • ${after.bodyFat}% BF",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Changes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ChangeItem(
                    label = "Weight",
                    value = "%.1f lbs".format(after.weight - before.weight),
                    positive = after.weight < before.weight
                )
                ChangeItem(
                    label = "Body Fat",
                    value = "%.1f%%".format(after.bodyFat - before.bodyFat),
                    positive = after.bodyFat < before.bodyFat
                )
                ChangeItem(
                    label = "Duration",
                    value = "${java.time.Period.between(before.date, after.date).weeks} weeks",
                    positive = true
                )
            }
        }
    }
}

@Composable
fun ChangeItem(label: String, value: String, positive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (positive) Success else Danger
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun PhotoCard(photo: ProgressPhoto) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = TextSecondary.copy(alpha = 0.3f)
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    photo.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${photo.weight} lbs",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AddPhotoCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = ForgeRed.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Photo",
                    modifier = Modifier.size(48.dp),
                    tint = ForgeRed
                )
                Text(
                    "Take Photo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForgeRed
                )
            }
        }
    }
}

@Composable
fun StatsSection(photos: List<ProgressPhoto>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Progress Stats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = "📸",
                value = photos.size.toString(),
                label = "Photos Taken",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = "📅",
                value = "${java.time.Period.between(photos.first().date, photos.last().date).weeks}",
                label = "Weeks Tracked",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = "📈",
                value = "%.1f%%".format(photos.first().bodyFat - photos.last().bodyFat),
                label = "BF% Lost",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun TipsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, null, tint = Info)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "PHOTO TIPS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Info,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            listOf(
                "Take photos weekly for best tracking",
                "Use same lighting and location",
                "Take front, side, and back angles",
                "Relax and stand naturally",
                "Morning photos show true progress"
            ).forEach { tip ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ", color = Info)
                    Text(tip, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotoCameraSheet(
    onDismiss: () -> Unit,
    onPhotoTaken: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Take Progress Photo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Camera preview placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IronBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Camera Preview",
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPhotoTaken,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ForgeRed)
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CAPTURE PHOTO", fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class ProgressPhoto(
    val id: String,
    val date: LocalDate,
    val angle: String,
    val weight: Float,
    val bodyFat: Float
)

val LocalDate.weeks: Int
    get() = (this.toEpochDay() / 7).toInt()
