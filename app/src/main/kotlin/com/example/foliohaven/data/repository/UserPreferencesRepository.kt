package com.example.foliohaven.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.foliohaven.data.model.ReaderTheme

class UserPreferencesRepository(
    context: Context? = null,
    injectedPrefs: SharedPreferences? = null
) {

    private val prefs: SharedPreferences = injectedPrefs ?: context?.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    ) ?: throw IllegalArgumentException("Either context or injectedPrefs must be provided")

    companion object {
        private const val PREFS_NAME = "foliohaven_preferences"
        private const val KEY_GREETING_COMPLETED = "has_completed_greeting"
        private const val KEY_DEFAULT_READER_THEME = "default_reader_theme"
    }

    fun isGreetingCompleted(): Boolean {
        return prefs.getBoolean(KEY_GREETING_COMPLETED, false)
    }

    fun setGreetingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_GREETING_COMPLETED, completed).apply()
    }

    fun getDefaultReadingTheme(): ReaderTheme {
        val name = prefs.getString(KEY_DEFAULT_READER_THEME, ReaderTheme.SEPIA.name)
        return try {
            ReaderTheme.valueOf(name ?: ReaderTheme.SEPIA.name)
        } catch (_: Exception) {
            ReaderTheme.SEPIA
        }
    }

    fun setDefaultReadingTheme(theme: ReaderTheme) {
        prefs.edit().putString(KEY_DEFAULT_READER_THEME, theme.name).apply()
    }
}
