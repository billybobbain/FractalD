package com.billybobbain.fractald.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billybobbain.fractald.data.Bookmark
import com.billybobbain.fractald.data.BookmarkDatabase
import com.billybobbain.fractald.data.BookmarkRepository
import com.billybobbain.fractald.data.PreferencesRepository
import com.billybobbain.fractald.data.UserPreferences
import com.billybobbain.fractald.data.ViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FractalViewModel(context: Context) : ViewModel() {
    private val preferencesRepository = PreferencesRepository(context)
    private val bookmarkRepository = BookmarkRepository(
        BookmarkDatabase.getDatabase(context).bookmarkDao()
    )

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    var recalculationCounter by mutableStateOf(0)
        private set

    var isCalculating by mutableStateOf(false)

    private var debounceJob: Job? = null

    // Navigation history
    private val history = mutableListOf<ViewState>()
    private var currentHistoryIndex = -1

    var canGoBack by mutableStateOf(false)
        private set
    var canGoForward by mutableStateOf(false)
        private set

    private var isNavigatingHistory = false

    fun requestRecalculation() {
        // Cancel pending recalculation
        debounceJob?.cancel()

        // Schedule new recalculation after delay
        debounceJob = viewModelScope.launch {
            delay(150) // Wait 150ms after last gesture
            if (!isCalculating) {
                recalculationCounter++
            }
        }
    }

    fun updateCalculatingState(calculating: Boolean) {
        isCalculating = calculating
    }

    fun updateColorPalette(palette: String) {
        viewModelScope.launch {
            preferencesRepository.updateColorPalette(palette)
        }
    }

    fun updateIsAnimated(isAnimated: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateIsAnimated(isAnimated)
        }
    }

    fun updateAnimationSpeed(speed: Float) {
        viewModelScope.launch {
            preferencesRepository.updateAnimationSpeed(speed)
        }
    }

    fun updateMaxIterations(iterations: Int) {
        viewModelScope.launch {
            preferencesRepository.updateMaxIterations(iterations)
            requestRecalculation()
        }
    }

    fun saveLastViewState(centerX: Double, centerY: Double, zoom: Double) {
        viewModelScope.launch {
            preferencesRepository.updateLastViewState(centerX, centerY, zoom)
        }
    }

    fun updateRestoreLastView(restore: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateRestoreLastView(restore)
        }
    }

    // Bookmark management
    val allBookmarks: StateFlow<List<Bookmark>> = bookmarkRepository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveBookmark(
        name: String,
        centerX: Double,
        centerY: Double,
        zoom: Double,
        maxIterations: Int,
        colorPalette: String,
        thumbnail: ByteArray?
    ) {
        viewModelScope.launch {
            val bookmark = Bookmark(
                name = name,
                centerX = centerX,
                centerY = centerY,
                zoom = zoom,
                maxIterations = maxIterations,
                colorPalette = colorPalette,
                thumbnail = thumbnail
            )
            bookmarkRepository.insert(bookmark)
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkRepository.delete(bookmark)
        }
    }

    fun deleteBookmarkById(id: Long) {
        viewModelScope.launch {
            bookmarkRepository.deleteById(id)
        }
    }

    // Navigation history management
    fun addToHistory(state: ViewState) {
        // Don't add if we're navigating history
        if (isNavigatingHistory) {
            isNavigatingHistory = false
            return
        }

        // Don't add if this state matches the current history position
        if (currentHistoryIndex >= 0 && currentHistoryIndex < history.size) {
            val currentState = history[currentHistoryIndex]
            if (statesMatch(currentState, state)) {
                return
            }
        }

        // Remove forward history when adding new state
        if (currentHistoryIndex < history.size - 1) {
            history.subList(currentHistoryIndex + 1, history.size).clear()
        }

        // Add new state
        history.add(state)
        currentHistoryIndex = history.size - 1

        // Limit history size to 100 entries
        if (history.size > 100) {
            history.removeAt(0)
            currentHistoryIndex--
        }

        updateHistoryButtons()
    }

    private fun statesMatch(state1: ViewState, state2: ViewState): Boolean {
        return state1.centerX == state2.centerX &&
               state1.centerY == state2.centerY &&
               state1.zoom == state2.zoom &&
               state1.maxIterations == state2.maxIterations &&
               state1.colorPalette == state2.colorPalette
    }

    fun goBack(): ViewState? {
        if (canGoBack) {
            currentHistoryIndex--
            updateHistoryButtons()
            isNavigatingHistory = true
            return history[currentHistoryIndex]
        }
        return null
    }

    fun goForward(): ViewState? {
        if (canGoForward) {
            currentHistoryIndex++
            updateHistoryButtons()
            isNavigatingHistory = true
            return history[currentHistoryIndex]
        }
        return null
    }

    fun finishHistoryNavigation() {
        isNavigatingHistory = false
    }

    private fun updateHistoryButtons() {
        canGoBack = currentHistoryIndex > 0
        canGoForward = currentHistoryIndex < history.size - 1
    }
}
