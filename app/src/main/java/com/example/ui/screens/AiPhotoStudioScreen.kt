package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.ui.viewmodel.GalleryViewModel

enum class StudioMode(val label: String) {
    DEBLUR("AI Deblur"),
    BLUR_STUDIO("Blur Studio"),
    BACKGROUND("Background"),
    RETOUCH("Retouch")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPhotoStudioScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allMedia by viewModel.allMedia.collectAsState()
    val activePhoto by viewModel.activeStudioPhoto.collectAsState()
    val activeBitmap by viewModel.activeStudioBitmap.collectAsState()
    val enhancedBitmap by viewModel.enhancedStudioBitmap.collectAsState()
    val isProcessing by viewModel.isProcessingAi.collectAsState()

    val photos = remember(allMedia) { allMedia.filter { !it.isVideo } }

    // Auto-select first photo if none active
    LaunchedEffect(photos) {
        if (activePhoto == null && photos.isNotEmpty()) {
            viewModel.openPhotoStudio(photos.first())
        }
    }

    var selectedMode by remember { mutableStateOf(StudioMode.DEBLUR) }

    // Deblur settings
    var deblurStrength by remember { mutableFloatStateOf(1.2f) }
    var sharpenStrength by remember { mutableFloatStateOf(1.0f) }
    var denoiseStrength by remember { mutableFloatStateOf(0.4f) }

    // Blur studio settings
    var selectedBlurType by remember { mutableStateOf("GAUSSIAN") }
    var blurIntensity by remember { mutableFloatStateOf(0.5f) }
    val blurTypes = listOf(
        "GAUSSIAN" to "Gaussian",
        "WHITE_HAZE" to "White Haze",
        "MOTION" to "Motion",
        "RADIAL" to "Radial",
        "BLACK_HAZE" to "Cinematic Haze",
        "PIXELATE" to "Mosaic / Privacy"
    )

    // Background studio settings
    var selectedBgMode by remember { mutableStateOf("WHITE") }
    val bgModes = listOf(
        "WHITE" to "Pure White",
        "BLACK" to "Solid Black",
        "BLUR" to "Portrait Blur",
        "TRANSPARENT" to "Transparent PNG"
    )

    // Comparison split slider (0.0f = all original, 1.0f = all enhanced, 0.5f = split)
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    var showPhotoSelector by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Photo & Blur Studio", fontWeight = FontWeight.Bold)
                        Text(
                            text = activePhoto?.name ?: "Select a photo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closePhotoStudio() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { showPhotoSelector = true }) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Photo")
                        }
                        DropdownMenu(
                            expanded = showPhotoSelector,
                            onDismissRequest = { showPhotoSelector = false }
                        ) {
                            photos.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name, maxLines = 1) },
                                    onClick = {
                                        viewModel.openPhotoStudio(p)
                                        showPhotoSelector = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showSaveDialog = true },
                        enabled = enhancedBitmap != null,
                        modifier = Modifier.testTag("studio_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Enhanced",
                            tint = if (enhancedBitmap != null) MaterialTheme.colorScheme.primary else Color.Gray
                        )
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
            // Main Interactive Comparison Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                if (isProcessing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Running AI Enhancement...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (activePhoto != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Base / Original Photo Layer
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(activePhoto!!.uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Original",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Enhanced Layer (Clipped to split fraction if available)
                        if (enhancedBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(splitFraction)
                                    .clip(RoundedCornerShape(0.dp))
                            ) {
                                Image(
                                    bitmap = enhancedBitmap!!.asImageBitmap(),
                                    contentDescription = "Enhanced",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Interactive Vertical Split Slider Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(3.dp)
                                    .align(Alignment.CenterStart)
                                    .padding(start = (boxWidth.value * splitFraction).dp - 1.5.dp)
                                    .background(Color.White)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, _ ->
                                            val newFrac = (change.position.x / boxWidth.value).coerceIn(0.05f, 0.95f)
                                            splitFraction = newFrac
                                        }
                                    }
                            )

                            // Floating comparison badges
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "AI Enhanced (${(splitFraction * 100).toInt()}%)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Original (${((1f - splitFraction) * 100).toInt()}%)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Adjust controls below and tap 'Apply' to see AI result",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Studio Mode Selection Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StudioMode.values()) { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedMode = mode }
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Studio Tool Controls Drawer
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (selectedMode) {
                        StudioMode.DEBLUR -> {
                            Column {
                                Text(
                                    text = "AI Blur Removal & Detail Recovery",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Removes motion blur, camera shake, and defocus blur while recovering sharp edges.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Deblur Strength", style = MaterialTheme.typography.bodySmall)
                                    Text(String.format("%.1fx", deblurStrength), fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = deblurStrength,
                                    onValueChange = { deblurStrength = it },
                                    valueRange = 0.2f..2.5f
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Edge Sharpening", style = MaterialTheme.typography.bodySmall)
                                    Text(String.format("%.1fx", sharpenStrength), fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = sharpenStrength,
                                    onValueChange = { sharpenStrength = it },
                                    valueRange = 0.2f..2.5f
                                )

                                Button(
                                    onClick = {
                                        viewModel.processDeblur(deblurStrength, sharpenStrength, denoiseStrength)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("apply_deblur_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Remove Blur & Enhance Clarity")
                                }
                            }
                        }

                        StudioMode.BLUR_STUDIO -> {
                            Column {
                                Text(
                                    text = "Add Any Type of Blur",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(blurTypes) { (typeKey, typeLabel) ->
                                        val isSel = selectedBlurType == typeKey
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { selectedBlurType = typeKey }
                                        ) {
                                            Text(
                                                text = typeLabel,
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Blur Radius / Intensity", style = MaterialTheme.typography.bodySmall)
                                    Text("${(blurIntensity * 100).toInt()}%", fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = blurIntensity,
                                    onValueChange = { blurIntensity = it },
                                    valueRange = 0.1f..1.0f
                                )

                                Button(
                                    onClick = {
                                        viewModel.processBlurStudio(selectedBlurType, blurIntensity)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.BlurOn, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply Blur Effect")
                                }
                            }
                        }

                        StudioMode.BACKGROUND -> {
                            Column {
                                Text(
                                    text = "Background Studio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(bgModes) { (modeKey, modeLabel) ->
                                        val isSel = selectedBgMode == modeKey
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { selectedBgMode = modeKey }
                                        ) {
                                            Text(
                                                text = modeLabel,
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        viewModel.processBackgroundStudio(selectedBgMode)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Extract Subject & Apply Background")
                                }
                            }
                        }

                        StudioMode.RETOUCH -> {
                            Column {
                                Text(
                                    text = "Quick Retouch & Polish",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Auto-enhances lighting, removes compression artifacts, and restores faded colors.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        viewModel.processDeblur(1.0f, 1.2f, 0.6f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Auto-Enhance Photo")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Choice Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save AI Enhanced Photo") },
            text = { Text("Save this enhanced version as a new photo copy in your gallery or overwrite the original?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveStudioEnhancedPhoto(saveAsCopy = true)
                        showSaveDialog = false
                    }
                ) {
                    Text("Save as New Copy")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.saveStudioEnhancedPhoto(saveAsCopy = false)
                        showSaveDialog = false
                    }
                ) {
                    Text("Overwrite Original")
                }
            }
        )
    }
}
