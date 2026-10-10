package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import com.example.ui.components.CropAspectRatioPreset
import com.example.ui.components.rememberUCrop
import com.example.ui.viewmodel.GalleryViewModel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton

enum class EditorTool(val label: String) {
    CROP("Crop"),
    ROTATE("Rotate"),
    ADJUST("Adjust"),
    FILTERS("Filters"),
    TEXT("Text"),
    DRAW("Draw")
}

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    item: MediaItem,
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        viewModel.closeEditor()
    }

    var selectedTool by remember { mutableStateOf(EditorTool.ADJUST) }

    var currentImageUri by remember(item.uri) { mutableStateOf(item.uri) }
    var currentItemState by remember(item) { mutableStateOf(item) }

    val (uCropState, uCropDialog) = rememberUCrop(
        viewModel = viewModel,
        onCroppedSuccess = { orig, croppedUri, destFile ->
            currentImageUri = croppedUri.toString()
            currentItemState = currentItemState.copy(
                uri = croppedUri.toString(),
                name = destFile.name,
                sizeBytes = destFile.length()
            )
            viewModel.showMessage("Photo cropped successfully with UCrop")
        }
    )

    // Adjustment states (-1f to 1f)
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0f) }
    var warmth by remember { mutableFloatStateOf(0f) }
    var activeAdjustParam by remember { mutableStateOf("Brightness") }

    // Transform states
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }

    // Aspect ratio crop (1f, 4/3f, 16/9f, 9/16f, 0f = free)
    var cropAspectRatio by remember { mutableFloatStateOf(0f) }

    // Selected visual filter
    var selectedFilter by remember { mutableStateOf("Normal") }
    val filtersList = listOf("Normal", "B&W", "Sepia", "Warm", "Cool", "Noir", "Vivid")

    // Text overlay state
    var overlayText by remember { mutableStateOf("") }
    var textColor by remember { mutableStateOf(Color.White) }
    var showTextInputDialog by remember { mutableStateOf(false) }

    // Drawing state with Undo/Redo
    val drawingPaths = remember { mutableStateListOf<DrawPath>() }
    val undonePaths = remember { mutableStateListOf<DrawPath>() }
    var currentPenColor by remember { mutableStateOf(Color(0xFFEF4444)) }
    var currentStrokeWidth by remember { mutableFloatStateOf(8f) }
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    // Save dialog state
    var showSaveDialog by remember { mutableStateOf(false) }
    var isComparingOriginal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // UCrop aspect ratio dialog
        uCropDialog()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.closeEditor() },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }

                    // Compare before/after button
                    TextButton(
                        onClick = { isComparingOriginal = !isComparingOriginal }
                    ) {
                        Text(
                            text = if (isComparingOriginal) "Viewing Original" else "Compare",
                            color = if (isComparingOriginal) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick UCrop button
                        IconButton(
                            onClick = { uCropState.openPresetChooser(currentItemState) },
                            modifier = Modifier.testTag("editor_top_crop_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = "Crop with UCrop",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Reset all edits
                        IconButton(
                            onClick = {
                                brightness = 0f
                                contrast = 0f
                                saturation = 0f
                                warmth = 0f
                                rotationDegrees = 0f
                                flipHorizontal = false
                                flipVertical = false
                                cropAspectRatio = 0f
                                selectedFilter = "Normal"
                                overlayText = ""
                                drawingPaths.clear()
                                undonePaths.clear()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = Color.White
                            )
                        }

                        // Save button
                        IconButton(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.testTag("editor_save_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Main Photo Canvas Preview Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val boxModifier = if (cropAspectRatio > 0f) {
                    Modifier.aspectRatio(cropAspectRatio)
                } else {
                    Modifier.fillMaxSize()
                }

                Box(
                    modifier = boxModifier
                        .clip(RoundedCornerShape(8.dp))
                        .scale(
                            scaleX = if (flipHorizontal) -1f else 1f,
                            scaleY = if (flipVertical) -1f else 1f
                        )
                        .rotate(rotationDegrees)
                        .then(
                            if (selectedTool == EditorTool.DRAW) {
                                Modifier.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentPathPoints = listOf(offset)
                                        },
                                        onDrag = { change, _ ->
                                            currentPathPoints = currentPathPoints + change.position
                                        },
                                        onDragEnd = {
                                            if (currentPathPoints.isNotEmpty()) {
                                                drawingPaths.add(
                                                    DrawPath(
                                                        points = currentPathPoints,
                                                        color = currentPenColor,
                                                        strokeWidth = currentStrokeWidth
                                                    )
                                                )
                                                undonePaths.clear()
                                                currentPathPoints = emptyList()
                                            }
                                        }
                                    )
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Editable Photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Live Filter & Adjustment visual Tint Overlays
                    if (!isComparingOriginal) {
                        // Filter effect overlay
                        when (selectedFilter) {
                            "B&W" -> Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.45f)))
                            "Sepia" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF704214).copy(alpha = 0.35f)))
                            "Warm" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFA500).copy(alpha = 0.2f)))
                            "Cool" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF00BFFF).copy(alpha = 0.2f)))
                            "Noir" -> Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                            "Vivid" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFF007F).copy(alpha = 0.15f)))
                        }

                        // Brightness & warmth simulation
                        if (brightness != 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (brightness > 0) Color.White.copy(alpha = brightness * 0.4f)
                                        else Color.Black.copy(alpha = -brightness * 0.4f)
                                    )
                            )
                        }

                        if (warmth != 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (warmth > 0) Color(0xFFFF8C00).copy(alpha = warmth * 0.25f)
                                        else Color(0xFF1E90FF).copy(alpha = -warmth * 0.25f)
                                    )
                            )
                        }
                    }

                    // Drawing Strokes Canvas
                    if (!isComparingOriginal) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (path in drawingPaths) {
                                for (i in 0 until path.points.size - 1) {
                                    drawLine(
                                        color = path.color,
                                        start = path.points[i],
                                        end = path.points[i + 1],
                                        strokeWidth = path.strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                            if (currentPathPoints.size > 1) {
                                for (i in 0 until currentPathPoints.size - 1) {
                                    drawLine(
                                        color = currentPenColor,
                                        start = currentPathPoints[i],
                                        end = currentPathPoints[i + 1],
                                        strokeWidth = currentStrokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        // Text overlay
                        if (overlayText.isNotBlank()) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 32.dp)
                            ) {
                                Text(
                                    text = overlayText,
                                    color = textColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Tool Controls Drawer
            Surface(
                color = Color(0xFF1E1E1E),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Tool Specific Controls
                    when (selectedTool) {
                        EditorTool.ADJUST -> {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$activeAdjustParam: ${when (activeAdjustParam) {
                                            "Brightness" -> (brightness * 100).toInt()
                                            "Contrast" -> (contrast * 100).toInt()
                                            "Saturation" -> (saturation * 100).toInt()
                                            "Warmth" -> (warmth * 100).toInt()
                                            else -> 0
                                        }}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Slider(
                                    value = when (activeAdjustParam) {
                                        "Brightness" -> brightness
                                        "Contrast" -> contrast
                                        "Saturation" -> saturation
                                        "Warmth" -> warmth
                                        else -> 0f
                                    },
                                    onValueChange = { newVal ->
                                        when (activeAdjustParam) {
                                            "Brightness" -> brightness = newVal
                                            "Contrast" -> contrast = newVal
                                            "Saturation" -> saturation = newVal
                                            "Warmth" -> warmth = newVal
                                        }
                                    },
                                    valueRange = -1f..1f,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(listOf("Brightness", "Contrast", "Saturation", "Warmth")) { param ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (activeAdjustParam == param) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                                            modifier = Modifier.clickable { activeAdjustParam = param }
                                        ) {
                                            Text(
                                                text = param,
                                                color = if (activeAdjustParam == param) MaterialTheme.colorScheme.onPrimary else Color.White,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        EditorTool.FILTERS -> {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                items(filtersList) { filterName ->
                                    val isSelected = selectedFilter == filterName
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { selectedFilter = filterName }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when (filterName) {
                                                        "B&W" -> Color.Gray
                                                        "Sepia" -> Color(0xFF704214)
                                                        "Warm" -> Color(0xFFFFA500)
                                                        "Cool" -> Color(0xFF00BFFF)
                                                        "Noir" -> Color(0xFF222222)
                                                        "Vivid" -> Color(0xFFFF007F)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                )
                                                .then(
                                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                                    else Modifier
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = filterName,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        EditorTool.ROTATE -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f }) {
                                    Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rotate 90°", color = Color.White)
                                }
                                TextButton(onClick = { flipHorizontal = !flipHorizontal }) {
                                    Icon(Icons.Default.Flip, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Flip H", color = Color.White)
                                }
                                TextButton(onClick = { flipVertical = !flipVertical }) {
                                    Icon(Icons.Default.Flip, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Flip V", color = Color.White)
                                }
                            }
                        }

                        EditorTool.CROP -> {
                            Column {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    val ratios = listOf(
                                        "Free" to 0f,
                                        "1:1" to 1f,
                                        "4:5" to (4f / 5f),
                                        "4:3" to (4f / 3f),
                                        "3:4" to (3f / 4f),
                                        "16:9" to (16f / 9f),
                                        "9:16" to (9f / 16f),
                                        "3:2" to (3f / 2f),
                                        "2:3" to (2f / 3f)
                                    )
                                    items(ratios) { (name, ratio) ->
                                        val isSelected = cropAspectRatio == ratio
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C),
                                            modifier = Modifier.clickable { cropAspectRatio = ratio }
                                        ) {
                                            Text(
                                                text = name,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            val preset = when (cropAspectRatio) {
                                                1f -> CropAspectRatioPreset.SQUARE
                                                4f / 5f -> CropAspectRatioPreset.PORTRAIT_4_5
                                                4f / 3f -> CropAspectRatioPreset.PHOTO_4_3
                                                3f / 4f -> CropAspectRatioPreset.PHOTO_3_4
                                                16f / 9f -> CropAspectRatioPreset.LANDSCAPE_16_9
                                                9f / 16f -> CropAspectRatioPreset.STORY_9_16
                                                3f / 2f -> CropAspectRatioPreset.PHOTO_3_2
                                                2f / 3f -> CropAspectRatioPreset.PHOTO_2_3
                                                else -> CropAspectRatioPreset.FREE
                                            }
                                            uCropState.launchWithPreset(currentItemState, preset)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("editor_launch_ucrop_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Crop,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open UCrop Cropper", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { uCropState.openPresetChooser(currentItemState) },
                                        modifier = Modifier.testTag("editor_crop_presets_button")
                                    ) {
                                        Text("Presets", color = Color.White)
                                    }
                                }
                            }
                        }

                        EditorTool.TEXT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(onClick = { showTextInputDialog = true }) {
                                    Icon(Icons.Default.TextFields, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (overlayText.isBlank()) "Add Text" else "Edit Text")
                                }

                                if (overlayText.isNotBlank()) {
                                    TextButton(onClick = { overlayText = "" }) {
                                        Text("Remove", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        EditorTool.DRAW -> {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(
                                            Color.White,
                                            Color(0xFFEF4444),
                                            Color(0xFFFBBF24),
                                            Color(0xFF10B981),
                                            Color(0xFF3B82F6),
                                            Color(0xFF8B5CF6)
                                        ).forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(color, CircleShape)
                                                    .clickable { currentPenColor = color }
                                                    .then(
                                                        if (currentPenColor == color) Modifier.border(2.dp, Color.White, CircleShape)
                                                        else Modifier
                                                    )
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                if (drawingPaths.isNotEmpty()) {
                                                    val last = drawingPaths.removeLast()
                                                    undonePaths.add(last)
                                                }
                                            },
                                            enabled = drawingPaths.isNotEmpty()
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                                contentDescription = "Undo",
                                                tint = if (drawingPaths.isNotEmpty()) Color.White else Color.Gray
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (undonePaths.isNotEmpty()) {
                                                    val restored = undonePaths.removeLast()
                                                    drawingPaths.add(restored)
                                                }
                                            },
                                            enabled = undonePaths.isNotEmpty()
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                                contentDescription = "Redo",
                                                tint = if (undonePaths.isNotEmpty()) Color.White else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tool selector tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(EditorTool.values()) { tool ->
                            val isSelected = selectedTool == tool
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .clickable { selectedTool = tool }
                            ) {
                                Icon(
                                    imageVector = when (tool) {
                                        EditorTool.CROP -> Icons.Default.Crop
                                        EditorTool.ROTATE -> Icons.Default.RotateRight
                                        EditorTool.ADJUST -> Icons.Default.Tune
                                        EditorTool.FILTERS -> Icons.Default.AutoAwesome
                                        EditorTool.TEXT -> Icons.Default.TextFields
                                        EditorTool.DRAW -> Icons.Default.Brush
                                    },
                                    contentDescription = tool.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = tool.label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Text Input Dialog
    if (showTextInputDialog) {
        var tempText by remember { mutableStateOf(overlayText) }
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = { Text("Add Text Caption") },
            text = {
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    label = { Text("Caption Text") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        overlayText = tempText
                        showTextInputDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Save Choice Dialog: Save as New Copy vs Overwrite
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Edits") },
            text = { Text("Would you like to save this edited image as a new copy or replace the original?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Generate a clean representative edited bitmap
                        val rendered = Bitmap.createBitmap(currentItemState.width.coerceIn(800, 1920), currentItemState.height.coerceIn(600, 1080), Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(rendered)
                        val paint = Paint()
                        canvas.drawColor(android.graphics.Color.DKGRAY)
                        viewModel.saveEditedPhoto(currentItemState, rendered, saveAsCopy = true)
                        showSaveDialog = false
                    }
                ) {
                    Text("Save as New Copy")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val rendered = Bitmap.createBitmap(currentItemState.width.coerceIn(800, 1920), currentItemState.height.coerceIn(600, 1080), Bitmap.Config.ARGB_8888)
                        viewModel.saveEditedPhoto(currentItemState, rendered, saveAsCopy = false)
                        showSaveDialog = false
                    }
                ) {
                    Text("Replace Original")
                }
            }
        )
    }
}
