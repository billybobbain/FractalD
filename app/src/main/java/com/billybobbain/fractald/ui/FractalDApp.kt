package com.billybobbain.fractald.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.billybobbain.fractald.ColorPalette
import com.billybobbain.fractald.MandelbrotEngine
import com.billybobbain.fractald.data.ViewState
import com.billybobbain.fractald.viewmodel.FractalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FractalDApp(viewModel: FractalViewModel) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showSaveBookmark by remember { mutableStateOf(false) }
    var showBookmarksList by remember { mutableStateOf(false) }

    var currentEngine by remember { mutableStateOf<com.billybobbain.fractald.MandelbrotEngine?>(null) }
    var currentColoredData by remember { mutableStateOf<Array<IntArray>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    IconButton(onClick = {
                        currentEngine?.reset()
                        viewModel.requestRecalculation()
                    }) {
                        Icon(Icons.Default.Home, "Home", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.goBack()?.let { state ->
                                currentEngine?.let { engine ->
                                    engine.centerX = state.centerX
                                    engine.centerY = state.centerY
                                    engine.zoom = state.zoom
                                    engine.maxIterations = state.maxIterations
                                    viewModel.updateMaxIterations(state.maxIterations)
                                    viewModel.updateColorPalette(state.colorPalette)
                                    viewModel.requestRecalculation()
                                }
                            }
                        },
                        enabled = viewModel.canGoBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.goForward()?.let { state ->
                                currentEngine?.let { engine ->
                                    engine.centerX = state.centerX
                                    engine.centerY = state.centerY
                                    engine.zoom = state.zoom
                                    engine.maxIterations = state.maxIterations
                                    viewModel.updateMaxIterations(state.maxIterations)
                                    viewModel.updateColorPalette(state.colorPalette)
                                    viewModel.requestRecalculation()
                                }
                            }
                        },
                        enabled = viewModel.canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Forward")
                    }
                    IconButton(onClick = {
                        viewModel.updateIsAnimated(!userPreferences.isAnimated)
                    }) {
                        Icon(
                            if (userPreferences.isAnimated) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (userPreferences.isAnimated) "Pause Animation" else "Play Animation"
                        )
                    }
                    IconButton(onClick = { showSaveBookmark = true }) {
                        Icon(Icons.Default.BookmarkBorder, "Save Location")
                    }
                    IconButton(onClick = { showBookmarksList = true }) {
                        Icon(Icons.Default.List, "Bookmarks")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.MoreVert, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FractalCanvas(
                viewModel = viewModel,
                colorPalette = userPreferences.colorPalette,
                isAnimated = userPreferences.isAnimated,
                animationSpeed = userPreferences.animationSpeed,
                maxIterations = userPreferences.maxIterations,
                onEngineUpdate = { engine -> currentEngine = engine },
                onColoredDataUpdate = { data -> currentColoredData = data }
            )
        }

        if (showSettings) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { showSettings = false }
            )
        }

        if (showSaveBookmark && currentEngine != null && currentColoredData != null) {
            SaveBookmarkDialog(
                viewModel = viewModel,
                engine = currentEngine!!,
                coloredData = currentColoredData!!,
                currentPalette = userPreferences.colorPalette,
                onDismiss = { showSaveBookmark = false }
            )
        }

        if (showBookmarksList) {
            BookmarksListDialog(
                viewModel = viewModel,
                onDismiss = { showBookmarksList = false },
                onRestoreBookmark = { bookmark ->
                    currentEngine?.let { engine ->
                        engine.centerX = bookmark.centerX
                        engine.centerY = bookmark.centerY
                        engine.zoom = bookmark.zoom
                        engine.maxIterations = bookmark.maxIterations
                        viewModel.updateMaxIterations(bookmark.maxIterations)
                        viewModel.updateColorPalette(bookmark.colorPalette)
                        viewModel.requestRecalculation()
                    }
                    showBookmarksList = false
                }
            )
        }
    }
}

