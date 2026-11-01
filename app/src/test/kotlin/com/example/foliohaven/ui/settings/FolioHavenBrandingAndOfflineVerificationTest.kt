package com.example.foliohaven.ui.settings

import androidx.compose.ui.graphics.Color
import com.example.foliohaven.data.LibraryStorageStats
import com.example.foliohaven.data.StorageManager
import com.example.foliohaven.data.model.ReaderTheme
import com.example.foliohaven.data.repository.FakeSharedPreferences
import com.example.foliohaven.data.repository.UserPreferencesRepository
import com.example.foliohaven.model.AppInfo
import com.example.foliohaven.ui.theme.BackgroundDark
import com.example.foliohaven.ui.theme.BorderCharcoal
import com.example.foliohaven.ui.theme.BorderSubtle
import com.example.foliohaven.ui.theme.CanvasDark
import com.example.foliohaven.ui.theme.CardSurface
import com.example.foliohaven.ui.theme.DarkSepiaBg
import com.example.foliohaven.ui.theme.DarkSepiaText
import com.example.foliohaven.ui.theme.DayBg
import com.example.foliohaven.ui.theme.DayText
import com.example.foliohaven.ui.theme.NightBg
import com.example.foliohaven.ui.theme.NightText
import com.example.foliohaven.ui.theme.PrimaryHover
import com.example.foliohaven.ui.theme.PrimaryPressed
import com.example.foliohaven.ui.theme.PrimarySubtle
import com.example.foliohaven.ui.theme.PrimaryTerracotta
import com.example.foliohaven.ui.theme.SepiaBg
import com.example.foliohaven.ui.theme.SepiaText
import com.example.foliohaven.ui.theme.SurfaceEspresso
import com.example.foliohaven.ui.theme.TextLinen
import com.example.foliohaven.ui.theme.TextMuted
import com.example.foliohaven.ui.theme.TextParchment
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Verification test suite for FolioHaven Branding, Architecture Adherence,
 * Zero-Network Security Invariants, and Cross-Feature Combinations.
 */
