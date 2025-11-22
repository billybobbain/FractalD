package com.billybobbain.fractald.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserPreferences(
    val colorPalette: String = "RAINBOW",
    val isAnimated: Boolean = true,
    val animationSpeed: Float = 0.5f,
    val maxIterations: Int = 256,
    val lastCenterX: Double = -0.5,
    val lastCenterY: Double = 0.0,
    val lastZoom: Double = 0.6,
    val restoreLastView: Boolean = true
)

class PreferencesRepository(private val context: Context) {
    private val COLOR_PALETTE_KEY = stringPreferencesKey("color_palette")
    private val IS_ANIMATED_KEY = booleanPreferencesKey("is_animated")
    private val ANIMATION_SPEED_KEY = floatPreferencesKey("animation_speed")
    private val MAX_ITERATIONS_KEY = intPreferencesKey("max_iterations")
    private val LAST_CENTER_X_KEY = doublePreferencesKey("last_center_x")
    private val LAST_CENTER_Y_KEY = doublePreferencesKey("last_center_y")
    private val LAST_ZOOM_KEY = doublePreferencesKey("last_zoom")
    private val RESTORE_LAST_VIEW_KEY = booleanPreferencesKey("restore_last_view")

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                colorPalette = preferences[COLOR_PALETTE_KEY] ?: "RAINBOW",
                isAnimated = preferences[IS_ANIMATED_KEY] ?: true,
                animationSpeed = preferences[ANIMATION_SPEED_KEY] ?: 0.5f,
                maxIterations = preferences[MAX_ITERATIONS_KEY] ?: 256,
                lastCenterX = preferences[LAST_CENTER_X_KEY] ?: -0.5,
                lastCenterY = preferences[LAST_CENTER_Y_KEY] ?: 0.0,
                lastZoom = preferences[LAST_ZOOM_KEY] ?: 0.6,
                restoreLastView = preferences[RESTORE_LAST_VIEW_KEY] ?: true
            )
        }

    suspend fun updateColorPalette(palette: String) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_PALETTE_KEY] = palette
        }
    }

    suspend fun updateIsAnimated(isAnimated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_ANIMATED_KEY] = isAnimated
        }
    }

    suspend fun updateAnimationSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[ANIMATION_SPEED_KEY] = speed
        }
    }

    suspend fun updateMaxIterations(iterations: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_ITERATIONS_KEY] = iterations
        }
    }

    suspend fun updateLastViewState(centerX: Double, centerY: Double, zoom: Double) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CENTER_X_KEY] = centerX
            preferences[LAST_CENTER_Y_KEY] = centerY
            preferences[LAST_ZOOM_KEY] = zoom
        }
    }

    suspend fun updateRestoreLastView(restore: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RESTORE_LAST_VIEW_KEY] = restore
        }
    }
}