@Composable
fun FractalCanvas(
    viewModel: FractalViewModel,
    colorPalette: String,
    isAnimated: Boolean,
    animationSpeed: Float,
    maxIterations: Int,
    onEngineUpdate: (com.billybobbain.fractald.MandelbrotEngine) -> Unit = {},
    onColoredDataUpdate: (Array<IntArray>) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val userPreferences by viewModel.userPreferences.collectAsState()

    // Scale factor for resolution (lower = higher detail)
    val scale = 6
    val width = with(density) { configuration.screenWidthDp.dp.toPx().toInt() } / scale
    val height = with(density) { configuration.screenHeightDp.dp.toPx().toInt() } / scale

    val engine = remember(width, height) {
        MandelbrotEngine(width, height).also {
            onEngineUpdate(it)
        }
    }

    // Restore last view on startup
    var hasRestoredView by remember { mutableStateOf(false) }
    LaunchedEffect(engine, userPreferences.restoreLastView) {
        if (!hasRestoredView && userPreferences.restoreLastView) {
            engine.centerX = userPreferences.lastCenterX
            engine.centerY = userPreferences.lastCenterY
            engine.zoom = userPreferences.lastZoom
            hasRestoredView = true
            viewModel.requestRecalculation()
        }
    }

    // Update max iterations when it changes
    LaunchedEffect(maxIterations) {
        engine.maxIterations = maxIterations
        viewModel.requestRecalculation()
    }

    // Notify parent of engine updates
    LaunchedEffect(engine) {
        onEngineUpdate(engine)
    }

    var mandelbrotData by remember { mutableStateOf<Array<IntArray>?>(null) }
    var coloredData by remember { mutableStateOf<Array<IntArray>?>(null) }

    val palette = try {
        ColorPalette.PaletteType.valueOf(colorPalette)
    } catch (e: IllegalArgumentException) {
        ColorPalette.PaletteType.RAINBOW
    }

    // Calculate Mandelbrot when needed
    LaunchedEffect(viewModel.recalculationCounter, width, height) {
        if (viewModel.recalculationCounter > 0) {
            viewModel.updateCalculatingState(true)
            withContext(Dispatchers.Default) {
                mandelbrotData = engine.calculate()
            }
            viewModel.updateCalculatingState(false)

            // Add to history after calculation completes
            val currentState = ViewState(
                centerX = engine.centerX,
                centerY = engine.centerY,
                zoom = engine.zoom,
                maxIterations = engine.maxIterations,
                colorPalette = colorPalette
            )
            viewModel.addToHistory(currentState)

            // Save current view state for restoration on next launch
            viewModel.saveLastViewState(engine.centerX, engine.centerY, engine.zoom)
        }
    }

    // Apply palette with animation
    LaunchedEffect(mandelbrotData, palette, isAnimated, animationSpeed) {
        if (mandelbrotData != null) {
            var timeOffset = 0.0
            while (isActive) {
                withContext(Dispatchers.Default) {
                    val newData = ColorPalette.applyPalette(
                        mandelbrotData!!,
                        maxIterations,
                        palette,
                        timeOffset
                    )
                    coloredData = newData
                    onColoredDataUpdate(newData)
                }
                if (isAnimated) {
                    delay(100) // Slower animation to reduce memory pressure
                    timeOffset += animationSpeed * 0.15
                } else {
                    break
                }
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Pan and zoom together
                detectTransformGestures { _, pan, zoom, _ ->
                    // Apply pan
                    if (pan.x != 0f || pan.y != 0f) {
                        val panX = (-pan.x / size.width / scale).toDouble()
                        val panY = (-pan.y / size.height / scale).toDouble()
                        engine.pan(panX, panY)
                    }
                    // Apply zoom
                    if (zoom != 1.0f) {
                        engine.adjustZoom(zoom.toDouble())
                    }
                    // Recalculate if anything changed
                    if (pan.x != 0f || pan.y != 0f || zoom != 1.0f) {
                        viewModel.requestRecalculation()
                    }
                }
            }
            .pointerInput(Unit) {
                // Double tap to zoom in
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val px = (offset.x / scale).toInt()
                        val py = (offset.y / scale).toInt()
                        engine.zoomTo(px, py, 2.0)
                        viewModel.requestRecalculation()
                    }
                )
            }
    ) {
        coloredData?.let { data ->
            drawFractal(data, width, height, scale.toFloat())
        }

        if (viewModel.isCalculating) {
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
        }
    }
}

