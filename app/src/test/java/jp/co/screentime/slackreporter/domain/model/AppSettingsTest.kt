package jp.co.screentime.slackreporter.domain.model

import org.junit.Assert.*
import org.junit.Test

class AppSettingsTest {

    @Test
    fun `isWebhookConfigured returns true when webhookUrl is not blank`() {
        val settings = AppSettings(
            webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX"
        )
        
        assertTrue(settings.isWebhookConfigured)
    }

    @Test
    fun `isWebhookConfigured returns false when webhookUrl is empty`() {
        val settings = AppSettings(webhookUrl = "")
        
        assertFalse(settings.isWebhookConfigured)
    }

    @Test
    fun `isWebhookConfigured returns false when webhookUrl is blank`() {
        val settings = AppSettings(webhookUrl = "   ")
        
        assertFalse(settings.isWebhookConfigured)
    }

    @Test
    fun `canSend returns true when webhook configured and send enabled`() {
        val settings = AppSettings(
            webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX",
            sendEnabled = true
        )
        
        assertTrue(settings.canSend)
    }

    @Test
    fun `canSend returns false when webhook not configured`() {
        val settings = AppSettings(
            webhookUrl = "",
            sendEnabled = true
        )
        
        assertFalse(settings.canSend)
    }

    @Test
    fun `canSend returns false when send not enabled`() {
        val settings = AppSettings(
            webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX",
            sendEnabled = false
        )
        
        assertFalse(settings.canSend)
    }

    @Test
    fun `default values are correct`() {
        val settings = AppSettings()
        
        assertEquals("", settings.webhookUrl)
        assertFalse(settings.sendEnabled)
        assertEquals(21, settings.sendHour)
        assertEquals(0, settings.sendMinute)
        assertTrue(settings.excludedPackages.isEmpty())
    }

    @Test
    fun `excludedPackages is stored correctly`() {
        val packages = setOf("com.app1", "com.app2", "com.app3")
        val settings = AppSettings(excludedPackages = packages)
        
        assertEquals(3, settings.excludedPackages.size)
        assertTrue(settings.excludedPackages.contains("com.app1"))
        assertTrue(settings.excludedPackages.contains("com.app2"))
        assertTrue(settings.excludedPackages.contains("com.app3"))
    }

    @Test
    fun `sendHour and sendMinute are stored correctly`() {
        val settings = AppSettings(sendHour = 18, sendMinute = 30)
        
        assertEquals(18, settings.sendHour)
        assertEquals(30, settings.sendMinute)
    }

    @Test
    fun `data class equality works correctly`() {
        val settings1 = AppSettings(webhookUrl = "url", sendEnabled = true)
        val settings2 = AppSettings(webhookUrl = "url", sendEnabled = true)
        val settings3 = AppSettings(webhookUrl = "different", sendEnabled = true)
        
        assertEquals(settings1, settings2)
        assertNotEquals(settings1, settings3)
    }

    @Test
    fun `copy works correctly`() {
        val original = AppSettings(
            webhookUrl = "https://hooks.slack.com/services/T00/B00/XXX",
            sendEnabled = false,
            sendHour = 21,
            sendMinute = 0
        )
        val copied = original.copy(sendEnabled = true, sendHour = 18)
        
        assertEquals("https://hooks.slack.com/services/T00/B00/XXX", copied.webhookUrl)
        assertTrue(copied.sendEnabled)
        assertEquals(18, copied.sendHour)
        assertEquals(0, copied.sendMinute)
    }
}
