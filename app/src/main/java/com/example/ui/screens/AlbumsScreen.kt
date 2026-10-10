package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Album
import com.example.ui.components.MediaThumbnail
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val albums by viewModel.albums.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val trashMedia by viewModel.trashMedia.collectAsState()
    val vaultMedia by viewModel.vaultMedia.collectAsState()
    val activeAlbum by viewModel.activeAlbum.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }
    var albumToRename by remember { mutableStateOf<Album?>(null) }
    var renameText by remember { mutableStateOf("") }
    var albumToDelete by remember { mutableStateOf<Album?>(null) }

    // If viewing a specific album's contents
    if (activeAlbum != null) {
        val album = activeAlbum!!
        val albumMedia = remember(allMedia, album) {
            allMedia.filter { it.albumId == album.id }
        }

        BackHandler {
            viewModel.closeAlbum()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(album.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${albumMedia.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.closeAlbum() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (albumMedia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "This album is empty.\nSelect photos in the main gallery to add them here.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(albumMedia, key = { it.id }) { item ->
                        MediaThumbnail(
                            item = item,
                            isSelectionMode = false,
                            isSelected = false,
                            onClick = { viewModel.openViewer(item) },
                            onLongClick = {},
                            onToggleFavorite = { viewModel.toggleFavorite(item) }
                        )
                    }
                }
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Albums & Folders", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            newAlbumName = ""
                            showCreateDialog = true
                        },
                        modifier = Modifier.testTag("create_album_top_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Album")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newAlbumName = ""
                    showCreateDialog = true
                },
                modifier = Modifier.testTag("fab_create_album")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Album")
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Smart System Albums
            item {
                SmartAlbumCard(
                    title = "Favorites",
                    count = favorites.size,
                    icon = Icons.Default.Favorite,
                    coverUri = favorites.firstOrNull()?.uri,
                    onClick = { viewModel.setTab(GalleryTab.FAVORITES) }
                )
            }

            item {
                val videos = allMedia.filter { it.isVideo }
                SmartAlbumCard(
                    title = "Videos",
                    count = videos.size,
                    icon = Icons.Default.Movie,
                    coverUri = videos.firstOrNull()?.uri,
                    onClick = {
                        viewModel.setFilter(com.example.ui.viewmodel.MediaFilter.VIDEOS)
                        viewModel.setTab(GalleryTab.PHOTOS)
                    }
                )
            }

            item {
                val cameraItems = allMedia.filter { it.category.equals("Camera", ignoreCase = true) }
                SmartAlbumCard(
                    title = "Camera",
                    count = cameraItems.size,
                    icon = Icons.Default.CameraAlt,
                    coverUri = cameraItems.firstOrNull()?.uri,
                    onClick = {
                        viewModel.setFilter(com.example.ui.viewmodel.MediaFilter.CAMERA)
                        viewModel.setTab(GalleryTab.PHOTOS)
                    }
                )
            }

            item {
                val screenshots = allMedia.filter { it.category.equals("Screenshots", ignoreCase = true) }
                SmartAlbumCard(
                    title = "Screenshots",
                    count = screenshots.size,
                    icon = Icons.Default.Screenshot,
                    coverUri = screenshots.firstOrNull()?.uri,
                    onClick = {
                        viewModel.setFilter(com.example.ui.viewmodel.MediaFilter.SCREENSHOTS)
                        viewModel.setTab(GalleryTab.PHOTOS)
                    }
                )
            }

            item {
                val editedItems = allMedia.filter { it.category.equals("Edited Media", ignoreCase = true) || it.category.equals("Edits", ignoreCase = true) }
                SmartAlbumCard(
                    title = "Edited Media",
                    count = editedItems.size,
                    icon = Icons.Default.AutoAwesome,
                    coverUri = editedItems.firstOrNull()?.uri,
                    onClick = {
                        viewModel.setFilter(com.example.ui.viewmodel.MediaFilter.EDITED)
                        viewModel.setTab(GalleryTab.PHOTOS)
                    }
                )
            }

            item {
                SmartAlbumCard(
                    title = "Private Vault",
                    count = vaultMedia.size,
                    icon = Icons.Default.Lock,
                    coverUri = null,
                    onClick = { viewModel.setTab(GalleryTab.VAULT) }
                )
            }

            item {
                SmartAlbumCard(
                    title = "Trash",
                    count = trashMedia.size,
                    icon = Icons.Default.Delete,
                    coverUri = trashMedia.firstOrNull()?.uri,
                    onClick = { viewModel.setTab(GalleryTab.TRASH) }
                )
            }

            // Custom User Albums
            items(albums, key = { it.id }) { album ->
                val mediaInAlbum = allMedia.filter { it.albumId == album.id }
                var showAlbumMenu by remember { mutableStateOf(false) }

                AlbumCard(
                    album = album,
                    count = mediaInAlbum.size,
                    coverUri = album.coverUri ?: mediaInAlbum.firstOrNull()?.uri,
                    onClick = { viewModel.openAlbum(album) },
                    onMoreOptions = { showAlbumMenu = true }
                )

                DropdownMenu(
                    expanded = showAlbumMenu,
                    onDismissRequest = { showAlbumMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showAlbumMenu = false
                            renameText = album.name
                            albumToRename = album
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Album") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showAlbumMenu = false
                            albumToDelete = album
                        }
                    )
                }
            }
        }
    }

    // Create Album Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Album") },
            text = {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    label = { Text("Album Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAlbumName.isNotBlank()) {
                            viewModel.createAlbum(newAlbumName.trim())
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Album Dialog
    if (albumToRename != null) {
        AlertDialog(
            onDismissRequest = { albumToRename = null },
            title = { Text("Rename Album") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        albumToRename?.let { album ->
                            if (renameText.isNotBlank()) {
                                viewModel.renameAlbum(album, renameText.trim())
                            }
                        }
                        albumToRename = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { albumToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Album Dialog
    if (albumToDelete != null) {
        AlertDialog(
            onDismissRequest = { albumToDelete = null },
            title = { Text("Delete Album?") },
            text = {
                Text("Deleting \"${albumToDelete?.name}\" will only remove the album organization. Your original photos and videos will stay safe in your main gallery.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        albumToDelete?.let { viewModel.deleteAlbum(it) }
                        albumToDelete = null
                    }
                ) {
                    Text("Delete Album", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { albumToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SmartAlbumCard(
    title: String,
    count: Int,
    icon: ImageVector,
    coverUri: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (coverUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "$count items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    count: Int,
    coverUri: String?,
    onClick: () -> Unit,
    onMoreOptions: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (coverUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "$count items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onMoreOptions) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
            }
        }
    }
}
