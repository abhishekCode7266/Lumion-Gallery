package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AiPhotoStudioScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MediaViewerScreen
import com.example.ui.screens.MovieStudioScreen
import com.example.ui.screens.PhotoEditorScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@Composable
fun GalleryApp(
    viewModel: GalleryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeViewerItem by viewModel.activeViewerItem.collectAsState()
    val activeEditorItem by viewModel.activeEditorItem.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Fullscreen Photo Editor
    if (activeEditorItem != null) {
        PhotoEditorScreen(
            item = activeEditorItem!!,
            viewModel = viewModel
        )
        return
    }

    // Fullscreen Media Viewer
    if (activeViewerItem != null) {
        MediaViewerScreen(
            viewModel = viewModel
        )
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Adaptive Tablet Layout with Side Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.PHOTOS,
                        onClick = { viewModel.setTab(GalleryTab.PHOTOS) },
                        icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Photos") },
                        label = { Text("Photos") },
                        modifier = Modifier.testTag("nav_rail_photos")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.AI_PHOTO_STUDIO,
                        onClick = { viewModel.setTab(GalleryTab.AI_PHOTO_STUDIO) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Photo") },
                        label = { Text("AI Studio") },
                        modifier = Modifier.testTag("nav_rail_ai_studio")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.MOVIE_STUDIO,
                        onClick = { viewModel.setTab(GalleryTab.MOVIE_STUDIO) },
                        icon = { Icon(Icons.Default.Movie, contentDescription = "Movie Studio") },
                        label = { Text("Movie") },
                        modifier = Modifier.testTag("nav_rail_movie_studio")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.AI_ASSISTANT,
                        onClick = { viewModel.setTab(GalleryTab.AI_ASSISTANT) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Assistant") },
                        label = { Text("Assistant") },
                        modifier = Modifier.testTag("nav_rail_assistant")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.ALBUMS,
                        onClick = { viewModel.setTab(GalleryTab.ALBUMS) },
                        icon = { Icon(Icons.Default.Collections, contentDescription = "Albums") },
                        label = { Text("Albums") },
                        modifier = Modifier.testTag("nav_rail_albums")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.FAVORITES,
                        onClick = { viewModel.setTab(GalleryTab.FAVORITES) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (favorites.isNotEmpty()) {
                                        Badge {
                                            Text(
                                                text = if (favorites.size > 99) "99+" else favorites.size.toString(),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == GalleryTab.FAVORITES) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorites",
                                    tint = if (currentTab == GalleryTab.FAVORITES) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = { Text("Favorites") },
                        modifier = Modifier.testTag("nav_rail_favorites")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.SEARCH,
                        onClick = { viewModel.setTab(GalleryTab.SEARCH) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        modifier = Modifier.testTag("nav_rail_search")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.VAULT,
                        onClick = { viewModel.setTab(GalleryTab.VAULT) },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Vault") },
                        label = { Text("Vault") },
                        modifier = Modifier.testTag("nav_rail_vault")
                    )
                    NavigationRailItem(
                        selected = currentTab == GalleryTab.TRASH,
                        onClick = { viewModel.setTab(GalleryTab.TRASH) },
                        icon = { Icon(Icons.Default.Delete, contentDescription = "Trash") },
                        label = { Text("Trash") },
                        modifier = Modifier.testTag("nav_rail_trash")
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    TabContent(currentTab, viewModel)
                }
            }
        } else {
            // Compact Phone Layout with Bottom Navigation
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == GalleryTab.PHOTOS,
                            onClick = { viewModel.setTab(GalleryTab.PHOTOS) },
                            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Photos") },
                            label = { Text("Photos") },
                            modifier = Modifier.testTag("nav_photos")
                        )
                        NavigationBarItem(
                            selected = currentTab == GalleryTab.ALBUMS,
                            onClick = { viewModel.setTab(GalleryTab.ALBUMS) },
                            icon = { Icon(Icons.Default.Collections, contentDescription = "Albums") },
                            label = { Text("Albums") },
                            modifier = Modifier.testTag("nav_albums")
                        )
                        NavigationBarItem(
                            selected = currentTab == GalleryTab.FAVORITES,
                            onClick = { viewModel.setTab(GalleryTab.FAVORITES) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (favorites.isNotEmpty()) {
                                            Badge {
                                                Text(
                                                    text = if (favorites.size > 99) "99+" else favorites.size.toString(),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentTab == GalleryTab.FAVORITES) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorites",
                                        tint = if (currentTab == GalleryTab.FAVORITES) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            label = { Text("Favorites") },
                            modifier = Modifier.testTag("nav_favorites")
                        )
                        NavigationBarItem(
                            selected = currentTab == GalleryTab.AI_PHOTO_STUDIO,
                            onClick = { viewModel.setTab(GalleryTab.AI_PHOTO_STUDIO) },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Studio") },
                            label = { Text("AI Studio") },
                            modifier = Modifier.testTag("nav_ai_studio")
                        )
                        NavigationBarItem(
                            selected = currentTab == GalleryTab.AI_ASSISTANT,
                            onClick = { viewModel.setTab(GalleryTab.AI_ASSISTANT) },
                            icon = { Icon(Icons.Default.Psychology, contentDescription = "Assistant") },
                            label = { Text("Assistant") },
                            modifier = Modifier.testTag("nav_assistant")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TabContent(currentTab, viewModel)
                }
            }
        }
    }
}

@Composable
private fun TabContent(
    currentTab: GalleryTab,
    viewModel: GalleryViewModel
) {
    AnimatedContent(
        targetState = currentTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TabContentTransition"
    ) { targetTab ->
        when (targetTab) {
            GalleryTab.PHOTOS -> HomeScreen(viewModel = viewModel)
            GalleryTab.AI_PHOTO_STUDIO -> AiPhotoStudioScreen(viewModel = viewModel)
            GalleryTab.MOVIE_STUDIO -> MovieStudioScreen(viewModel = viewModel)
            GalleryTab.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
            GalleryTab.ALBUMS -> AlbumsScreen(viewModel = viewModel)
            GalleryTab.FAVORITES -> FavoritesScreen(viewModel = viewModel)
            GalleryTab.SEARCH -> SearchScreen(viewModel = viewModel)
            GalleryTab.VAULT -> VaultScreen(viewModel = viewModel)
            GalleryTab.TRASH -> TrashScreen(viewModel = viewModel)
        }
    }
}
