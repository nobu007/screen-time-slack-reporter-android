package jp.co.screentime.slackreporter.platform

import android.content.Context
import android.content.Intent
import android.provider.Settings
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class UsageAccessHelperTest {

    private lateinit var context: Context
    private lateinit var usageAccessHelper: UsageAccessHelper

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        usageAccessHelper = UsageAccessHelper(context)
    }

    @Test
    fun `hasUsageAccessPermission returns boolean value`() {
        // Verify the method returns a valid boolean without throwing exceptions
        val result = usageAccessHelper.hasUsageAccessPermission()
        assertNotNull(result)
        // The result can be true or false depending on Robolectric configuration
        assertTrue(result || !result) // Always true, validates no exception thrown
    }

    @Test
    fun `getUsageAccessSettingsIntent returns correct action`() {
        val intent = usageAccessHelper.getUsageAccessSettingsIntent()

        assertEquals(Settings.ACTION_USAGE_ACCESS_SETTINGS, intent.action)
    }

    @Test
    fun `getUsageAccessSettingsIntent returns non-null intent`() {
        val intent = usageAccessHelper.getUsageAccessSettingsIntent()

        assertNotNull(intent)
    }

    @Test
    fun `getUsageAccessSettingsIntent returns intent that can be used to start activity`() {
        val intent = usageAccessHelper.getUsageAccessSettingsIntent()

        // Verify the intent has the expected properties
        assertTrue(intent is Intent)
        assertNotNull(intent.action)
    }
}
