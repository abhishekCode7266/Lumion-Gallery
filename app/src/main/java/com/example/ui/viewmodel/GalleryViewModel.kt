package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.AiAssistantEngine
import com.example.data.engine.AiAssistantResponse
import com.example.data.engine.AiCommandAction
import com.example.data.engine.AiImageProcessingEngine
import com.example.data.engine.MovieScene
import com.example.data.engine.MovieSceneEngine
import com.example.data.engine.SceneEnhancementSettings
import com.example.data.model.Album
import com.example.data.model.MediaItem
import com.example.data.repository.GalleryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GalleryTab(val title: String) {
    PHOTOS("Photos"),
    ALBUMS("Albums"),
    AI_PHOTO_STUDIO("AI Photo & Blur"),
    MOVIE_STUDIO("Movie & Video"),
    AI_ASSISTANT("AI Assistant"),
    FAVORITES("Favorites"),
    SEARCH("Search"),
    VAULT("Vault"),
    TRASH("Trash")
}

enum class MediaFilter(val label: String) {
    ALL("All"),
    FAVORITES("Favorites"),
    PHOTOS("Photos"),
    VIDEOS("Videos"),
    EDITED("Edited Media"),
    CAMERA("Camera"),
    SCREENSHOTS("Screenshots"),
    DOWNLOADS("Downloads")
}

