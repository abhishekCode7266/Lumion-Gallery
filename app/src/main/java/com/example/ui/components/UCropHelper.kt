package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Crop169
import androidx.compose.material.icons.filled.Crop32
import androidx.compose.material.icons.filled.Crop54
import androidx.compose.material.icons.filled.Crop75
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.viewmodel.GalleryViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

/**
 * Aspect ratio preset for UCrop
 */
enum class CropAspectRatioPreset(
    val label: String,
    val ratioX: Float?,
    val ratioY: Float?,
    val icon: ImageVector,
    val description: String
) {
    FREE("Free", null, null, Icons.Default.CropFree, "Custom free-form cropping"),
    ORIGINAL("Original", null, null, Icons.Default.CropOriginal, "Keep source photo ratio"),
    SQUARE("1:1", 1f, 1f, Icons.Default.CropSquare, "Square (Profile / Instagram)"),
    PORTRAIT_4_5("4:5", 4f, 5f, Icons.Default.CropPortrait, "Instagram Feed Portrait"),
    PHOTO_4_3("4:3", 4f, 3f, Icons.Default.Crop54, "Standard Digital Camera"),
    PHOTO_3_4("3:4", 3f, 4f, Icons.Default.Crop75, "Standard Vertical Photo"),
    LANDSCAPE_16_9("16:9", 16f, 9f, Icons.Default.Crop169, "Widescreen Wallpaper"),
    STORY_9_16("9:16", 9f, 16f, Icons.Default.CropPortrait, "Story / Reels / Status"),
    PHOTO_3_2("3:2", 3f, 2f, Icons.Default.Crop32, "35mm Classic Film"),
    PHOTO_2_3("2:3", 2f, 3f, Icons.Default.Crop32, "35mm Vertical Film")
}

data class UCropConfig(
    val preset: CropAspectRatioPreset = CropAspectRatioPreset.FREE,
    val freeStyleCrop: Boolean = true,
    val compressionQuality: Int = 92,
    val title: String = "Crop & Edit"
)

/**
 * Builds configured UCrop Intent with Lumina Gallery Material 3 theme styling
 */
fun buildUCropIntent(
    context: Context,
    sourceUri: Uri,
    destinationUri: Uri,
    config: UCropConfig = UCropConfig()
): Intent {
    val options = UCrop.Options().apply {
        setCompressionFormat(Bitmap.CompressFormat.JPEG)
        setCompressionQuality(config.compressionQuality)
        setFreeStyleCropEnabled(config.freeStyleCrop)
        setShowCropGrid(true)
        setShowCropFrame(true)
        setHideBottomControls(false)
        setToolbarTitle(config.title)

        // Lumina Gallery Modern Dark Theme Palette
        setToolbarColor(android.graphics.Color.parseColor("#18181B"))
        setStatusBarColor(android.graphics.Color.parseColor("#09090B"))
        setActiveControlsWidgetColor(android.graphics.Color.parseColor("#8B5CF6"))
        setToolbarWidgetColor(android.graphics.Color.WHITE)
        setRootViewBackgroundColor(android.graphics.Color.parseColor("#121214"))
        setDimmedLayerColor(android.graphics.Color.parseColor("#D9000000"))
        setMaxScaleMultiplier(5.0f)
    }

    val uCrop = UCrop.of(sourceUri, destinationUri)
        .withOptions(options)

    if (config.preset.ratioX != null && config.preset.ratioY != null) {
        uCrop.withAspectRatio(config.preset.ratioX, config.preset.ratioY)
    }

    return uCrop.getIntent(context)
}

/**
 * Controller holder for UCrop session
 */
class UCropState(
    val launchWithPreset: (MediaItem, CropAspectRatioPreset) -> Unit,
    val openPresetChooser: (MediaItem) -> Unit
)

/**
 * Remembers a UCrop launcher and provides lifecycle-aware trigger methods
 */
@Composable
fun rememberUCrop(
    viewModel: GalleryViewModel,
    onCroppedSuccess: ((original: MediaItem, croppedUri: Uri, destFile: File) -> Unit)? = null
): Pair<UCropState, @Composable () -> Unit> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeMediaItem by remember { mutableStateOf<MediaItem?>(null) }
    var activeDestFile by remember { mutableStateOf<File?>(null) }
    var showPresetDialog by remember { mutableStateOf(false) }

    val cropLauncher: ManagedActivityResultLauncher<Intent, ActivityResult> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val dest = activeDestFile
            val orig = activeMediaItem

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val outputUri = UCrop.getOutput(result.data!!)
                if (outputUri != null && dest != null && orig != null) {
                    if (onCroppedSuccess != null) {
                        onCroppedSuccess(orig, outputUri, dest)
                    } else {
                        // Default handler: Save directly to Room & Gallery repository
                        viewModel.saveCroppedPhoto(
                            original = orig,
                            destFile = dest,
                            saveAsCopy = true
                        )
                    }
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR && result.data != null) {
                val cropError = UCrop.getError(result.data!!)
                viewModel.showMessage("Crop error: ${cropError?.localizedMessage ?: "Failed"}")
            }
        }

    val executeLaunch: (MediaItem, CropAspectRatioPreset) -> Unit = { item, preset ->
        activeMediaItem = item
        coroutineScope.launch {
            try {
                val sourceUri = viewModel.prepareCropSourceUri(item.uri)
                val (destFile, destUri) = viewModel.createCropDestinationUri()
                activeDestFile = destFile

                val config = UCropConfig(
                    preset = preset,
                    freeStyleCrop = preset == CropAspectRatioPreset.FREE,
                    title = "Crop: ${item.name.take(16)}"
                )
                val intent = buildUCropIntent(context, sourceUri, destUri, config)
                cropLauncher.launch(intent)
            } catch (e: Exception) {
                viewModel.showMessage("Unable to open crop tool: ${e.localizedMessage}")
            }
        }
    }

    val state = remember {
        UCropState(
            launchWithPreset = executeLaunch,
            openPresetChooser = { item ->
                activeMediaItem = item
                showPresetDialog = true
            }
        )
    }

    val dialogComposable: @Composable () -> Unit = {
        if (showPresetDialog && activeMediaItem != null) {
            UCropPresetDialog(
                item = activeMediaItem!!,
                onDismiss = { showPresetDialog = false },
                onPresetSelected = { preset ->
                    showPresetDialog = false
                    executeLaunch(activeMediaItem!!, preset)
                }
            )
        }
    }

    return Pair(state, dialogComposable)
}

/**
 * Aspect Ratio preset selector dialog with visual previews
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UCropPresetDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onPresetSelected: (CropAspectRatioPreset) -> Unit
) {
    var selectedPreset by remember { mutableStateOf(CropAspectRatioPreset.FREE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select Crop Aspect Ratio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Choose an aspect ratio preset or crop freely using touch gestures in UCrop.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CropAspectRatioPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .clickable { selectedPreset = preset }
                                .testTag("crop_preset_${preset.label.lowercase().replace(':', '_')}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = selectedPreset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPresetSelected(selectedPreset) },
                modifier = Modifier.testTag("confirm_crop_preset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Cropper")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
