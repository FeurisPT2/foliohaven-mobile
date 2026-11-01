package com.example.foliohaven.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.foliohaven.data.model.ReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(
    context: Context? = null,
    injectedPrefs: SharedPreferences? = null
) {

    private val prefs: SharedPreferences = injectedPrefs ?: context?.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    ) ?: throw IllegalArgumentException("Either context or injectedPrefs must be provided")

    companion object {
        const val PREFS_NAME = "foliohaven_preferences"
        const val KEY_GREETING_COMPLETED = "has_completed_greeting"

        // Reading Preference Keys & Defaults
        const val KEY_DEFAULT_FONT_SIZE = "pref_default_font_size"
        const val DEFAULT_FONT_SIZE = 16.0f
        const val MIN_FONT_SIZE = 12.0f
        const val MAX_FONT_SIZE = 28.0f

        const val KEY_DEFAULT_LINE_SPACING = "pref_default_line_spacing"
        const val DEFAULT_LINE_SPACING = 1.6f
        const val MIN_LINE_SPACING = 1.2f
        const val MAX_LINE_SPACING = 2.2f

        const val KEY_DEFAULT_READER_THEME = "pref_default_reader_theme"
        const val KEY_LEGACY_READER_THEME = "default_reader_theme"
        val DEFAULT_READER_THEME = ReaderTheme.SEPIA

        const val KEY_DEFAULT_MARGINS = "pref_default_margins"
        const val DEFAULT_MARGINS = 24
        const val MIN_MARGINS = 12
        const val MAX_MARGINS = 36

        // TTS Preference Keys & Defaults
        const val KEY_DEFAULT_TTS_SPEECH_RATE = "pref_default_tts_speech_rate"
        const val DEFAULT_TTS_SPEECH_RATE = 1.0f
        const val MIN_TTS_SPEECH_RATE = 0.5f
        const val MAX_TTS_SPEECH_RATE = 2.0f

        const val KEY_DEFAULT_TTS_PITCH = "pref_default_tts_pitch"
        const val DEFAULT_TTS_PITCH = 1.0f
        const val MIN_TTS_PITCH = 0.5f
        const val MAX_TTS_PITCH = 2.0f

        const val KEY_DEFAULT_TTS_LANGUAGE = "pref_default_tts_language"
        const val DEFAULT_TTS_LANGUAGE = "vi-VN"

        const val KEY_APP_LANGUAGE = "pref_app_language"
        const val DEFAULT_APP_LANGUAGE = "vi"

        const val KEY_USE_DYNAMIC_COLOR = "pref_use_dynamic_color"
        const val DEFAULT_USE_DYNAMIC_COLOR = false
    }

    // Reactive StateFlow properties backed by MutableStateFlow
    private val _useDynamicColor = MutableStateFlow(readStoredUseDynamicColor())
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    private val _appLanguage = MutableStateFlow(readStoredAppLanguage())
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _defaultFontSize = MutableStateFlow(readStoredFontSize())
    val defaultFontSize: StateFlow<Float> = _defaultFontSize.asStateFlow()

    private val _defaultLineSpacing = MutableStateFlow(readStoredLineSpacing())
    val defaultLineSpacing: StateFlow<Float> = _defaultLineSpacing.asStateFlow()

    private val _defaultReaderTheme = MutableStateFlow(readStoredReaderTheme())
    val defaultReaderTheme: StateFlow<ReaderTheme> = _defaultReaderTheme.asStateFlow()
    val defaultReadingTheme: StateFlow<ReaderTheme> get() = defaultReaderTheme

    private val _defaultMarginsDp = MutableStateFlow(readStoredMarginsDp())
    val defaultMarginsDp: StateFlow<Int> = _defaultMarginsDp.asStateFlow()
    val defaultMargins: StateFlow<Int> get() = defaultMarginsDp

    private val _ttsSpeechRate = MutableStateFlow(readStoredTtsSpeechRate())
    val ttsSpeechRate: StateFlow<Float> = _ttsSpeechRate.asStateFlow()

    private val _ttsPitch = MutableStateFlow(readStoredTtsPitch())
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()

    private val _ttsLanguage = MutableStateFlow(readStoredTtsLanguage())
    val ttsLanguage: StateFlow<String> = _ttsLanguage.asStateFlow()

    // Greeting status
    fun isGreetingCompleted(): Boolean {
        return prefs.getBoolean(KEY_GREETING_COMPLETED, false)
    }

    private fun Float.sanitize(min: Float, max: Float, default: Float): Float =
        if (isNaN()) default else coerceIn(min, max)

    @Synchronized
    fun setGreetingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_GREETING_COMPLETED, completed).apply()
    }

    // Reading preference setters with boundary clamping and direct StateFlow update
    @Synchronized
    fun setDefaultFontSize(size: Float) {
        val clamped = size.sanitize(MIN_FONT_SIZE, MAX_FONT_SIZE, DEFAULT_FONT_SIZE)
        prefs.edit().putFloat(KEY_DEFAULT_FONT_SIZE, clamped).apply()
        _defaultFontSize.value = clamped
    }

    @Synchronized
    fun setDefaultLineSpacing(spacing: Float) {
        val clamped = spacing.sanitize(MIN_LINE_SPACING, MAX_LINE_SPACING, DEFAULT_LINE_SPACING)
        prefs.edit().putFloat(KEY_DEFAULT_LINE_SPACING, clamped).apply()
        _defaultLineSpacing.value = clamped
    }

    @Synchronized
    fun setDefaultReaderTheme(theme: ReaderTheme) {
        prefs.edit()
            .putString(KEY_DEFAULT_READER_THEME, theme.name)
            .putString(KEY_LEGACY_READER_THEME, theme.name)
            .apply()
        _defaultReaderTheme.value = theme
    }

    @Synchronized
    fun setDefaultReadingTheme(theme: ReaderTheme) = setDefaultReaderTheme(theme)

    @Synchronized
    fun setDefaultMarginsDp(marginsDp: Int) {
        val clamped = marginsDp.coerceIn(MIN_MARGINS, MAX_MARGINS)
        prefs.edit().putInt(KEY_DEFAULT_MARGINS, clamped).apply()
        _defaultMarginsDp.value = clamped
    }

    @Synchronized
    fun setDefaultMargins(marginsDp: Int) = setDefaultMarginsDp(marginsDp)

    // TTS preference setters with boundary clamping and direct StateFlow update
    @Synchronized
    fun setTtsSpeechRate(rate: Float) {
        val clamped = rate.sanitize(MIN_TTS_SPEECH_RATE, MAX_TTS_SPEECH_RATE, DEFAULT_TTS_SPEECH_RATE)
        prefs.edit().putFloat(KEY_DEFAULT_TTS_SPEECH_RATE, clamped).apply()
        _ttsSpeechRate.value = clamped
    }

    @Synchronized
    fun setTtsPitch(pitch: Float) {
        val clamped = pitch.sanitize(MIN_TTS_PITCH, MAX_TTS_PITCH, DEFAULT_TTS_PITCH)
        prefs.edit().putFloat(KEY_DEFAULT_TTS_PITCH, clamped).apply()
        _ttsPitch.value = clamped
    }

    @Synchronized
    fun setTtsLanguage(language: String) {
        val sanitized = language.trim().ifBlank { DEFAULT_TTS_LANGUAGE }
        prefs.edit().putString(KEY_DEFAULT_TTS_LANGUAGE, sanitized).apply()
        _ttsLanguage.value = sanitized
    }

    @Synchronized
    fun setAppLanguage(language: String) {
        val sanitized = if (language.equals("en", ignoreCase = true)) "en" else "vi"
        prefs.edit().putString(KEY_APP_LANGUAGE, sanitized).apply()
        _appLanguage.value = sanitized
    }

    @Synchronized
    fun setUseDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_DYNAMIC_COLOR, enabled).apply()
        _useDynamicColor.value = enabled
    }

    // Synchronous getters
    fun isDynamicColorEnabled(): Boolean = useDynamicColor.value
    fun getAppLanguage(): String = appLanguage.value
    fun getDefaultFontSize(): Float = defaultFontSize.value
    fun getDefaultLineSpacing(): Float = defaultLineSpacing.value
    fun getDefaultReaderTheme(): ReaderTheme = defaultReaderTheme.value
    fun getDefaultReadingTheme(): ReaderTheme = defaultReaderTheme.value
    fun getDefaultMarginsDp(): Int = defaultMarginsDp.value
    fun getDefaultMargins(): Int = defaultMarginsDp.value
    fun getTtsSpeechRate(): Float = ttsSpeechRate.value
    fun getTtsPitch(): Float = ttsPitch.value
    fun getTtsLanguage(): String = ttsLanguage.value

    // Internal safe storage hydration methods
    private fun readStoredFontSize(): Float =
        prefs.getFloat(KEY_DEFAULT_FONT_SIZE, DEFAULT_FONT_SIZE)
            .sanitize(MIN_FONT_SIZE, MAX_FONT_SIZE, DEFAULT_FONT_SIZE)

    private fun readStoredLineSpacing(): Float =
        prefs.getFloat(KEY_DEFAULT_LINE_SPACING, DEFAULT_LINE_SPACING)
            .sanitize(MIN_LINE_SPACING, MAX_LINE_SPACING, DEFAULT_LINE_SPACING)

    private fun readStoredReaderTheme(): ReaderTheme {
        val name = prefs.getString(KEY_DEFAULT_READER_THEME, null)
            ?: prefs.getString(KEY_LEGACY_READER_THEME, null)
            ?: DEFAULT_READER_THEME.name
        return try {
            ReaderTheme.valueOf(name)
        } catch (_: Exception) {
            DEFAULT_READER_THEME
        }
    }

    private fun readStoredMarginsDp(): Int =
        prefs.getInt(KEY_DEFAULT_MARGINS, DEFAULT_MARGINS).coerceIn(MIN_MARGINS, MAX_MARGINS)

    private fun readStoredTtsSpeechRate(): Float =
        prefs.getFloat(KEY_DEFAULT_TTS_SPEECH_RATE, DEFAULT_TTS_SPEECH_RATE)
            .sanitize(MIN_TTS_SPEECH_RATE, MAX_TTS_SPEECH_RATE, DEFAULT_TTS_SPEECH_RATE)

    private fun readStoredTtsPitch(): Float =
        prefs.getFloat(KEY_DEFAULT_TTS_PITCH, DEFAULT_TTS_PITCH)
            .sanitize(MIN_TTS_PITCH, MAX_TTS_PITCH, DEFAULT_TTS_PITCH)

    private fun readStoredTtsLanguage(): String {
        val raw = prefs.getString(KEY_DEFAULT_TTS_LANGUAGE, DEFAULT_TTS_LANGUAGE)
        return raw?.trim()?.ifBlank { DEFAULT_TTS_LANGUAGE } ?: DEFAULT_TTS_LANGUAGE
    }

    private fun readStoredAppLanguage(): String {
        val raw = prefs.getString(KEY_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)
        return if (raw?.equals("en", ignoreCase = true) == true) "en" else "vi"
    }

    private fun readStoredUseDynamicColor(): Boolean =
        prefs.getBoolean(KEY_USE_DYNAMIC_COLOR, DEFAULT_USE_DYNAMIC_COLOR)
}
