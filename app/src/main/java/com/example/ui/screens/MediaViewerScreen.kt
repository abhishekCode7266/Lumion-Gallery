package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.components.InfoBottomSheet
import com.example.ui.components.rememberUCrop
import com.example.ui.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val activeItem by viewModel.activeViewerItem.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val context = LocalContext.current

    if (activeItem == null) return
    val currentItem = activeItem!!

    BackHandler {
        viewModel.closeViewer()
    }

    val currentList = remember(allMedia, currentItem) {
        if (allMedia.any { it.id == currentItem.id }) allMedia else listOf(currentItem)
    }
    val currentIndex = currentList.indexOfFirst { it.id == currentItem.id }.coerceAtLeast(0)

    var showControls by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Reset transform when image changes
    LaunchedEffect(currentItem.id) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        rotationAngle = 0f
    }

    // Slideshow state
    var isSlideshowActive by remember { mutableStateOf(false) }
    var slideshowIntervalSeconds by remember { mutableStateOf(3) }

    LaunchedEffect(isSlideshowActive, currentIndex) {
        if (isSlideshowActive && currentList.size > 1) {
            delay(slideshowIntervalSeconds * 1000L)
            val nextIndex = (currentIndex + 1) % currentList.size
            viewModel.openViewer(currentList[nextIndex])
        }
    }

    // Video playback simulation for rich media player UI
    var isVideoPlaying by remember { mutableStateOf(false) }
    var videoProgressMs by remember { mutableLongStateOf(0L) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    val totalDurationMs = remember(currentItem) {
        if (currentItem.durationMs > 0) currentItem.durationMs else 15000L
    }

    LaunchedEffect(isVideoPlaying, currentItem.id) {
        if (isVideoPlaying && currentItem.isVideo) {
            while (isVideoPlaying) {
                delay(100)
                videoProgressMs = (videoProgressMs + (100 * playbackSpeed).toLong())
                if (videoProgressMs >= totalDurationMs) {
                    videoProgressMs = 0L
                    isVideoPlaying = false
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState()

    val (uCropState, uCropDialog) = rememberUCrop(
        viewModel = viewModel,
        onCroppedSuccess = { orig, croppedUri, destFile ->
            viewModel.saveCroppedPhoto(
                original = orig,
                destFile = destFile,
                saveAsCopy = true
            ) { newItem ->
                viewModel.openViewer(newItem)
            }
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    },
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.5f
                        offsetX = 0f
                        offsetY = 0f
                    }
                )
            }
    ) {
        // UCrop Preset Chooser Dialog
        uCropDialog()
        // Main Image / Video display with pinch-to-zoom & pan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentItem.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = currentItem.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .rotate(rotationAngle)
            )

            // Video Center Play Button Overlay
            if (currentItem.isVideo && !isVideoPlaying) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("video_center_play_button")
                ) {
                    IconButton(
                        onClick = { isVideoPlaying = true },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }

        // Top Navigation & Action Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.closeViewer() },
                            modifier = Modifier.testTag("viewer_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = currentItem.name,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${currentIndex + 1} of ${currentList.size}",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Row {
                        // AI Studio shortcut
                        if (!currentItem.isVideo) {
                            IconButton(
                                onClick = {
                                    viewModel.openPhotoStudio(currentItem)
                                    viewModel.closeViewer()
                                    viewModel.setTab(com.example.ui.viewmodel.GalleryTab.AI_PHOTO_STUDIO)
                                },
                                modifier = Modifier.testTag("viewer_ai_studio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Studio",
                                    tint = Color(0xFF60A5FA)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    viewModel.openMovieStudio(currentItem)
                                    viewModel.closeViewer()
                                    viewModel.setTab(com.example.ui.viewmodel.GalleryTab.MOVIE_STUDIO)
                                },
                                modifier = Modifier.testTag("viewer_movie_studio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = "Movie Studio",
                                    tint = Color(0xFF60A5FA)
                                )
                            }
                        }

                        // Slideshow toggle
                        IconButton(
                            onClick = {
                                isSlideshowActive = !isSlideshowActive
                                viewModel.showMessage(if (isSlideshowActive) "Slideshow started" else "Slideshow stopped")
                            },
                            modifier = Modifier.testTag("viewer_slideshow_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Slideshow,
                                contentDescription = "Slideshow",
                                tint = if (isSlideshowActive) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }

                        // Rotate 90 degrees
                        IconButton(
                            onClick = { rotationAngle = (rotationAngle + 90f) % 360f },
                            modifier = Modifier.testTag("viewer_rotate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Rotate",
                                tint = Color.White
                            )
                        }

                        // EXIF Info
                        IconButton(
                            onClick = { showInfoSheet = true },
                            modifier = Modifier.testTag("viewer_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Details",
                                tint = Color.White
                            )
                        }

                        // Move to Vault / Remove from Vault
                        IconButton(
                            onClick = {
                                if (currentItem.isVault) {
                                    viewModel.restoreFromVault(listOf(currentItem.id))
                                    viewModel.closeViewer()
                                } else {
                                    viewModel.moveToVault(listOf(currentItem.id))
                                    viewModel.closeViewer()
                                }
                            },
                            modifier = Modifier.testTag("viewer_vault_button")
                        ) {
                            Icon(
                                imageVector = if (currentItem.isVault) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = if (currentItem.isVault) "Remove from Vault" else "Move to Vault",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls Bar (Video controls if video, plus Favorite, Edit, Share, Delete)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dedicated Video Player Controls
                    if (currentItem.isVideo) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTime(videoProgressMs),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Speed toggle
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = "${playbackSpeed}x",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                .pointerInput(Unit) {
                                                    detectTapGestures {
                                                        playbackSpeed = when (playbackSpeed) {
                                                            1.0f -> 1.5f
                                                            1.5f -> 2.0f
                                                            2.0f -> 0.5f
                                                            else -> 1.0f
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                    // Mute toggle
                                    IconButton(
                                        onClick = { isMuted = !isMuted },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                            contentDescription = "Mute",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = formatTime(totalDurationMs),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }

                            // Interactive Seek slider
                            Slider(
                                value = videoProgressMs.toFloat(),
                                onValueChange = { videoProgressMs = it.toLong() },
                                valueRange = 0f..totalDurationMs.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Main Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous item
                        IconButton(
                            onClick = {
                                if (currentIndex > 0) {
                                    viewModel.openViewer(currentList[currentIndex - 1])
                                }
                            },
                            enabled = currentIndex > 0
                        ) {
                            Text(
                                "◀",
                                color = if (currentIndex > 0) Color.White else Color.Gray,
                                fontSize = 18.sp
                            )
                        }

                        // Favorite toggle
                        IconButton(
                            onClick = { viewModel.toggleFavorite(currentItem) },
                            modifier = Modifier.testTag("viewer_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (currentItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (currentItem.isFavorite) Color(0xFFEF4444) else Color.White
                            )
                        }

                        // Crop Photo with UCrop (only enabled for photos)
                        if (!currentItem.isVideo) {
                            IconButton(
                                onClick = {
                                    uCropState.openPresetChooser(currentItem)
                                },
                                modifier = Modifier.testTag("viewer_crop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = "Crop Photo",
                                    tint = Color.White
                                )
                            }
                        }

                        // Edit Photo (only enabled for photos)
                        if (!currentItem.isVideo) {
                            IconButton(
                                onClick = {
                                    viewModel.openEditor(currentItem)
                                },
                                modifier = Modifier.testTag("viewer_edit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Photo",
                                    tint = Color.White
                                )
                            }
                        } else {
                            // Play / Pause video toggle
                            IconButton(
                                onClick = { isVideoPlaying = !isVideoPlaying },
                                modifier = Modifier.testTag("viewer_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White
                                )
                            }
                        }

                        // Share
                        IconButton(
                            onClick = { viewModel.shareMediaItems(listOf(currentItem)) },
                            modifier = Modifier.testTag("viewer_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }

                        // Delete to trash
                        IconButton(
                            onClick = { viewModel.moveToTrash(listOf(currentItem)) },
                            modifier = Modifier.testTag("viewer_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete to Trash",
                                tint = Color.White
                            )
                        }

                        // Next item
                        IconButton(
                            onClick = {
                                if (currentIndex < currentList.size - 1) {
                                    viewModel.openViewer(currentList[currentIndex + 1])
                                }
                            },
                            enabled = currentIndex < currentList.size - 1
                        ) {
                            Text(
                                "▶",
                                color = if (currentIndex < currentList.size - 1) Color.White else Color.Gray,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // EXIF Metadata Sheet
    if (showInfoSheet) {
        InfoBottomSheet(
            item = currentItem,
            sheetState = sheetState,
            onDismissRequest = { showInfoSheet = false }
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%d:%02d", mins, secs)
}
