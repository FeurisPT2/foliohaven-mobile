package com.example.foliohaven.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppInfoTest {

    @Test
    fun appInfo_hasCorrectRequiredConstants() {
        val appInfo = AppInfo.DEFAULT
        assertEquals("FolioHaven Mobile", appInfo.appName)
        assertEquals("1.0", appInfo.versionName)
        assertEquals(1, appInfo.buildNumber)
        assertEquals("MIT License", appInfo.license)
        assertTrue(appInfo.licenseUrl.contains("opensource.org"))
        assertTrue(appInfo.repositoryUrl.contains("github.com"))
        assertNotNull(appInfo.tagline)
        assertNotNull(appInfo.taglineEn)
    }

    @Test
    fun offlineGuarantee_containsRequiredPrivacyInvariants() {
        val guaranteeVi = AppInfo.OFFLINE_GUARANTEE
        assertTrue("Must guarantee 100% Offline", guaranteeVi.contains("100% Offline"))
        assertTrue("Must specify AndroidManifest.xml", guaranteeVi.contains("AndroidManifest.xml"))
        assertTrue(
            "Must guarantee 0 network permissions",
            guaranteeVi.contains("0 network permissions") || guaranteeVi.contains("quyền truy cập Internet")
        )
        assertTrue("Must state no telemetry", guaranteeVi.contains("không gửi telemetry") || guaranteeVi.contains("telemetry"))

        val guaranteeEn = AppInfo.OFFLINE_GUARANTEE_EN
        assertTrue("Must guarantee zero internet permissions", guaranteeEn.contains("zero Internet permissions"))
        assertTrue("Must guarantee zero telemetry", guaranteeEn.contains("zero telemetry"))
        assertTrue("Must state privacy first", guaranteeEn.contains("Privacy-First"))
    }

    @Test
    fun credits_coverAllCoreArchitecturalLayers() {
        val credits = AppInfo.DEFAULT_CREDITS
        assertTrue("Credits must contain at least 4 key components", credits.size >= 4)

        val names = credits.map { it.name }
        assertTrue("Credits must include Jetpack Compose", names.any { it.contains("Compose") })
        assertTrue("Credits must include reader engines", names.any { it.contains("Readium") || it.contains("PdfRenderer") })
        assertTrue("Credits must include design system / canvas", names.any { it.contains("Design System") || it.contains("Canvas") })
        assertTrue("Credits must include SQLite storage", names.any { it.contains("SQLite") })
        assertTrue("Credits must include TTS", names.any { it.contains("TTS") || it.contains("Text-to-Speech") })
    }

    @Test
    fun creditItem_instantiatesCorrectly() {
        val item = CreditItem("UI", "Compose", "Modern toolkit")
        assertEquals("UI", item.category)
        assertEquals("Compose", item.name)
        assertEquals("Modern toolkit", item.description)
    }
}