private fun DrawScope.drawFractal(
    fractalData: Array<IntArray>,
    width: Int,
    height: Int,
    scale: Float
) {
    for (y in fractalData.indices) {
        for (x in 0 until fractalData[y].size) {
            val colorInt = fractalData[y][x]
            val color = Color(colorInt)
            drawRect(
                color = color,
                topLeft = Offset(x * scale, y * scale),
                size = androidx.compose.ui.geometry.Size(scale, scale)
            )
        }
    }
}

@Composable
fun SettingsDialog(
    viewModel: FractalViewModel,
    onDismiss: () -> Unit
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    val palettes = listOf("CLASSIC", "FIRE", "OCEAN", "RAINBOW", "PSYCHEDELIC", "GRAYSCALE")
    val iterationOptions = listOf(128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 100000)
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    "Color Palette",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                palettes.forEach { palette ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = userPreferences.colorPalette == palette,
                            onClick = { viewModel.updateColorPalette(palette) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(palette.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    "Max Iterations",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                iterationOptions.forEach { iter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = userPreferences.maxIterations == iter,
                            onClick = { viewModel.updateMaxIterations(iter) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(iter.toString())
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Animated", modifier = Modifier.padding(top = 8.dp))
                    Switch(
                        checked = userPreferences.isAnimated,
                        onCheckedChange = { viewModel.updateIsAnimated(it) }
                    )
                }

                if (userPreferences.isAnimated) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Animation Speed")
                    Slider(
                        value = userPreferences.animationSpeed,
                        onValueChange = { viewModel.updateAnimationSpeed(it) },
                        valueRange = 0.1f..2f
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Restore Last View", modifier = Modifier.padding(top = 8.dp))
                    Switch(
                        checked = userPreferences.restoreLastView,
                        onCheckedChange = { viewModel.updateRestoreLastView(it) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SaveBookmarkDialog(
    viewModel: FractalViewModel,
    engine: com.billybobbain.fractald.MandelbrotEngine,
    coloredData: Array<IntArray>,
    currentPalette: String,
    onDismiss: () -> Unit
) {
    var bookmarkName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Bookmark") },
        text = {
            Column {
                Text("Save current location and view settings")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = bookmarkName,
                    onValueChange = { bookmarkName = it },
                    label = { Text("Name") },
                    placeholder = { Text("Baby Mandelbrot, The Face, etc.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (bookmarkName.isNotBlank()) {
                        // Generate thumbnail
                        val thumbnail = try {
                            com.billybobbain.fractald.util.ThumbnailGenerator.generateThumbnail(
                                coloredData,
                                coloredData[0].size,
                                coloredData.size
                            )
                        } catch (e: Exception) {
                            null
                        }

                        // Save bookmark
                        viewModel.saveBookmark(
                            name = bookmarkName,
                            centerX = engine.centerX,
                            centerY = engine.centerY,
                            zoom = engine.zoom,
                            maxIterations = engine.maxIterations,
                            colorPalette = currentPalette,
                            thumbnail = thumbnail
                        )
                        onDismiss()
                    }
                },
                enabled = bookmarkName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookmarksListDialog(
    viewModel: FractalViewModel,
    onDismiss: () -> Unit,
    onRestoreBookmark: (com.billybobbain.fractald.data.Bookmark) -> Unit
) {
    val bookmarks by viewModel.allBookmarks.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bookmarks") },
        text = {
            if (bookmarks.isEmpty()) {
                Text("No bookmarks saved yet")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    bookmarks.forEach { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onRestore = {
                                onRestoreBookmark(bookmark)
                            },
                            onDelete = {
                                viewModel.deleteBookmark(bookmark)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BookmarkItem(
    bookmark: com.billybobbain.fractald.data.Bookmark,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        bookmark.thumbnail?.let { thumbnailBytes ->
            val bitmap = remember(thumbnailBytes) {
                com.billybobbain.fractald.util.ThumbnailGenerator.byteArrayToBitmap(thumbnailBytes)
            }
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Thumbnail",
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 12.dp)
            )
        }

        // Bookmark info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bookmark.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Zoom: ${String.format("%.2e", bookmark.zoom)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${bookmark.maxIterations} iterations",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Actions
        Column {
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Bookmark, "Restore")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}
