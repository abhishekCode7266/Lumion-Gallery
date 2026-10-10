package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

enum class MediaCaptureType {
    PHOTO,
    VIDEO
}

/**
 * Accompanist-powered runtime permission and media capture helper state.
 */
class MediaCaptureState(
    val onLaunchPhoto: () -> Unit,
    val onLaunchVideo: () -> Unit
) {
    var showChooserSheet by mutableStateOf(false)
    var showRationaleDialog by mutableStateOf(false)
    var showSettingsDialog by mutableStateOf(false)
    var pendingCaptureType by mutableStateOf<MediaCaptureType?>(null)
    var rationaleTitle by mutableStateOf("")
    var rationaleMessage by mutableStateOf("")
    var rationaleIcon by mutableStateOf(Icons.Default.CameraAlt)

    fun openChooser() {
        showChooserSheet = true
    }

    fun closeChooser() {
        showChooserSheet = false
    }

    fun dismissRationale() {
        showRationaleDialog = false
        pendingCaptureType = null
    }

    fun dismissSettings() {
        showSettingsDialog = false
        pendingCaptureType = null
    }
}

@Composable
fun rememberMediaCaptureState(
    onLaunchPhoto: () -> Unit,
    onLaunchVideo: () -> Unit
): MediaCaptureState {
    return remember(onLaunchPhoto, onLaunchVideo) {
        MediaCaptureState(
            onLaunchPhoto = onLaunchPhoto,
            onLaunchVideo = onLaunchVideo
        )
    }
}

/**
 * Composable helper that integrates Google Accompanist Permissions to handle
 * Camera (android.permission.CAMERA) and Microphone (android.permission.RECORD_AUDIO)
 * runtime permission requests for direct photo and video capture.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaCaptureAccompanistHelper(
    captureState: MediaCaptureState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Accompanist single permission state for Camera (Photos)
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    ) { isGranted ->
        if (isGranted && captureState.pendingCaptureType == MediaCaptureType.PHOTO) {
            captureState.pendingCaptureType = null
            captureState.onLaunchPhoto()
        }
    }

    // Accompanist multiple permissions state for Camera + Microphone (Videos)
    val videoPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    ) { results ->
        val cameraGranted = results[Manifest.permission.CAMERA] == true
        val audioGranted = results[Manifest.permission.RECORD_AUDIO] == true
        if (cameraGranted && audioGranted && captureState.pendingCaptureType == MediaCaptureType.VIDEO) {
            captureState.pendingCaptureType = null
            captureState.onLaunchVideo()
        }
    }

    fun initiatePhotoCapture() {
        captureState.closeChooser()
        when {
            cameraPermissionState.status.isGranted -> {
                captureState.onLaunchPhoto()
            }
            cameraPermissionState.status.shouldShowRationale -> {
                captureState.pendingCaptureType = MediaCaptureType.PHOTO
                captureState.rationaleTitle = "Camera Permission Needed"
                captureState.rationaleMessage =
                    "Lumina Gallery requires camera access to take high-resolution photos directly and save them to your gallery."
                captureState.rationaleIcon = Icons.Default.CameraAlt
                captureState.showRationaleDialog = true
            }
            else -> {
                captureState.pendingCaptureType = MediaCaptureType.PHOTO
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    fun initiateVideoCapture() {
        captureState.closeChooser()
        when {
            videoPermissionsState.allPermissionsGranted -> {
                captureState.onLaunchVideo()
            }
            videoPermissionsState.shouldShowRationale -> {
                captureState.pendingCaptureType = MediaCaptureType.VIDEO
                captureState.rationaleTitle = "Camera & Microphone Required"
                captureState.rationaleMessage =
                    "Recording videos with sound directly in Lumina Gallery requires access to both your camera and microphone."
                captureState.rationaleIcon = Icons.Default.Videocam
                captureState.showRationaleDialog = true
            }
            else -> {
                captureState.pendingCaptureType = MediaCaptureType.VIDEO
                videoPermissionsState.launchMultiplePermissionRequest()
            }
        }
    }

    // Media Capture Chooser Bottom Sheet
    if (captureState.showChooserSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { captureState.closeChooser() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = modifier.testTag("media_capture_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Capture Media",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Select what you would like to capture into Lumina Gallery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Take Photo Option
                CaptureOptionCard(
                    title = "Take Photo",
                    subtitle = "Capture full-resolution photo (Requires Camera)",
                    icon = Icons.Default.PhotoCamera,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    testTag = "capture_option_photo",
                    onClick = { initiatePhotoCapture() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Record Video Option
                CaptureOptionCard(
                    title = "Record Video",
                    subtitle = "Record HD video with audio (Requires Camera & Mic)",
                    icon = Icons.Default.Videocam,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    testTag = "capture_option_video",
                    onClick = { initiateVideoCapture() }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Accompanist Rationale Dialog
    if (captureState.showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { captureState.dismissRationale() },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = captureState.rationaleIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = captureState.rationaleTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = captureState.rationaleMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your privacy is protected. Files are stored directly in your private gallery storage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val type = captureState.pendingCaptureType
                        captureState.showRationaleDialog = false
                        if (type == MediaCaptureType.PHOTO) {
                            cameraPermissionState.launchPermissionRequest()
                        } else {
                            videoPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier.testTag("dialog_grant_permission_btn")
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { captureState.dismissRationale() },
                    modifier = Modifier.testTag("dialog_dismiss_permission_btn")
                ) {
                    Text("Not Now")
                }
            }
        )
    }

    // Permanently Denied / Open Settings Dialog
    if (captureState.showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { captureState.dismissSettings() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Camera and/or Microphone permissions are required to capture media. Please enable them in Lumina Gallery App Settings.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        captureState.dismissSettings()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.testTag("dialog_open_settings_btn")
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { captureState.dismissSettings() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CaptureOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
