package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.engine.MovieScene
import com.example.data.engine.SceneEnhancementSettings
import com.example.data.model.MediaItem
import com.example.ui.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieStudioScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allMedia by viewModel.allMedia.collectAsState()
    val activeVideo by viewModel.activeStudioVideo.collectAsState()
    val scenes by viewModel.movieScenes.collectAsState()
    val selectedScene by viewModel.selectedScene.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingMovie.collectAsState()

    val videos = remember(allMedia) { allMedia.filter { it.isVideo } }

    // Auto-select first video if none loaded
    LaunchedEffect(videos) {
        if (activeVideo == null && videos.isNotEmpty()) {
            viewModel.openMovieStudio(videos.first())
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentProgressMs by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    val totalDurationMs = remember(activeVideo) {
        if (activeVideo != null && activeVideo!!.durationMs > 0) activeVideo!!.durationMs else 15000L
    }

    // Playback loop
    LaunchedEffect(isPlaying, activeVideo) {
        while (isPlaying) {
            delay(100)
            currentProgressMs += (100 * playbackSpeed).toLong()
            if (currentProgressMs >= totalDurationMs) {
                currentProgressMs = 0L
                isPlaying = false
            }
        }
    }

    // Scene Enhancement dialog & settings
    var showEnhanceDialog by remember { mutableStateOf(false) }
    var deblurVal by remember { mutableFloatStateOf(1.2f) }
    var sharpenVal by remember { mutableFloatStateOf(1.0f) }
    var lowLightVal by remember { mutableFloatStateOf(0.3f) }
    var stabilizeVal by remember { mutableStateOf(true) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showVideoDropdown by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Movie Clarity & Scene Studio", fontWeight = FontWeight.Bold)
                        Text(
                            text = activeVideo?.name ?: "Select a video",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeMovieStudio() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { showVideoDropdown = true }) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Clip")
                        }
                        DropdownMenu(
                            expanded = showVideoDropdown,
                            onDismissRequest = { showVideoDropdown = false }
                        ) {
                            videos.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(v.name, maxLines = 1) },
                                    onClick = {
                                        viewModel.openMovieStudio(v)
                                        showVideoDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Extract frame
                    IconButton(
                        onClick = { viewModel.extractFrameFromCurrentVideo(currentProgressMs) },
                        modifier = Modifier.testTag("extract_frame_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Extract Frame")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Video Player Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (activeVideo != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(activeVideo!!.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Movie Footage",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Enhanced Scene Badge
                    if (selectedScene?.isEnhanced == true) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.85f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scene Restored & Clarified", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Center Play Overlay
                    if (!isPlaying) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            IconButton(onClick = { isPlaying = true }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }
            }

            // Timeline Scrubber & Speed Controls
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMs(currentProgressMs),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                            }
                            TextButton(
                                onClick = {
                                    playbackSpeed = when (playbackSpeed) {
                                        1.0f -> 1.5f
                                        1.5f -> 2.0f
                                        2.0f -> 0.5f
                                        else -> 1.0f
                                    }
                                }
                            ) {
                                Text("${playbackSpeed}x")
                            }
                        }
                        Text(
                            text = formatMs(totalDurationMs),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Slider(
                        value = currentProgressMs.toFloat(),
                        onValueChange = { currentProgressMs = it.toLong() },
                        valueRange = 0f..totalDurationMs.toFloat()
                    )
                }
            }

            // Scene Detection & Timeline Segment Selector
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movie Scene Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { viewModel.detectMovieScenes() },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Detect", fontSize = 11.sp)
                    }
                }

                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    // Scenes Horizontal Timeline Cards
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(scenes) { scene ->
                            val isSelected = selectedScene?.id == scene.id
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .width(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.selectMovieScene(scene)
                                        currentProgressMs = scene.startMs
                                    }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                        else Modifier
                                    )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Scene ${scene.id}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        if (scene.isEnhanced) {
                                            Text("★ Restored", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        } else if (scene.isBlurry) {
                                            Text("⚠ Blurry", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("✓ Sharp", color = Color(0xFF3B82F6), fontSize = 10.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = scene.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = scene.formattedTimestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Blur Score: ${(scene.blurScore).toInt()}%",
                                        fontSize = 10.sp,
                                        color = if (scene.isBlurry) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Scene Management Panel
                selectedScene?.let { scene ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Selected: ${scene.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Duration: ${scene.formattedDuration} · Blur Score: ${(scene.blurScore).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons: Enhance, Replace, Remove
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showEnhanceDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Enhance Scene", fontSize = 12.sp)
                                }

                                if (scene.isEnhanced) {
                                    OutlinedButton(
                                        onClick = { viewModel.replaceSceneInMovie(scene) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Replace in Movie", fontSize = 12.sp)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { showDeleteConfirmDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(0.7f)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Enhance Scene Dialog
    if (showEnhanceDialog && selectedScene != null) {
        val scene = selectedScene!!
        AlertDialog(
            onDismissRequest = { showEnhanceDialog = false },
            title = { Text("AI Enhance Scene ${scene.id}") },
            text = {
                Column {
                    Text(
                        "Removes motion blur, stabilizes camera shake, and restores low-light details for this specific scene while leaving other scenes intact.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Deblur Strength", fontSize = 12.sp)
                        Text(String.format("%.1fx", deblurVal), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(value = deblurVal, onValueChange = { deblurVal = it }, valueRange = 0.5f..2.5f)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Edge Sharpening", fontSize = 12.sp)
                        Text(String.format("%.1fx", sharpenVal), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(value = sharpenVal, onValueChange = { sharpenVal = it }, valueRange = 0.5f..2.0f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Motion Stabilization", fontSize = 12.sp)
                        Switch(checked = stabilizeVal, onCheckedChange = { stabilizeVal = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.enhanceSelectedMovieScene(
                            SceneEnhancementSettings(
                                deblurStrength = deblurVal,
                                detailSharpening = sharpenVal,
                                lowLightBoost = lowLightVal,
                                motionStabilization = stabilizeVal
                            )
                        )
                        showEnhanceDialog = false
                    }
                ) {
                    Text("Apply to Scene")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnhanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Remove Scene confirmation dialog
    if (showDeleteConfirmDialog && selectedScene != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Remove Scene ${selectedScene?.id}?") },
            text = { Text("This will cut Scene ${selectedScene?.id} (${selectedScene?.name}) from the timeline and join surrounding clips together.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedScene?.let { viewModel.removeSceneFromMovie(it) }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove Scene")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%02d:%02d", mins, secs)
}
