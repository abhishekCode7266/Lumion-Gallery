package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.GalleryRepository
import com.example.ui.components.MediaThumbnail
import com.example.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val ftsMediaResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchScope by viewModel.searchScope.collectAsState()

    var mediaTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "PHOTO", "VIDEO"
    var favoritesOnly by remember { mutableStateOf(false) }

    val quickDates = listOf("2026", "October", "2024", "Camera", "Scenic")
    val quickTags = listOf("nature", "mountain", "beach", "city", "portrait", "architecture", "sunset", "forest", "video")

    val finalResults = remember(ftsMediaResults, mediaTypeFilter, favoritesOnly) {
        ftsMediaResults.filter { item ->
            val matchesType = when (mediaTypeFilter) {
                "PHOTO" -> !item.isVideo
                "VIDEO" -> item.isVideo
                else -> true
            }
            val matchesFav = if (favoritesOnly) item.isFavorite else true
            matchesType && matchesFav
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Smart Search (Room FTS)", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    val hint = when (searchScope) {
                        GalleryRepository.SearchType.ALL -> "Search by name, date, or tags..."
                        GalleryRepository.SearchType.NAME -> "Search by filename or title..."
                        GalleryRepository.SearchType.DATE -> "Search by date (e.g., 2026, October)..."
                        GalleryRepository.SearchType.TAGS -> "Search by tags (e.g., sunset, nature)..."
                    }
                    Text(hint)
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_text_input")
            )

            // Search Target Scope Chips (All, Name, Date, Tags)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search in:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = searchScope == GalleryRepository.SearchType.ALL,
                    onClick = { viewModel.setSearchScope(GalleryRepository.SearchType.ALL) },
                    label = { Text("All Fields") },
                    modifier = Modifier.testTag("search_scope_all")
                )
                FilterChip(
                    selected = searchScope == GalleryRepository.SearchType.NAME,
                    onClick = { viewModel.setSearchScope(GalleryRepository.SearchType.NAME) },
                    label = { Text("Name") },
                    modifier = Modifier.testTag("search_scope_name")
                )
                FilterChip(
                    selected = searchScope == GalleryRepository.SearchType.DATE,
                    onClick = { viewModel.setSearchScope(GalleryRepository.SearchType.DATE) },
                    label = { Text("Date") },
                    modifier = Modifier.testTag("search_scope_date")
                )
                FilterChip(
                    selected = searchScope == GalleryRepository.SearchType.TAGS,
                    onClick = { viewModel.setSearchScope(GalleryRepository.SearchType.TAGS) },
                    label = { Text("Tags") },
                    modifier = Modifier.testTag("search_scope_tags")
                )
            }

            // Quick date & tag suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Dates",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                quickDates.forEach { dateToken ->
                    AssistChip(
                        onClick = {
                            viewModel.setSearchScope(GalleryRepository.SearchType.DATE)
                            viewModel.setSearchQuery(dateToken)
                        },
                        label = { Text(dateToken) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                quickTags.take(5).forEach { tag ->
                    AssistChip(
                        onClick = {
                            viewModel.setSearchScope(GalleryRepository.SearchType.TAGS)
                            viewModel.setSearchQuery(tag)
                        },
                        label = { Text("#$tag") }
                    )
                }
            }

            // Filter chips (Type & Favorites)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mediaTypeFilter == "ALL",
                    onClick = { mediaTypeFilter = "ALL" },
                    label = { Text("All Media") }
                )
                FilterChip(
                    selected = mediaTypeFilter == "PHOTO",
                    onClick = { mediaTypeFilter = "PHOTO" },
                    label = { Text("Photos Only") }
                )
                FilterChip(
                    selected = mediaTypeFilter == "VIDEO",
                    onClick = { mediaTypeFilter = "VIDEO" },
                    label = { Text("Videos Only") }
                )
                FilterChip(
                    selected = favoritesOnly,
                    onClick = { favoritesOnly = !favoritesOnly },
                    label = { Text("Favorites ★") }
                )
            }

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${finalResults.size} results found",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (searchQuery.isNotBlank() || searchScope != GalleryRepository.SearchType.ALL || favoritesOnly || mediaTypeFilter != "ALL") {
                    Text(
                        text = "Reset all",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            viewModel.clearSearch()
                            favoritesOnly = false
                            mediaTypeFilter = "ALL"
                        }
                    )
                }
            }

            if (finalResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "No matching items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (searchQuery.isNotBlank())
                                "No matches for \"$searchQuery\" in Room Database FTS index."
                            else
                                "Try searching for a different name, date, or tag.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(finalResults, key = { it.id }) { item ->
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
    }
}
