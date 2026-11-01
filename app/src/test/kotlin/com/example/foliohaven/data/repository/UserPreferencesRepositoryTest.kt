package com.example.foliohaven.data.repository

import android.content.SharedPreferences
import com.example.foliohaven.data.model.ReaderTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {

    // --- Greeting Tests (Preserved) ---

    @Test
    fun isGreetingCompleted_initiallyReturnsFalse() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertFalse(repo.isGreetingCompleted())
    }

    @Test
    fun setGreetingCompleted_updatesValue() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setGreetingCompleted(true)
        assertTrue(repo.isGreetingCompleted())

        repo.setGreetingCompleted(false)
        assertFalse(repo.isGreetingCompleted())
    }

    @Test
    fun initialAppLanguage_defaultsToVietnamese() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals("vi", repo.appLanguage.value)
        assertEquals("vi", repo.getAppLanguage())
    }

    @Test
    fun setAppLanguage_updatesStateFlowAndPersists() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setAppLanguage("en")
        assertEquals("en", repo.appLanguage.value)
        assertEquals("en", repo.getAppLanguage())

        repo.setAppLanguage("vi")
        assertEquals("vi", repo.appLanguage.value)
        assertEquals("vi", repo.getAppLanguage())

        // Fallback check on unexpected string
        repo.setAppLanguage("other")
        assertEquals("vi", repo.appLanguage.value)
    }

    // --- Reading Preferences Tests ---

    @Test
    fun initialReadingPreferences_haveCorrectDefaults() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(16.0f, repo.defaultFontSize.value, 0.001f)
        assertEquals(16.0f, repo.getDefaultFontSize(), 0.001f)

        assertEquals(1.6f, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(1.6f, repo.getDefaultLineSpacing(), 0.001f)

        assertEquals(ReaderTheme.SEPIA, repo.defaultReaderTheme.value)
        assertEquals(ReaderTheme.SEPIA, repo.defaultReadingTheme.value)
        assertEquals(ReaderTheme.SEPIA, repo.getDefaultReaderTheme())
        assertEquals(ReaderTheme.SEPIA, repo.getDefaultReadingTheme())

        assertEquals(24, repo.defaultMarginsDp.value)
        assertEquals(24, repo.defaultMargins.value)
        assertEquals(24, repo.getDefaultMarginsDp())
        assertEquals(24, repo.getDefaultMargins())
    }

    @Test
    fun setDefaultFontSize_updatesStateFlowAndGetterImmediately() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setDefaultFontSize(20.0f)
        assertEquals(20.0f, repo.defaultFontSize.value, 0.001f)
        assertEquals(20.0f, repo.getDefaultFontSize(), 0.001f)
    }

    @Test
    fun setDefaultFontSize_clampsValuesWithinBounds() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // Lower bound clamp (min = 12.0f)
        repo.setDefaultFontSize(8.0f)
        assertEquals(12.0f, repo.defaultFontSize.value, 0.001f)
        assertEquals(12.0f, repo.getDefaultFontSize(), 0.001f)

        // Upper bound clamp (max = 28.0f)
        repo.setDefaultFontSize(36.0f)
        assertEquals(28.0f, repo.defaultFontSize.value, 0.001f)
        assertEquals(28.0f, repo.getDefaultFontSize(), 0.001f)
    }

    @Test
    fun setDefaultLineSpacing_updatesStateFlowAndGetterImmediately() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setDefaultLineSpacing(1.8f)
        assertEquals(1.8f, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(1.8f, repo.getDefaultLineSpacing(), 0.001f)
    }

    @Test
    fun setDefaultLineSpacing_clampsValuesWithinBounds() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // Lower bound clamp (min = 1.2f)
        repo.setDefaultLineSpacing(0.9f)
        assertEquals(1.2f, repo.defaultLineSpacing.value, 0.001f)

        // Upper bound clamp (max = 2.2f)
        repo.setDefaultLineSpacing(3.0f)
        assertEquals(2.2f, repo.defaultLineSpacing.value, 0.001f)
    }

    @Test
    fun setDefaultReaderTheme_updatesBothModernAndLegacyKeys() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setDefaultReaderTheme(ReaderTheme.NIGHT)
        assertEquals(ReaderTheme.NIGHT, repo.defaultReaderTheme.value)
        assertEquals(ReaderTheme.NIGHT, repo.defaultReadingTheme.value)
        assertEquals(ReaderTheme.NIGHT, repo.getDefaultReaderTheme())
        assertEquals(ReaderTheme.NIGHT, repo.getDefaultReadingTheme())

        assertEquals(ReaderTheme.NIGHT.name, fakePrefs.getString(UserPreferencesRepository.KEY_DEFAULT_READER_THEME, null))
        assertEquals(ReaderTheme.NIGHT.name, fakePrefs.getString(UserPreferencesRepository.KEY_LEGACY_READER_THEME, null))

        // Alias method check
        repo.setDefaultReadingTheme(ReaderTheme.DARK_SEPIA)
        assertEquals(ReaderTheme.DARK_SEPIA, repo.defaultReaderTheme.value)
        assertEquals(ReaderTheme.DARK_SEPIA, repo.getDefaultReaderTheme())
    }

    @Test
    fun setDefaultMarginsDp_clampsValuesWithinBounds() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setDefaultMarginsDp(18)
        assertEquals(18, repo.defaultMarginsDp.value)
        assertEquals(18, repo.getDefaultMarginsDp())

        // Lower bound clamp (min = 12)
        repo.setDefaultMarginsDp(6)
        assertEquals(12, repo.defaultMarginsDp.value)

        // Upper bound clamp (max = 36)
        repo.setDefaultMargins(48)
        assertEquals(36, repo.defaultMarginsDp.value)
    }

    // --- TTS Preferences Tests ---

    @Test
    fun initialTtsPreferences_haveCorrectDefaults() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(1.0f, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(1.0f, repo.getTtsSpeechRate(), 0.001f)

        assertEquals(1.0f, repo.ttsPitch.value, 0.001f)
        assertEquals(1.0f, repo.getTtsPitch(), 0.001f)

        assertEquals("vi-VN", repo.ttsLanguage.value)
        assertEquals("vi-VN", repo.getTtsLanguage())
    }

    @Test
    fun setTtsSpeechRate_updatesAndClamps() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setTtsSpeechRate(1.5f)
        assertEquals(1.5f, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(1.5f, repo.getTtsSpeechRate(), 0.001f)

        // Below min clamp (0.5f)
        repo.setTtsSpeechRate(0.2f)
        assertEquals(0.5f, repo.ttsSpeechRate.value, 0.001f)

        // Above max clamp (2.0f)
        repo.setTtsSpeechRate(3.0f)
        assertEquals(2.0f, repo.ttsSpeechRate.value, 0.001f)
    }

    @Test
    fun setTtsPitch_updatesAndClamps() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setTtsPitch(0.8f)
        assertEquals(0.8f, repo.ttsPitch.value, 0.001f)
        assertEquals(0.8f, repo.getTtsPitch(), 0.001f)

        // Below min clamp (0.5f)
        repo.setTtsPitch(0.1f)
        assertEquals(0.5f, repo.ttsPitch.value, 0.001f)

        // Above max clamp (2.0f)
        repo.setTtsPitch(2.5f)
        assertEquals(2.0f, repo.ttsPitch.value, 0.001f)
    }

    @Test
    fun setTtsLanguage_sanitizesAndFallsBackOnBlank() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setTtsLanguage("  en-US  ")
        assertEquals("en-US", repo.ttsLanguage.value)
        assertEquals("en-US", repo.getTtsLanguage())

        // Blank string falls back to vi-VN
        repo.setTtsLanguage("   ")
        assertEquals("vi-VN", repo.ttsLanguage.value)

        repo.setTtsLanguage("")
        assertEquals("vi-VN", repo.ttsLanguage.value)
    }

    // --- Persistence & Simulated Restart Tests ---

    @Test
    fun simulatedRestart_persistsAllReadingAndTtsPreferences() {
        val fakePrefs = FakeSharedPreferences()
        val initialRepo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // Modify reading preferences
        initialRepo.setDefaultFontSize(22.0f)
        initialRepo.setDefaultLineSpacing(1.9f)
        initialRepo.setDefaultReaderTheme(ReaderTheme.DARK_SEPIA)
        initialRepo.setDefaultMarginsDp(30)

        // Modify TTS preferences
        initialRepo.setTtsSpeechRate(1.25f)
        initialRepo.setTtsPitch(0.85f)
        initialRepo.setTtsLanguage("en-GB")
        initialRepo.setGreetingCompleted(true)

        // Recreate repository simulating app restart
        val restartedRepo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(22.0f, restartedRepo.defaultFontSize.value, 0.001f)
        assertEquals(22.0f, restartedRepo.getDefaultFontSize(), 0.001f)
        assertEquals(1.9f, restartedRepo.defaultLineSpacing.value, 0.001f)
        assertEquals(1.9f, restartedRepo.getDefaultLineSpacing(), 0.001f)
        assertEquals(ReaderTheme.DARK_SEPIA, restartedRepo.defaultReaderTheme.value)
        assertEquals(ReaderTheme.DARK_SEPIA, restartedRepo.getDefaultReaderTheme())
        assertEquals(30, restartedRepo.defaultMarginsDp.value)
        assertEquals(30, restartedRepo.getDefaultMarginsDp())

        assertEquals(1.25f, restartedRepo.ttsSpeechRate.value, 0.001f)
        assertEquals(1.25f, restartedRepo.getTtsSpeechRate(), 0.001f)
        assertEquals(0.85f, restartedRepo.ttsPitch.value, 0.001f)
        assertEquals(0.85f, restartedRepo.getTtsPitch(), 0.001f)
        assertEquals("en-GB", restartedRepo.ttsLanguage.value)
        assertEquals("en-GB", restartedRepo.getTtsLanguage())
        assertTrue(restartedRepo.isGreetingCompleted())
    }

    @Test
    fun simulatedRestart_withCorruptedStorage_clampsToValidRanges() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.edit()
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_FONT_SIZE, -10f)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_LINE_SPACING, 99f)
            .putString(UserPreferencesRepository.KEY_DEFAULT_READER_THEME, "NON_EXISTENT_THEME")
            .putInt(UserPreferencesRepository.KEY_DEFAULT_MARGINS, 100)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_SPEECH_RATE, 0.01f)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_PITCH, 50.0f)
            .putString(UserPreferencesRepository.KEY_DEFAULT_TTS_LANGUAGE, "   ")
            .apply()

        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(UserPreferencesRepository.MIN_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
        assertEquals(UserPreferencesRepository.MAX_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_READER_THEME, repo.defaultReaderTheme.value)
        assertEquals(UserPreferencesRepository.MAX_MARGINS, repo.defaultMarginsDp.value)
        assertEquals(UserPreferencesRepository.MIN_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(UserPreferencesRepository.MAX_TTS_PITCH, repo.ttsPitch.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_LANGUAGE, repo.ttsLanguage.value)
    }

    @Test
    fun legacyReaderThemeKey_isHonoredIfModernKeyMissing() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.edit()
            .putString(UserPreferencesRepository.KEY_LEGACY_READER_THEME, ReaderTheme.NIGHT.name)
            .apply()

        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)
        assertEquals(ReaderTheme.NIGHT, repo.defaultReaderTheme.value)
    }

    // --- Float.NaN & Infinity Guard Tests ---

    @Test
    fun setDefaultFontSize_guardsAgainstNaNAndInfinities() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // Float.NaN must safely fall back to DEFAULT_FONT_SIZE
        repo.setDefaultFontSize(20.0f)
        assertEquals(20.0f, repo.defaultFontSize.value, 0.001f)
        repo.setDefaultFontSize(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_FONT_SIZE, repo.getDefaultFontSize(), 0.001f)
        assertFalse(repo.defaultFontSize.value.isNaN())

        // Positive infinity clamps to MAX_FONT_SIZE
        repo.setDefaultFontSize(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_FONT_SIZE, repo.defaultFontSize.value, 0.001f)

        // Negative infinity clamps to MIN_FONT_SIZE
        repo.setDefaultFontSize(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
    }

    @Test
    fun setDefaultLineSpacing_guardsAgainstNaNAndInfinities() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setDefaultLineSpacing(1.8f)
        assertEquals(1.8f, repo.defaultLineSpacing.value, 0.001f)
        repo.setDefaultLineSpacing(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_LINE_SPACING, repo.getDefaultLineSpacing(), 0.001f)
        assertFalse(repo.defaultLineSpacing.value.isNaN())

        repo.setDefaultLineSpacing(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultLineSpacing(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)
    }

    @Test
    fun setTtsSpeechRate_guardsAgainstNaNAndInfinities() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setTtsSpeechRate(1.5f)
        assertEquals(1.5f, repo.ttsSpeechRate.value, 0.001f)
        repo.setTtsSpeechRate(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_SPEECH_RATE, repo.getTtsSpeechRate(), 0.001f)
        assertFalse(repo.ttsSpeechRate.value.isNaN())

        repo.setTtsSpeechRate(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsSpeechRate(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)
    }

    @Test
    fun setTtsPitch_guardsAgainstNaNAndInfinities() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setTtsPitch(1.3f)
        assertEquals(1.3f, repo.ttsPitch.value, 0.001f)
        repo.setTtsPitch(Float.NaN)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_PITCH, repo.ttsPitch.value, 0.001f)
        assertEquals(UserPreferencesRepository.DEFAULT_TTS_PITCH, repo.getTtsPitch(), 0.001f)
        assertFalse(repo.ttsPitch.value.isNaN())

        repo.setTtsPitch(Float.POSITIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MAX_TTS_PITCH, repo.ttsPitch.value, 0.001f)

        repo.setTtsPitch(Float.NEGATIVE_INFINITY)
        assertEquals(UserPreferencesRepository.MIN_TTS_PITCH, repo.ttsPitch.value, 0.001f)
    }

    @Test
    fun simulatedRestart_withNaNInStorage_hydratesToDefaultValues() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.edit()
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_FONT_SIZE, Float.NaN)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_LINE_SPACING, Float.NaN)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_SPEECH_RATE, Float.NaN)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_PITCH, Float.NaN)
            .apply()

        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(UserPreferencesRepository.DEFAULT_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
        assertFalse(repo.defaultFontSize.value.isNaN())

        assertEquals(UserPreferencesRepository.DEFAULT_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)
        assertFalse(repo.defaultLineSpacing.value.isNaN())

        assertEquals(UserPreferencesRepository.DEFAULT_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)
        assertFalse(repo.ttsSpeechRate.value.isNaN())

        assertEquals(UserPreferencesRepository.DEFAULT_TTS_PITCH, repo.ttsPitch.value, 0.001f)
        assertFalse(repo.ttsPitch.value.isNaN())
    }

    @Test
    fun simulatedRestart_withInfinitiesInStorage_clampsToMinMaxBounds() {
        val fakePrefs = FakeSharedPreferences()
        fakePrefs.edit()
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_FONT_SIZE, Float.POSITIVE_INFINITY)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_LINE_SPACING, Float.NEGATIVE_INFINITY)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_SPEECH_RATE, Float.POSITIVE_INFINITY)
            .putFloat(UserPreferencesRepository.KEY_DEFAULT_TTS_PITCH, Float.NEGATIVE_INFINITY)
            .apply()

        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(UserPreferencesRepository.MAX_FONT_SIZE, repo.defaultFontSize.value, 0.001f)
        assertEquals(UserPreferencesRepository.MIN_LINE_SPACING, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(UserPreferencesRepository.MAX_TTS_SPEECH_RATE, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(UserPreferencesRepository.MIN_TTS_PITCH, repo.ttsPitch.value, 0.001f)
    }
}

class FakeSharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map
    override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String> ?: defValues)
    override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor(map)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class FakeEditor(private val storage: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply { key?.let { temp[it] = value } }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply { key?.let { temp[it] = values } }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply { key?.let { temp[it] = value } }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply { key?.let { temp[it] = value } }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply { key?.let { temp[it] = value } }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply { key?.let { temp[it] = value } }
        override fun remove(key: String?): SharedPreferences.Editor = apply { key?.let { temp.remove(it); storage.remove(it) } }
        override fun clear(): SharedPreferences.Editor = apply { temp.clear(); storage.clear() }
        override fun commit(): Boolean {
            storage.putAll(temp)
            return true
        }
        override fun apply() {
            storage.putAll(temp)
        }
    }
}
