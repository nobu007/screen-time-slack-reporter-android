package jp.co.screentime.slackreporter.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.junit.Assert.*
import org.junit.Test

class PreferencesDataStoreTest {

    @Test
    fun `PreferencesKeys - SLACK_WEBHOOK_URL key exists`() {
        assertNotNull(PreferencesKeys.SLACK_WEBHOOK_URL)
        assertEquals("slack_webhook_url", PreferencesKeys.SLACK_WEBHOOK_URL.name)
    }

    @Test
    fun `PreferencesKeys - SEND_ENABLED key exists`() {
        assertNotNull(PreferencesKeys.SEND_ENABLED)
        assertEquals("send_enabled", PreferencesKeys.SEND_ENABLED.name)
    }

    @Test
    fun `PreferencesKeys - SEND_HOUR key exists`() {
        assertNotNull(PreferencesKeys.SEND_HOUR)
        assertEquals("send_hour", PreferencesKeys.SEND_HOUR.name)
    }

    @Test
    fun `PreferencesKeys - SEND_MINUTE key exists`() {
        assertNotNull(PreferencesKeys.SEND_MINUTE)
        assertEquals("send_minute", PreferencesKeys.SEND_MINUTE.name)
    }

    @Test
    fun `PreferencesKeys - EXCLUDED_PACKAGES key exists`() {
        assertNotNull(PreferencesKeys.EXCLUDED_PACKAGES)
        assertEquals("excluded_packages", PreferencesKeys.EXCLUDED_PACKAGES.name)
    }

    @Test
    fun `PreferencesKeys - LAST_SEND_STATUS key exists`() {
        assertNotNull(PreferencesKeys.LAST_SEND_STATUS)
        assertEquals("last_send_status", PreferencesKeys.LAST_SEND_STATUS.name)
    }

    @Test
    fun `PreferencesKeys - LAST_SEND_EPOCH_MILLIS key exists`() {
        assertNotNull(PreferencesKeys.LAST_SEND_EPOCH_MILLIS)
        assertEquals("last_send_epoch_millis", PreferencesKeys.LAST_SEND_EPOCH_MILLIS.name)
    }

    @Test
    fun `PreferencesKeys - LAST_SEND_ERROR key exists`() {
        assertNotNull(PreferencesKeys.LAST_SEND_ERROR)
        assertEquals("last_send_error", PreferencesKeys.LAST_SEND_ERROR.name)
    }

    @Test
    fun `PreferencesKeys - SHOW_EXCLUDED_ONLY key exists`() {
        assertNotNull(PreferencesKeys.SHOW_EXCLUDED_ONLY)
        assertEquals("show_excluded_only", PreferencesKeys.SHOW_EXCLUDED_ONLY.name)
    }

    @Test
    fun `PreferencesKeys - SLACK_WEBHOOK_URL is stringPreferencesKey`() {
        val expected = stringPreferencesKey("slack_webhook_url")
        assertEquals(expected, PreferencesKeys.SLACK_WEBHOOK_URL)
    }

    @Test
    fun `PreferencesKeys - SEND_ENABLED is booleanPreferencesKey`() {
        val expected = booleanPreferencesKey("send_enabled")
        assertEquals(expected, PreferencesKeys.SEND_ENABLED)
    }

    @Test
    fun `PreferencesKeys - SEND_HOUR is intPreferencesKey`() {
        val expected = intPreferencesKey("send_hour")
        assertEquals(expected, PreferencesKeys.SEND_HOUR)
    }

    @Test
    fun `PreferencesKeys - SEND_MINUTE is intPreferencesKey`() {
        val expected = intPreferencesKey("send_minute")
        assertEquals(expected, PreferencesKeys.SEND_MINUTE)
    }

    @Test
    fun `PreferencesKeys - EXCLUDED_PACKAGES is stringSetPreferencesKey`() {
        val expected = stringSetPreferencesKey("excluded_packages")
        assertEquals(expected, PreferencesKeys.EXCLUDED_PACKAGES)
    }

    @Test
    fun `PreferencesKeys - LAST_SEND_EPOCH_MILLIS is longPreferencesKey`() {
        val expected = longPreferencesKey("last_send_epoch_millis")
        assertEquals(expected, PreferencesKeys.LAST_SEND_EPOCH_MILLIS)
    }

    @Test
    fun `PreferencesKeys - LAST_SEND_STATUS is stringPreferencesKey`() {
        val expected = stringPreferencesKey("last_send_status")
        assertEquals(expected, PreferencesKeys.LAST_SEND_STATUS)
    }

    @Test
    fun `PreferencesKeys - LAST_SEND_ERROR is stringPreferencesKey`() {
        val expected = stringPreferencesKey("last_send_error")
        assertEquals(expected, PreferencesKeys.LAST_SEND_ERROR)
    }

    @Test
    fun `PreferencesKeys - SHOW_EXCLUDED_ONLY is booleanPreferencesKey`() {
        val expected = booleanPreferencesKey("show_excluded_only")
        assertEquals(expected, PreferencesKeys.SHOW_EXCLUDED_ONLY)
    }

    @Test
    fun `PreferencesKeys - all keys are unique`() {
        val keys = listOf(
            PreferencesKeys.SLACK_WEBHOOK_URL.name,
            PreferencesKeys.SEND_ENABLED.name,
            PreferencesKeys.SEND_HOUR.name,
            PreferencesKeys.SEND_MINUTE.name,
            PreferencesKeys.EXCLUDED_PACKAGES.name,
            PreferencesKeys.LAST_SEND_EPOCH_MILLIS.name,
            PreferencesKeys.LAST_SEND_STATUS.name,
            PreferencesKeys.LAST_SEND_ERROR.name,
            PreferencesKeys.SHOW_EXCLUDED_ONLY.name
        )
        assertEquals(keys.size, keys.toSet().size)
    }
}