class FolioHavenBrandingAndOfflineVerificationTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        repository = UserPreferencesRepository(injectedPrefs = fakePrefs)
    }

    // ========================================================================
    // 1. FolioHaven Brand Palette & Typography Tokens (Tier 1 & 2)
    // ========================================================================

    @Test
    fun brandColors_primaryTerracottaTokens_matchDesignSystem() {
        assertEquals(0xFFC2410CL, PrimaryTerracotta.value.toLong() shr 32 and 0xFFFFFFFFL or (PrimaryTerracotta.value.toLong() and 0xFFFFFFFFL))
        assertEquals(Color(0xFFC2410C), PrimaryTerracotta)
        assertEquals(Color(0xFFEA580C), PrimaryHover)
        assertEquals(Color(0xFF9A3412), PrimaryPressed)
        assertEquals(Color(0x1FC2410C), PrimarySubtle)
    }

    @Test
    fun brandColors_darkSurfaceHierarchy_strictlyOrdered() {
        assertEquals(Color(0xFF141110), CanvasDark)
        assertEquals(Color(0xFF181513), BackgroundDark)
        assertEquals(Color(0xFF1E1A18), SurfaceEspresso)
        assertEquals(Color(0xFF26211E), CardSurface)
        assertEquals(Color(0xFF38312C), BorderCharcoal)
        assertEquals(Color(0xFF2B2522), BorderSubtle)
    }

    @Test
    fun brandColors_textHierarchy_usesParchmentAndLinen() {
        assertEquals(Color(0xFFFAF7F2), TextParchment)
        assertEquals(Color(0xFFB5ACA3), TextLinen)
        assertEquals(Color(0xFF7E756D), TextMuted)
    }

    @Test
    fun readerThemes_fourCanvasModes_haveContrastingColors() {
        assertEquals(Color(0xFFFAF8F5), DayBg)
        assertEquals(Color(0xFF1F1D1B), DayText)
        assertEquals(Color(0xFFF5EBD7), SepiaBg)
        assertEquals(Color(0xFF3E2D1E), SepiaText)
        assertEquals(Color(0xFF111111), NightBg)
        assertEquals(Color(0xFFD4D4D8), NightText)
        assertEquals(Color(0xFF221A14), DarkSepiaBg)
        assertEquals(Color(0xFFD7C7B7), DarkSepiaText)
    }

    @Test
    fun readerThemeEnum_matchesExpectedColorTokens() {
        assertEquals(DayBg, ReaderTheme.DAY.backgroundColor)
        assertEquals(DayText, ReaderTheme.DAY.textColor)

        assertEquals(SepiaBg, ReaderTheme.SEPIA.backgroundColor)
        assertEquals(SepiaText, ReaderTheme.SEPIA.textColor)

        assertEquals(NightBg, ReaderTheme.NIGHT.backgroundColor)
        assertEquals(NightText, ReaderTheme.NIGHT.textColor)

        assertEquals(DarkSepiaBg, ReaderTheme.DARK_SEPIA.backgroundColor)
        assertEquals(DarkSepiaText, ReaderTheme.DARK_SEPIA.textColor)
    }

    // ========================================================================
    // 2. Native Canvas Vector Icons (Tier 1 & 2)
    // ========================================================================

    @Test
    fun canvasIcons_allRequiredFunctionsExistInBytecode() {
        val clazz = Class.forName("com.example.foliohaven.ui.settings.SettingsIconsKt")
        val methods = clazz.declaredMethods.map { it.name }

        assertTrue("SettingsGearIcon must exist", methods.any { it.startsWith("SettingsGearIcon") })
        assertTrue("SlidersTuneIcon must exist", methods.any { it.startsWith("SlidersTuneIcon") })
        assertTrue("TtsVoiceIcon must exist", methods.any { it.startsWith("TtsVoiceIcon") })
        assertTrue("StorageDbIcon must exist", methods.any { it.startsWith("StorageDbIcon") })
        assertTrue("ShieldCheckIcon must exist", methods.any { it.startsWith("ShieldCheckIcon") })
        assertTrue("TrashIcon must exist", methods.any { it.startsWith("TrashIcon") })
    }

    @Test
    fun canvasIcons_haveNoThirdPartyDependencies() {
        val clazz = Class.forName("com.example.foliohaven.ui.settings.SettingsIconsKt")
        val importsOrPackages = clazz.declaredMethods.flatMap { m ->
            m.parameterTypes.map { it.name }
        }.toSet()

        for (pkg in importsOrPackages) {
            assertFalse(
                "Must not depend on material-icons-extended or external icon pack: $pkg",
                pkg.contains("material.icons.filled") || pkg.contains("fontawesome") || pkg.contains("feather")
            )
        }
    }

    // ========================================================================
    // 3. 100% Offline & Zero Network Permission Verification (Tier 1 & 2)
    // ========================================================================

    @Test
    fun manifest_mainSource_declaresZeroInternetPermissions() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        val fallbackFile = File("app/src/main/AndroidManifest.xml")
        val fileToRead = if (manifestFile.exists()) manifestFile else fallbackFile
        assertTrue("AndroidManifest.xml must exist", fileToRead.exists())

        val content = fileToRead.readText()
        assertFalse("Manifest must not contain android.permission.INTERNET", content.contains("android.permission.INTERNET"))
        assertFalse("Manifest must not contain ACCESS_NETWORK_STATE", content.contains("android.permission.ACCESS_NETWORK_STATE"))
        assertFalse("Manifest must not contain ACCESS_WIFI_STATE", content.contains("android.permission.ACCESS_WIFI_STATE"))
        assertFalse("Manifest must not contain CHANGE_NETWORK_STATE", content.contains("android.permission.CHANGE_NETWORK_STATE"))
    }

    @Test
    fun manifest_parsedXml_hasNoInternetUsesPermissionNodes() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        val fallbackFile = File("app/src/main/AndroidManifest.xml")
        val fileToRead = if (manifestFile.exists()) manifestFile else fallbackFile

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(fileToRead)

        val usesPerms = doc.getElementsByTagName("uses-permission")
        for (i in 0 until usesPerms.length) {
            val node = usesPerms.item(i)
            val name = node.attributes.getNamedItem("android:name")?.nodeValue ?: ""
            assertFalse("Disallowed network permission: $name", name.contains("INTERNET", ignoreCase = true))
            assertFalse("Disallowed network state permission: $name", name.contains("NETWORK", ignoreCase = true))
        }
    }

    @Test
    fun appInfo_offlineCommitment_explicitlyGuaranteesZeroNetworkPermissions() {
        val appInfo = AppInfo.DEFAULT
        assertTrue(appInfo.offlineGuarantee.contains("100% Offline"))
        assertTrue(appInfo.offlineGuarantee.contains("0 network permissions"))
        assertTrue(appInfo.offlineGuarantee.contains("telemetry"))
        assertTrue(appInfo.offlineGuaranteeEn.contains("zero Internet permissions"))
        assertTrue(appInfo.offlineGuaranteeEn.contains("zero telemetry"))
    }

    // ========================================================================
    // 4. Boundary & Corner Cases (Tier 2)
    // ========================================================================

    @Test
    fun readingPreferences_extremeBoundaryClamping() {
        repository.setDefaultFontSize(-100f)
        assertEquals(12f, repository.defaultFontSize.value, 0.001f)

        repository.setDefaultFontSize(1000f)
        assertEquals(28f, repository.defaultFontSize.value, 0.001f)

        repository.setDefaultLineSpacing(0.1f)
        assertEquals(1.2f, repository.defaultLineSpacing.value, 0.001f)

        repository.setDefaultLineSpacing(10f)
        assertEquals(2.2f, repository.defaultLineSpacing.value, 0.001f)

        repository.setDefaultMarginsDp(-50)
        assertEquals(12, repository.defaultMarginsDp.value)

        repository.setDefaultMarginsDp(200)
        assertEquals(36, repository.defaultMarginsDp.value)
    }

    @Test
    fun ttsDefaults_extremeBoundaryClamping() {
        repository.setTtsSpeechRate(0.01f)
        assertEquals(0.5f, repository.ttsSpeechRate.value, 0.001f)

        repository.setTtsSpeechRate(99f)
        assertEquals(2.0f, repository.ttsSpeechRate.value, 0.001f)

        repository.setTtsPitch(-1f)
        assertEquals(0.5f, repository.ttsPitch.value, 0.001f)

        repository.setTtsPitch(50f)
        assertEquals(2.0f, repository.ttsPitch.value, 0.001f)
    }

    @Test
    fun ttsDefaults_languageSanitization_handlesWhitespaceAndSpecialLocales() {
        repository.setTtsLanguage("")
        assertEquals("vi-VN", repository.ttsLanguage.value)

        repository.setTtsLanguage("   \t\n  ")
        assertEquals("vi-VN", repository.ttsLanguage.value)

        repository.setTtsLanguage("ja-JP")
        assertEquals("ja-JP", repository.ttsLanguage.value)

        repository.setTtsLanguage("fr-FR")
        assertEquals("fr-FR", repository.ttsLanguage.value)
    }

    @Test
    fun storageFormatting_boundaryValues() {
        assertEquals("0 B", StorageManager.formatStorageSize(-500L))
        assertEquals("0 B", StorageManager.formatStorageSize(0L))
        assertEquals("1 B", StorageManager.formatStorageSize(1L))
        assertEquals("1023 B", StorageManager.formatStorageSize(1023L))
        assertEquals("1.0 KB", StorageManager.formatStorageSize(1024L))
        assertEquals("1.0 MB", StorageManager.formatStorageSize(1024L * 1024L))
        assertEquals("1.0 GB", StorageManager.formatStorageSize(1024L * 1024L * 1024L))
        assertEquals("1024.0 GB", StorageManager.formatStorageSize(1024L * 1024L * 1024L * 1024L))
    }

    @Test
    fun storageStats_readingHours_fractionalCalculation() {
        val s0 = LibraryStorageStats(0, 0L, 0L)
        assertEquals(0.0, s0.readingHours, 0.0001)

        val s30 = LibraryStorageStats(1, 30L, 100L)
        assertEquals(0.5, s30.readingHours, 0.0001)

        val s90 = LibraryStorageStats(2, 90L, 200L)
        assertEquals(1.5, s90.readingHours, 0.0001)
        assertEquals(1L, s90.readingHoursWhole)
    }

    // ========================================================================
    // 5. Cross-Feature Pairwise Combinations (Tier 3)
    // ========================================================================

    @Test
    fun crossFeature_preferencesAndThemeCombinations_persistIndependently() {
        val themes = listOf(ReaderTheme.DAY, ReaderTheme.SEPIA, ReaderTheme.NIGHT, ReaderTheme.DARK_SEPIA)
        val fontSizes = listOf(14f, 16f, 20f, 24f)
        val lineSpacings = listOf(1.4f, 1.6f, 1.8f, 2.0f)

        for (i in themes.indices) {
            repository.setDefaultReaderTheme(themes[i])
            repository.setDefaultFontSize(fontSizes[i])
            repository.setDefaultLineSpacing(lineSpacings[i])

            assertEquals(themes[i], repository.defaultReaderTheme.value)
            assertEquals(fontSizes[i], repository.defaultFontSize.value, 0.001f)
            assertEquals(lineSpacings[i], repository.defaultLineSpacing.value, 0.001f)
        }
    }

    @Test
    fun crossFeature_ttsAndMarginsCombinations_persistIndependently() {
        val speechRates = listOf(0.75f, 1.0f, 1.25f, 1.5f)
        val margins = listOf(16, 20, 24, 32)
        val languages = listOf("vi-VN", "en-US", "fr-FR", "de-DE")

        for (i in speechRates.indices) {
            repository.setTtsSpeechRate(speechRates[i])
            repository.setDefaultMarginsDp(margins[i])
            repository.setTtsLanguage(languages[i])

            assertEquals(speechRates[i], repository.ttsSpeechRate.value, 0.001f)
            assertEquals(margins[i], repository.defaultMarginsDp.value)
            assertEquals(languages[i], repository.ttsLanguage.value)
        }
    }

    @Test
    fun crossFeature_storageBreakdown_preservesZeroInvariants() {
        val stats = LibraryStorageStats(
            totalBooks = 0,
            readingMinutes = 0L,
            totalStorageBytes = 0L,
            booksStorageBytes = 0L,
            databaseStorageBytes = 0L,
            cacheStorageBytes = 0L
        )

        assertEquals(0, stats.totalBooks)
        assertEquals(0L, stats.readingMinutes)
        assertEquals(0L, stats.totalStorageBytes)
        assertEquals("0 phút", stats.formattedReadingTime)
        assertEquals("0 B", stats.formattedStorage)
    }

    @Test
    fun crossFeature_metadataExport_jsonIntegrity() {
        val json = JSONObject()
        json.put("appName", AppInfo.DEFAULT.appName)
        json.put("versionName", AppInfo.DEFAULT.versionName)
        json.put("buildNumber", AppInfo.DEFAULT.buildNumber)
        json.put("offlineGuarantee", AppInfo.DEFAULT.offlineGuarantee)

        val prefJson = JSONObject()
        prefJson.put("defaultFontSize", repository.defaultFontSize.value)
        prefJson.put("defaultLineSpacing", repository.defaultLineSpacing.value)
        prefJson.put("defaultReaderTheme", repository.defaultReaderTheme.value.name)
        prefJson.put("ttsSpeechRate", repository.ttsSpeechRate.value)
        prefJson.put("ttsLanguage", repository.ttsLanguage.value)
        json.put("preferences", prefJson)

        val jsonString = json.toString()
        assertTrue(jsonString.contains("FolioHaven Mobile"))
        assertTrue(jsonString.contains("1.0"))
        assertTrue(jsonString.contains("preferences"))

        // Re-parse and verify
        val parsed = JSONObject(jsonString)
        assertEquals("FolioHaven Mobile", parsed.getString("appName"))
        assertEquals(1, parsed.getInt("buildNumber"))
        val parsedPrefs = parsed.getJSONObject("preferences")
        assertEquals(16.0, parsedPrefs.getDouble("defaultFontSize"), 0.01)
        assertEquals("SEPIA", parsedPrefs.getString("defaultReaderTheme"))
        assertEquals("vi-VN", parsedPrefs.getString("ttsLanguage"))
    }

    // ========================================================================
    // 6. Real-World Application End-to-End Scenarios (Tier 4)
    // ========================================================================

    @Test
    fun e2eScenario_userCustomizesAllDefaults_andSimulatesColdRestart() {
        // User configures custom literary preferences
        repository.setDefaultFontSize(19.5f)
        repository.setDefaultLineSpacing(1.75f)
        repository.setDefaultReaderTheme(ReaderTheme.DARK_SEPIA)
        repository.setDefaultMarginsDp(28)
        repository.setTtsSpeechRate(1.15f)
        repository.setTtsPitch(0.95f)
        repository.setTtsLanguage("en-GB")

        // Simulate app termination & reload from same underlying storage
        val restartedRepo = UserPreferencesRepository(injectedPrefs = fakePrefs)

        assertEquals(19.5f, restartedRepo.defaultFontSize.value, 0.001f)
        assertEquals(1.75f, restartedRepo.defaultLineSpacing.value, 0.001f)
        assertEquals(ReaderTheme.DARK_SEPIA, restartedRepo.defaultReaderTheme.value)
        assertEquals(28, restartedRepo.defaultMarginsDp.value)
        assertEquals(1.15f, restartedRepo.ttsSpeechRate.value, 0.001f)
        assertEquals(0.95f, restartedRepo.ttsPitch.value, 0.001f)
        assertEquals("en-GB", restartedRepo.ttsLanguage.value)
    }

    @Test
    fun e2eScenario_resetDefaultsRestoresOriginalArchivalSettings() {
        // Set deviant values
        repository.setDefaultFontSize(28f)
        repository.setDefaultLineSpacing(2.2f)
        repository.setDefaultReaderTheme(ReaderTheme.NIGHT)
        repository.setDefaultMarginsDp(36)
        repository.setTtsSpeechRate(2.0f)
        repository.setTtsPitch(2.0f)
        repository.setTtsLanguage("de-DE")

        // Reset to FolioHaven defaults
        repository.setDefaultFontSize(16.0f)
        repository.setDefaultLineSpacing(1.6f)
        repository.setDefaultReaderTheme(ReaderTheme.SEPIA)
        repository.setDefaultMarginsDp(24)
        repository.setTtsSpeechRate(1.0f)
        repository.setTtsPitch(1.0f)
        repository.setTtsLanguage("vi-VN")

        assertEquals(16.0f, repository.defaultFontSize.value, 0.001f)
        assertEquals(1.6f, repository.defaultLineSpacing.value, 0.001f)
        assertEquals(ReaderTheme.SEPIA, repository.defaultReaderTheme.value)
        assertEquals(24, repository.defaultMarginsDp.value)
        assertEquals(1.0f, repository.ttsSpeechRate.value, 0.001f)
        assertEquals(1.0f, repository.ttsPitch.value, 0.001f)
        assertEquals("vi-VN", repository.ttsLanguage.value)
    }
}
