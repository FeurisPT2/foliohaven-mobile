package com.example.foliohaven.data

import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.repository.FakeSharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {

    @Test
    fun readingPreferences_defaultValuesAndClamping() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        // Initial defaults
        assertEquals(16.0f, repo.defaultFontSize.value, 0.001f)
        assertEquals(1.6f, repo.defaultLineSpacing.value, 0.001f)
        assertEquals(ReaderTheme.SEPIA, repo.defaultReaderTheme.value)
        assertEquals(24, repo.defaultMarginsDp.value)

        // Setters with boundary clamping
        repo.setDefaultFontSize(10.0f) // below min 12f
        assertEquals(12.0f, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultFontSize(30.0f) // above max 28f
        assertEquals(28.0f, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultFontSize(Float.NaN) // NaN fallback to default
        assertEquals(16.0f, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultFontSize(Float.POSITIVE_INFINITY) // Infinity clamps to max
        assertEquals(28.0f, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultFontSize(Float.NEGATIVE_INFINITY) // -Infinity clamps to min
        assertEquals(12.0f, repo.defaultFontSize.value, 0.001f)

        repo.setDefaultLineSpacing(1.0f) // below min 1.2f
        assertEquals(1.2f, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultLineSpacing(2.5f) // above max 2.2f
        assertEquals(2.2f, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultLineSpacing(Float.NaN) // NaN fallback to default
        assertEquals(1.6f, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultLineSpacing(Float.POSITIVE_INFINITY)
        assertEquals(2.2f, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultLineSpacing(Float.NEGATIVE_INFINITY)
        assertEquals(1.2f, repo.defaultLineSpacing.value, 0.001f)

        repo.setDefaultMarginsDp(5) // below min 12
        assertEquals(12, repo.defaultMarginsDp.value)

        repo.setDefaultMarginsDp(40) // above max 36
        assertEquals(36, repo.defaultMarginsDp.value)
    }

    @Test
    fun ttsPreferences_defaultValuesAndClamping() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(1.0f, repo.ttsSpeechRate.value, 0.001f)
        assertEquals(1.0f, repo.ttsPitch.value, 0.001f)
        assertEquals("vi-VN", repo.ttsLanguage.value)

        repo.setTtsSpeechRate(0.2f) // below min 0.5f
        assertEquals(0.5f, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsSpeechRate(3.0f) // above max 2.0f
        assertEquals(2.0f, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsSpeechRate(Float.NaN) // NaN fallback
        assertEquals(1.0f, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsSpeechRate(Float.POSITIVE_INFINITY)
        assertEquals(2.0f, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsSpeechRate(Float.NEGATIVE_INFINITY)
        assertEquals(0.5f, repo.ttsSpeechRate.value, 0.001f)

        repo.setTtsPitch(0.1f) // below min 0.5f
        assertEquals(0.5f, repo.ttsPitch.value, 0.001f)

        repo.setTtsPitch(2.5f) // above max 2.0f
        assertEquals(2.0f, repo.ttsPitch.value, 0.001f)

        repo.setTtsPitch(Float.NaN) // NaN fallback
        assertEquals(1.0f, repo.ttsPitch.value, 0.001f)

        repo.setTtsPitch(Float.POSITIVE_INFINITY)
        assertEquals(2.0f, repo.ttsPitch.value, 0.001f)

        repo.setTtsPitch(Float.NEGATIVE_INFINITY)
        assertEquals(0.5f, repo.ttsPitch.value, 0.001f)

        repo.setTtsLanguage("  en-US  ")
        assertEquals("en-US", repo.ttsLanguage.value)

        repo.setTtsLanguage("   ")
        assertEquals("vi-VN", repo.ttsLanguage.value)
    }

    @Test
    fun simulatedRestart_persistsState() {
        val fakePrefs = FakeSharedPreferences()
        val repo1 = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo1.setDefaultFontSize(20f)
        repo1.setDefaultLineSpacing(1.8f)
        repo1.setDefaultReaderTheme(ReaderTheme.NIGHT)
        repo1.setDefaultMarginsDp(28)
        repo1.setTtsSpeechRate(1.2f)
        repo1.setTtsPitch(1.1f)
        repo1.setTtsLanguage("en-US")
        repo1.setGreetingCompleted(true)

        val repo2 = UserPreferencesRepository(injectedPrefs = fakePrefs)
        assertEquals(20f, repo2.defaultFontSize.value, 0.001f)
        assertEquals(1.8f, repo2.defaultLineSpacing.value, 0.001f)
        assertEquals(ReaderTheme.NIGHT, repo2.defaultReaderTheme.value)
        assertEquals(28, repo2.defaultMarginsDp.value)
        assertEquals(1.2f, repo2.ttsSpeechRate.value, 0.001f)
        assertEquals(1.1f, repo2.ttsPitch.value, 0.001f)
        assertEquals("en-US", repo2.ttsLanguage.value)
        assertTrue(repo2.isGreetingCompleted())
    }
}
