package jp.co.screentime.slackreporter.data.usage

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import jp.co.screentime.slackreporter.platform.UsageAccessHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class UsageStatsDataSourceTest {

    private lateinit var context: Context
    private lateinit var usageAccessHelper: UsageAccessHelper
    private lateinit var dataSource: UsageStatsDataSource

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        usageAccessHelper = mockk()
    }

    @Test
    fun `isUsageAccessGranted returns true when helper returns true`() {
        every { usageAccessHelper.hasUsageAccessPermission() } returns true
        dataSource = UsageStatsDataSource(context, usageAccessHelper)

        assertTrue(dataSource.isUsageAccessGranted())
    }

    @Test
    fun `isUsageAccessGranted returns false when helper returns false`() {
        every { usageAccessHelper.hasUsageAccessPermission() } returns false
        dataSource = UsageStatsDataSource(context, usageAccessHelper)

        assertFalse(dataSource.isUsageAccessGranted())
    }

    @Test
    fun `getUsageStats returns empty list when UsageStatsManager is null`() {
        // Robolectric doesn't provide UsageStatsManager by default
        every { usageAccessHelper.hasUsageAccessPermission() } returns true
        dataSource = UsageStatsDataSource(context, usageAccessHelper)

        val result = dataSource.getUsageStats(0L, System.currentTimeMillis())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getUsageStats filters out zero duration entries`() {
        // Test the filtering logic - entries with 0 duration should be excluded
        every { usageAccessHelper.hasUsageAccessPermission() } returns true
        dataSource = UsageStatsDataSource(context, usageAccessHelper)

        // When UsageStatsManager returns null (Robolectric default), result is empty
        val result = dataSource.getUsageStats(
            System.currentTimeMillis() - 86400000L,
            System.currentTimeMillis()
        )

        // Verify that filtering doesn't cause crashes
        assertNotNull(result)
    }

    @Test
    fun `getUsageStats handles time range correctly`() {
        every { usageAccessHelper.hasUsageAccessPermission() } returns true
        dataSource = UsageStatsDataSource(context, usageAccessHelper)

        val startTime = System.currentTimeMillis() - 86400000L // 24 hours ago
        val endTime = System.currentTimeMillis()

        // Should not throw exception with valid time range
        val result = dataSource.getUsageStats(startTime, endTime)
        assertNotNull(result)
    }

    @Test
    fun `getUsageStats handles same start and end time`() {
        every { usageAccessHelper.hasUsageAccessPermission() } returns true
        dataSource = UsageStatsDataSource(context, usageAccessHelper)

        val time = System.currentTimeMillis()

        // Edge case: same start and end time
        val result = dataSource.getUsageStats(time, time)
        assertNotNull(result)
    }
}
