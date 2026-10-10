package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.components.MediaCaptureAccompanistHelper
import com.example.ui.components.rememberMediaCaptureState
import java.io.File
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.Album
import com.example.data.model.MediaItem
import com.example.ui.components.BatchActionBar
import com.example.ui.components.MediaThumbnail
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel
import com.example.ui.viewmodel.MediaFilter
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val allMedia by viewModel.allMedia.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val isListView by viewModel.isListView.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode = selectedIds.isNotEmpty()

    var showMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showAddToAlbumDialog by remember { mutableStateOf(false) }

    // Pick media launcher (Android Photo Picker - zero permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importUris(uris)
        }
    }

    // High-resolution photo capture launcher
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val file = currentPhotoFile
        val uri = currentPhotoUri
        if (success && file != null && uri != null) {
            viewModel.saveCapturedPhotoFile(file, uri)
        }
    }

    // High-definition video capture launcher
    var currentVideoFile by remember { mutableStateOf<File?>(null) }
    var currentVideoUri by remember { mutableStateOf<Uri?>(null) }
    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success: Boolean ->
        val file = currentVideoFile
        val uri = currentVideoUri
        if (success && file != null && uri != null) {
            viewModel.saveCapturedVideo(file, uri)
        }
    }

    // Accompanist runtime permission & capture controller
    val mediaCaptureState = rememberMediaCaptureState(
        onLaunchPhoto = {
            val (file, uri) = viewModel.createImageOutputFile()
            currentPhotoFile = file
            currentPhotoUri = uri
            takePictureLauncher.launch(uri)
        },
        onLaunchVideo = {
            val (file, uri) = viewModel.createVideoOutputFile()
            currentVideoFile = file
            currentVideoUri = uri
            captureVideoLauncher.launch(uri)
        }
    )

    // Filter media
    val filteredMedia = remember(allMedia, currentFilter, currentSort) {
        val filtered = allMedia.filter { item ->
            when (currentFilter) {
                MediaFilter.ALL -> true
                MediaFilter.FAVORITES -> item.isFavorite
                MediaFilter.PHOTOS -> !item.isVideo
                MediaFilter.VIDEOS -> item.isVideo
                MediaFilter.EDITED -> item.category.equals("Edited Media", ignoreCase = true) || item.category.equals("Edits", ignoreCase = true)
                MediaFilter.CAMERA -> item.category.equals("Camera", ignoreCase = true)
                MediaFilter.SCREENSHOTS -> item.category.equals("Screenshots", ignoreCase = true)
                MediaFilter.DOWNLOADS -> item.category.equals("Downloads", ignoreCase = true)
            }
        }
        when (currentSort) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.dateTaken }
            SortOption.DATE_ASC -> filtered.sortedBy { it.dateTaken }
            SortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOption.SIZE_DESC -> filtered.sortedByDescending { it.sizeBytes }
        }
    }

    val photosCount = filteredMedia.count { !it.isVideo }
    val videosCount = filteredMedia.count { it.isVideo }
    val totalSizeBytes = filteredMedia.sumOf { it.sizeBytes }
    val formattedTotalSize = formatBytes(totalSizeBytes)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lumina Gallery",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$photosCount photos · $videosCount videos · $formattedTotalSize",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setTab(GalleryTab.FAVORITES) },
                        modifier = Modifier.testTag("home_favorites_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            tint = Color(0xFFEF4444)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setTab(GalleryTab.SEARCH) },
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    IconButton(
                        onClick = { viewModel.toggleListView() },
                        modifier = Modifier.testTag("home_view_toggle")
                    ) {
                        Icon(
                            imageVector = if (isListView) Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "Toggle View"
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("home_sort_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (option == currentSort) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSort(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("home_more_options")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Grid Columns: 2") },
                                onClick = {
                                    viewModel.setGridColumns(2)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Grid Columns: 3") },
                                onClick = {
                                    viewModel.setGridColumns(3)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Grid Columns: 4") },
                                onClick = {
                                    viewModel.setGridColumns(4)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Capture Photo / Video") },
                                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    mediaCaptureState.openChooser()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import from Device") },
                                onClick = {
                                    showMenu = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Private Vault") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.setTab(GalleryTab.VAULT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Trash") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.setTab(GalleryTab.TRASH)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { mediaCaptureState.openChooser() },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag("fab_camera")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture Photo or Video")
                    }

                    FloatingActionButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_import")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Import Media")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                val selectedItems = filteredMedia.filter { it.id in selectedIds }
                BatchActionBar(
                    selectedCount = selectedIds.size,
                    onCloseSelection = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll(filteredMedia.map { it.id }) },
                    onShare = { viewModel.shareMediaItems(selectedItems) },
                    onFavorite = { viewModel.batchToggleFavorite(true) },
                    onAddToAlbum = { showAddToAlbumDialog = true },
                    onMoveToVault = { viewModel.moveToVault(selectedIds.toList()) },
                    onDelete = { viewModel.moveToTrash(selectedItems) }
                )
            }
        }
    ) { innerPadding ->
        // Accompanist runtime permission handler and capture sheet
        MediaCaptureAccompanistHelper(captureState = mediaCaptureState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips horizontal scroll
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MediaFilter.values()) { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) },
                        leadingIcon = if (filter == MediaFilter.FAVORITES) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (currentFilter == filter) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                    )
                }
            }

            if (filteredMedia.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No media found in this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Import photos and videos from your device or take a new picture.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            modifier = Modifier.testTag("empty_state_import_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Media")
                        }
                    }
                }
            } else if (isListView) {
                // Detailed List view
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMedia, key = { it.id }) { item ->
                        MediaListItem(
                            item = item,
                            isSelected = item.id in selectedIds,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleSelection(item.id)
                                } else {
                                    viewModel.openViewer(item)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleSelection(item.id)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(item)
                            }
                        )
                    }
                }
            } else {
                // Responsive Grid view
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMedia, key = { it.id }) { item ->
                        MediaThumbnail(
                            item = item,
                            isSelectionMode = isSelectionMode,
                            isSelected = item.id in selectedIds,
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleSelection(item.id)
                                } else {
                                    viewModel.openViewer(item)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleSelection(item.id)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(item)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add to Album Dialog
    if (showAddToAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showAddToAlbumDialog = false },
            title = { Text("Add to Album") },
            text = {
                if (albums.isEmpty()) {
                    Text("No albums available. Please create an album in the Albums tab first.")
                } else {
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(albums) { album ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSelectedToAlbum(album.id)
                                        showAddToAlbumDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(album.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToAlbumDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MediaListItem(
    item: MediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavorite: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.category} · ${item.formattedSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${item.width} × ${item.height} · ${if (item.isVideo) "Video (${item.formattedDuration})" else item.mimeType.substringAfter('/')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onToggleFavorite != null && !isSelectionMode) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("list_toggle_favorite_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (item.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    val gb = mb / 1024.0
    return if (gb >= 1.0) String.format("%.2f GB", gb) else String.format("%.1f MB", mb)
}
