package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.BatchActionBar
import com.example.ui.components.MediaThumbnail
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode = selectedIds.isNotEmpty()

    var favTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "PHOTO", "VIDEO"

    val displayedFavorites = remember(favorites, favTypeFilter) {
        when (favTypeFilter) {
            "PHOTO" -> favorites.filter { !it.isVideo }
            "VIDEO" -> favorites.filter { it.isVideo }
            else -> favorites
        }
    }

    val favPhotosCount = favorites.count { !it.isVideo }
    val favVideosCount = favorites.count { it.isVideo }

    BackHandler {
        viewModel.setTab(GalleryTab.PHOTOS)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setTab(GalleryTab.PHOTOS) },
                        modifier = Modifier.testTag("favorites_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Gallery"
                        )
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                            Text("Favorites", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "${favorites.size} marked items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setTab(GalleryTab.SEARCH) },
                        modifier = Modifier.testTag("favorites_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        bottomBar = {
            if (isSelectionMode) {
                val selectedItems = favorites.filter { it.id in selectedIds }
                BatchActionBar(
                    selectedCount = selectedIds.size,
                    onCloseSelection = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll(displayedFavorites.map { it.id }) },
                    onShare = { viewModel.shareMediaItems(selectedItems) },
                    onFavorite = { viewModel.batchToggleFavorite(false) },
                    onAddToAlbum = {},
                    onMoveToVault = { viewModel.moveToVault(selectedIds.toList()) },
                    onDelete = { viewModel.moveToTrash(selectedItems) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips (All, Photos, Videos)
            if (favorites.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = favTypeFilter == "ALL",
                        onClick = { favTypeFilter = "ALL" },
                        label = { Text("All (${favorites.size})") },
                        modifier = Modifier.testTag("fav_filter_all")
                    )
                    FilterChip(
                        selected = favTypeFilter == "PHOTO",
                        onClick = { favTypeFilter = "PHOTO" },
                        label = { Text("Photos ($favPhotosCount)") },
                        modifier = Modifier.testTag("fav_filter_photos")
                    )
                    FilterChip(
                        selected = favTypeFilter == "VIDEO",
                        onClick = { favTypeFilter = "VIDEO" },
                        label = { Text("Videos ($favVideosCount)") },
                        modifier = Modifier.testTag("fav_filter_videos")
                    )
                }
            }

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFEF4444).copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Favorites Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the heart icon on any photo or video in your gallery to mark it as a favorite for instant access here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.setTab(GalleryTab.PHOTOS) },
                            modifier = Modifier.testTag("favorites_browse_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse Gallery")
                        }
                    }
                }
            } else if (displayedFavorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No $favTypeFilter items in favorites.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedFavorites, key = { it.id }) { item ->
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
}
