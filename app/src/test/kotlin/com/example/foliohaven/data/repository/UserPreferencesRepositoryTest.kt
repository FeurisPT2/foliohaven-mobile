package com.example.foliohaven.data.repository

import android.content.SharedPreferences
import com.example.foliohaven.data.model.ReaderTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {

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
    fun defaultReadingTheme_defaultsToSepia() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(ReaderTheme.SEPIA, repo.getDefaultReadingTheme())
    }

    @Test
    fun setDefaultReadingTheme_persistsThemeSelection() {
        val fakePrefs = FakeSharedPreferences()
        val repo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        repo.setDefaultReadingTheme(ReaderTheme.NIGHT)
        assertEquals(ReaderTheme.NIGHT, repo.getDefaultReadingTheme())

        repo.setDefaultReadingTheme(ReaderTheme.DARK_SEPIA)
        assertEquals(ReaderTheme.DARK_SEPIA, repo.getDefaultReadingTheme())

        repo.setDefaultReadingTheme(ReaderTheme.DAY)
        assertEquals(ReaderTheme.DAY, repo.getDefaultReadingTheme())
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