enum class SortOption(val label: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    NAME_ASC("Name A-Z"),
    SIZE_DESC("Largest size")
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GalleryRepository(application)

    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<MediaItem>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashMedia: StateFlow<List<MediaItem>> = repository.trashMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultMedia: StateFlow<List<MediaItem>> = repository.vaultMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = repository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(GalleryTab.PHOTOS)
    val currentTab: StateFlow<GalleryTab> = _currentTab.asStateFlow()

    // Grid columns & View style
    private val _gridColumns = MutableStateFlow(3)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _isListView = MutableStateFlow(false)
    val isListView: StateFlow<Boolean> = _isListView.asStateFlow()

    // Filter & Sort
    private val _currentFilter = MutableStateFlow(MediaFilter.ALL)
    val currentFilter: StateFlow<MediaFilter> = _currentFilter.asStateFlow()

    private val _currentSort = MutableStateFlow(SortOption.DATE_DESC)
    val currentSort: StateFlow<SortOption> = _currentSort.asStateFlow()

    // Selection Mode
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            _selectedIds.collect { set ->
                value = set.isNotEmpty()
            }
        }
    }

    // Active Media in Fullscreen Viewer
    private val _activeViewerItem = MutableStateFlow<MediaItem?>(null)
    val activeViewerItem: StateFlow<MediaItem?> = _activeViewerItem.asStateFlow()

    // Active Media in Photo Editor
    private val _activeEditorItem = MutableStateFlow<MediaItem?>(null)
    val activeEditorItem: StateFlow<MediaItem?> = _activeEditorItem.asStateFlow()

    // Active Album for Detail View
    private val _activeAlbum = MutableStateFlow<Album?>(null)
    val activeAlbum: StateFlow<Album?> = _activeAlbum.asStateFlow()

    // Search query & filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchScope = MutableStateFlow(GalleryRepository.SearchType.ALL)
    val searchScope: StateFlow<GalleryRepository.SearchType> = _searchScope.asStateFlow()

    private val _searchTypeFilter = MutableStateFlow("ALL") // "ALL", "PHOTO", "VIDEO"
    val searchTypeFilter: StateFlow<String> = _searchTypeFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<MediaItem>> = combine(_searchQuery, _searchScope) { query, scope ->
        Pair(query, scope)
    }.flatMapLatest { (query, scope) ->
        repository.searchMedia(query, scope)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Vault Authentication
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _vaultPinSet = MutableStateFlow(repository.isVaultPinSet())
    val vaultPinSet: StateFlow<Boolean> = _vaultPinSet.asStateFlow()

    private val _vaultPinError = MutableStateFlow(false)
    val vaultPinError: StateFlow<Boolean> = _vaultPinError.asStateFlow()

    // User feedback message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // AI Photo & Blur Studio
    private val movieEngine = MovieSceneEngine(application)

    private val _activeStudioPhoto = MutableStateFlow<MediaItem?>(null)
    val activeStudioPhoto: StateFlow<MediaItem?> = _activeStudioPhoto.asStateFlow()

    private val _activeStudioBitmap = MutableStateFlow<Bitmap?>(null)
    val activeStudioBitmap: StateFlow<Bitmap?> = _activeStudioBitmap.asStateFlow()

    private val _enhancedStudioBitmap = MutableStateFlow<Bitmap?>(null)
    val enhancedStudioBitmap: StateFlow<Bitmap?> = _enhancedStudioBitmap.asStateFlow()

    private val _isProcessingAi = MutableStateFlow(false)
    val isProcessingAi: StateFlow<Boolean> = _isProcessingAi.asStateFlow()

    // Movie & Video Scene Studio
    private val _activeStudioVideo = MutableStateFlow<MediaItem?>(null)
    val activeStudioVideo: StateFlow<MediaItem?> = _activeStudioVideo.asStateFlow()

    private val _movieScenes = MutableStateFlow<List<MovieScene>>(emptyList())
    val movieScenes: StateFlow<List<MovieScene>> = _movieScenes.asStateFlow()

    private val _selectedScene = MutableStateFlow<MovieScene?>(null)
    val selectedScene: StateFlow<MovieScene?> = _selectedScene.asStateFlow()

    private val _isAnalyzingMovie = MutableStateFlow(false)
    val isAnalyzingMovie: StateFlow<Boolean> = _isAnalyzingMovie.asStateFlow()

    // AI Assistant state
    private val _aiAssistantResponse = MutableStateFlow<AiAssistantResponse?>(null)
    val aiAssistantResponse: StateFlow<AiAssistantResponse?> = _aiAssistantResponse.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            repository.cleanExpiredTrash()
            _vaultPinSet.value = repository.isVaultPinSet()
        }
    }

    fun setTab(tab: GalleryTab) {
        _currentTab.value = tab
        clearSelection()
        _activeAlbum.value = null
    }

    fun setGridColumns(columns: Int) {
        _gridColumns.value = columns.coerceIn(2, 5)
    }

    fun toggleListView() {
        _isListView.value = !_isListView.value
    }

    fun setFilter(filter: MediaFilter) {
        _currentFilter.value = filter
    }

    fun setSort(sort: SortOption) {
        _currentSort.value = sort
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchScope(scope: GalleryRepository.SearchType) {
        _searchScope.value = scope
    }

    fun setSearchTypeFilter(type: String) {
        _searchTypeFilter.value = type
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchScope.value = GalleryRepository.SearchType.ALL
    }

    // Selection Handling
    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
    }

    fun selectAll(ids: List<Long>) {
        _selectedIds.value = ids.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    // Viewer Navigation
    fun openViewer(item: MediaItem) {
        _activeViewerItem.value = item
    }

    fun closeViewer() {
        _activeViewerItem.value = null
    }

    fun openEditor(item: MediaItem) {
        _activeEditorItem.value = item
    }

    fun closeEditor() {
        _activeEditorItem.value = null
    }

    fun openAlbum(album: Album) {
        _activeAlbum.value = album
    }

    fun closeAlbum() {
        _activeAlbum.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Media Actions
    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
            if (_activeViewerItem.value?.id == item.id) {
                _activeViewerItem.value = item.copy(isFavorite = !item.isFavorite)
            }
        }
    }

    fun batchToggleFavorite(isFavorite: Boolean) {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.batchSetFavorite(ids, isFavorite)
            clearSelection()
            showMessage(if (isFavorite) "Added to Favorites" else "Removed from Favorites")
        }
    }

    fun moveToTrash(items: List<MediaItem>) {
        val ids = items.map { it.id }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.moveToTrash(ids)
            clearSelection()
            if (_activeViewerItem.value?.id in ids) {
                _activeViewerItem.value = null
            }
            showMessage("Moved ${ids.size} item(s) to Trash")
        }
    }

    fun restoreFromTrash(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.restoreFromTrash(ids)
            clearSelection()
            showMessage("Restored ${ids.size} item(s) to Gallery")
        }
    }

    fun permanentlyDelete(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.permanentlyDelete(ids)
            clearSelection()
            if (_activeViewerItem.value?.id in ids) {
                _activeViewerItem.value = null
            }
            showMessage("Permanently deleted ${ids.size} item(s)")
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            showMessage("Trash emptied")
        }
    }

    fun cleanExpiredTrash() {
        viewModelScope.launch {
            val purged = repository.cleanExpiredTrash()
            if (purged > 0) {
                showMessage("Automatically purged $purged item(s) older than 30 days")
            }
        }
    }

    fun restoreAllTrash() {
        val currentTrash = trashMedia.value
        if (currentTrash.isEmpty()) return
        restoreFromTrash(currentTrash.map { it.id })
    }

    fun moveToVault(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.moveToVault(ids)
            clearSelection()
            if (_activeViewerItem.value?.id in ids) {
                _activeViewerItem.value = null
            }
            showMessage("Moved ${ids.size} item(s) to Private Vault")
        }
    }

    fun restoreFromVault(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.restoreFromVault(ids)
            clearSelection()
            if (_activeViewerItem.value?.id in ids) {
                _activeViewerItem.value = null
            }
            showMessage("Restored ${ids.size} item(s) to General Gallery")
        }
    }

    // Album Actions
    fun createAlbum(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createAlbum(name)
            showMessage("Album \"$name\" created")
        }
    }

    fun addSelectedToAlbum(albumId: Long) {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.addMediaToAlbum(ids, albumId)
            clearSelection()
            showMessage("Added ${ids.size} item(s) to Album")
        }
    }

    fun renameAlbum(album: Album, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renameAlbum(album, newName)
            if (_activeAlbum.value?.id == album.id) {
                _activeAlbum.value = album.copy(name = newName)
            }
            showMessage("Album renamed")
        }
    }

    fun deleteAlbum(album: Album) {
        viewModelScope.launch {
            repository.deleteAlbum(album)
            if (_activeAlbum.value?.id == album.id) {
                _activeAlbum.value = null
            }
            showMessage("Album deleted (Media preserved)")
        }
    }

    // Vault Security
    fun setupVaultPin(pin: String) {
        repository.setVaultPin(pin)
        _vaultPinSet.value = true
        _isVaultUnlocked.value = true
        _vaultPinError.value = false
        showMessage("Vault PIN created successfully")
    }

    fun unlockVault(pin: String): Boolean {
        val verified = repository.verifyVaultPin(pin)
        if (verified) {
            _isVaultUnlocked.value = true
            _vaultPinError.value = false
        } else {
            _vaultPinError.value = true
        }
        return verified
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        _vaultPinError.value = false
    }

    // Import Flow
    fun importUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val count = repository.importMediaUris(uris)
            showMessage("Imported ${count.size} item(s) to Gallery")
        }
    }

    fun saveCapturedPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            repository.saveCapturedPhoto(bitmap)
            showMessage("Photo captured and saved")
        }
    }

    fun createImageOutputFile(): Pair<java.io.File, Uri> = repository.createImageOutputFile()

    fun saveCapturedPhotoFile(file: java.io.File, uri: Uri) {
        viewModelScope.launch {
            repository.saveCapturedPhotoFile(file, uri)
            showMessage("Photo captured and saved to Gallery")
        }
    }

    fun createVideoOutputFile(): Pair<java.io.File, Uri> = repository.createVideoOutputFile()

    fun saveCapturedVideo(file: java.io.File, uri: Uri) {
        viewModelScope.launch {
            repository.saveCapturedVideo(file, uri)
            showMessage("Video recorded and saved to Gallery")
        }
    }

    fun saveEditedPhoto(original: MediaItem, editedBitmap: Bitmap, saveAsCopy: Boolean) {
        viewModelScope.launch {
            repository.saveEditedPhoto(original, editedBitmap, saveAsCopy)
            closeEditor()
            showMessage(if (saveAsCopy) "Saved as new copy" else "Changes saved")
        }
    }

    suspend fun prepareCropSourceUri(uriString: String): Uri = repository.prepareCropSourceUri(uriString)

    fun createCropDestinationUri(): Pair<java.io.File, Uri> = repository.createCropDestinationUri()

    fun saveCroppedPhoto(
        original: MediaItem,
        destFile: java.io.File,
        saveAsCopy: Boolean = true,
        onSuccess: ((MediaItem) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.saveCroppedPhoto(original, destFile, saveAsCopy)
            showMessage(if (saveAsCopy) "Cropped photo saved as new copy" else "Photo crop updated")
            onSuccess?.invoke(result)
        }
    }

    // Share helper
    fun shareMediaItems(items: List<MediaItem>) {
        if (items.isEmpty()) return
        try {
            val context = getApplication<Application>()
            val uris = ArrayList<Uri>()
            for (item in items) {
                try {
                    uris.add(Uri.parse(item.uri))
                } catch (_: Exception) {}
            }
            if (uris.isEmpty()) return

            val intent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = items.first().mimeType
                    putExtra(Intent.EXTRA_STREAM, uris.first())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            val chooser = Intent.createChooser(intent, "Share Media").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            showMessage("Sharing failed: ${e.message}")
        }
    }

    // AI Photo & Blur Studio Controls
    fun openPhotoStudio(item: MediaItem) {
        _activeStudioPhoto.value = item
        _enhancedStudioBitmap.value = null
        viewModelScope.launch {
            _isProcessingAi.value = true
            val bmp = repository.loadBitmapFromUri(item.uri)
            _activeStudioBitmap.value = bmp
            _isProcessingAi.value = false
        }
    }

    fun closePhotoStudio() {
        _activeStudioPhoto.value = null
        _activeStudioBitmap.value = null
        _enhancedStudioBitmap.value = null
    }

    fun processDeblur(deblurStrength: Float, sharpenStrength: Float, denoiseStrength: Float) {
        val baseBmp = _activeStudioBitmap.value ?: return
        viewModelScope.launch {
            _isProcessingAi.value = true
            val result = AiImageProcessingEngine.removeBlurAndClarify(
                source = baseBmp,
                deblurStrength = deblurStrength,
                sharpenStrength = sharpenStrength,
                denoiseStrength = denoiseStrength
            )
            _enhancedStudioBitmap.value = result
            _isProcessingAi.value = false
            showMessage("Deblur & clarification applied")
        }
    }

    fun processBlurStudio(blurType: String, intensity: Float) {
        val baseBmp = _activeStudioBitmap.value ?: return
        viewModelScope.launch {
            _isProcessingAi.value = true
            val result = AiImageProcessingEngine.applyAdvancedBlur(
                source = baseBmp,
                blurType = blurType,
                intensity = intensity
            )
            _enhancedStudioBitmap.value = result
            _isProcessingAi.value = false
            showMessage("$blurType effect applied")
        }
    }

    fun processBackgroundStudio(mode: String, color: Int = android.graphics.Color.WHITE) {
        val baseBmp = _activeStudioBitmap.value ?: return
        viewModelScope.launch {
            _isProcessingAi.value = true
            val result = AiImageProcessingEngine.processBackground(
                source = baseBmp,
                mode = mode,
                backgroundColor = color
            )
            _enhancedStudioBitmap.value = result
            _isProcessingAi.value = false
            showMessage("Background $mode processed")
        }
    }

    fun saveStudioEnhancedPhoto(saveAsCopy: Boolean) {
        val orig = _activeStudioPhoto.value ?: return
        val enhanced = _enhancedStudioBitmap.value ?: _activeStudioBitmap.value ?: return
        viewModelScope.launch {
            repository.saveEnhancedPhoto(orig, enhanced, if (saveAsCopy) "Enhanced_Copy" else "Enhanced")
            showMessage("Saved enhanced image to Gallery")
            closePhotoStudio()
        }
    }

    // Movie & Scene Studio Controls
    fun openMovieStudio(videoItem: MediaItem) {
        _activeStudioVideo.value = videoItem
        _selectedScene.value = null
        detectMovieScenes(videoItem)
    }

    fun closeMovieStudio() {
        _activeStudioVideo.value = null
        _selectedScene.value = null
        _movieScenes.value = emptyList()
    }

    fun detectMovieScenes(videoItem: MediaItem? = _activeStudioVideo.value) {
        val video = videoItem ?: return
        viewModelScope.launch {
            _isAnalyzingMovie.value = true
            val scenes = movieEngine.detectScenes(video)
            _movieScenes.value = scenes
            _selectedScene.value = scenes.firstOrNull()
            _isAnalyzingMovie.value = false
            showMessage("Detected ${scenes.size} scenes in movie")
        }
    }

    fun selectMovieScene(scene: MovieScene) {
        _selectedScene.value = scene
    }

    fun enhanceSelectedMovieScene(settings: SceneEnhancementSettings) {
        val current = _selectedScene.value ?: return
        viewModelScope.launch {
            _isAnalyzingMovie.value = true
            val enhanced = movieEngine.enhanceScene(current, settings)
            _selectedScene.value = enhanced
            // Update scenes list
            val updated = _movieScenes.value.map { if (it.id == enhanced.id) enhanced else it }
            _movieScenes.value = updated
            _isAnalyzingMovie.value = false
            showMessage("Scene ${enhanced.id} successfully enhanced! Other scenes preserved.")
        }
    }

    fun replaceSceneInMovie(scene: MovieScene) {
        val updated = _movieScenes.value.map {
            if (it.id == scene.id) scene.copy(isEnhanced = true) else it
        }
        _movieScenes.value = updated
        _selectedScene.value = scene.copy(isEnhanced = true)
        showMessage("Scene ${scene.id} replaced in movie with enhanced version. Audio & timing preserved.")
    }

    fun removeSceneFromMovie(scene: MovieScene) {
        val updated = _movieScenes.value.filter { it.id != scene.id }
        _movieScenes.value = updated
        _selectedScene.value = updated.firstOrNull()
        showMessage("Scene ${scene.id} removed from timeline. Surrounding footage joined.")
    }

    fun extractFrameFromCurrentVideo(positionMs: Long) {
        val video = _activeStudioVideo.value ?: return
        viewModelScope.launch {
            val frame = movieEngine.extractFrame(video, positionMs)
            repository.saveExtractedVideoFrame(video, frame)
            showMessage("Extracted high-resolution frame saved to Photos")
        }
    }

    // AI Assistant Natural Language Dispatcher
    fun executeAiAssistantCommand(prompt: String) {
        val activeMedia = _activeStudioPhoto.value ?: _activeStudioVideo.value ?: allMedia.value.firstOrNull()
        val response = AiAssistantEngine.parseCommand(prompt, activeMedia)
        _aiAssistantResponse.value = response

        // Auto execute non-destructive actions if suitable
        when (response.action) {
            AiCommandAction.REMOVE_BLUR_PHOTO -> {
                val photo = allMedia.value.firstOrNull { !it.isVideo }
                if (photo != null) {
                    openPhotoStudio(photo)
                    processDeblur(1.2f, 1.0f, 0.4f)
                    setTab(GalleryTab.AI_PHOTO_STUDIO)
                }
            }
            AiCommandAction.MAKE_BACKGROUND_WHITE -> {
                val photo = allMedia.value.firstOrNull { !it.isVideo }
                if (photo != null) {
                    openPhotoStudio(photo)
                    processBackgroundStudio("WHITE")
                    setTab(GalleryTab.AI_PHOTO_STUDIO)
                }
            }
            AiCommandAction.BLUR_BACKGROUND -> {
                val photo = allMedia.value.firstOrNull { !it.isVideo }
                if (photo != null) {
                    openPhotoStudio(photo)
                    processBackgroundStudio("BLUR")
                    setTab(GalleryTab.AI_PHOTO_STUDIO)
                }
            }
            AiCommandAction.APPLY_WHITE_HAZE -> {
                val photo = allMedia.value.firstOrNull { !it.isVideo }
                if (photo != null) {
                    openPhotoStudio(photo)
                    processBlurStudio("WHITE_HAZE", 0.6f)
                    setTab(GalleryTab.AI_PHOTO_STUDIO)
                }
            }
            AiCommandAction.FIND_BLURRY_SCENES, AiCommandAction.ENHANCE_MOVIE_SCENE -> {
                val video = allMedia.value.firstOrNull { it.isVideo }
                if (video != null) {
                    openMovieStudio(video)
                    setTab(GalleryTab.MOVIE_STUDIO)
                }
            }
            else -> {}
        }
    }

    fun clearAiAssistantResponse() {
        _aiAssistantResponse.value = null
    }
}
