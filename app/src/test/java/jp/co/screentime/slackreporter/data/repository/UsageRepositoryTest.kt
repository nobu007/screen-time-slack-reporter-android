package jp.co.screentime.slackreporter.data.repository

import io.mockk.every
import io.mockk.mockk
import jp.co.screentime.slackreporter.data.usage.UsageStatsDataSource
import jp.co.screentime.slackreporter.domain.model.AppUsage
import org.junit.Assert.*
import org.junit.Test

class UsageRepositoryTest {

    @Test
    fun `getUsage returns data from data source`() {
        val dataSource = mockk<UsageStatsDataSource>()
        val mockUsage = listOf(
            AppUsage("com.youtube", 3600000L),
            AppUsage("com.chrome", 1800000L)
        )
        every { dataSource.getUsageStats(any(), any()) } returns mockUsage

        val repository = UsageRepository(dataSource)
        val result = repository.getUsage(0L, System.currentTimeMillis())

        assertEquals(2, result.size)
        assertEquals("com.youtube", result[0].packageName)
        assertEquals(3600000L, result[0].durationMillis)
    }

    @Test
    fun `getUsage returns empty list when no data`() {
        val dataSource = mockk<UsageStatsDataSource>()
        every { dataSource.getUsageStats(any(), any()) } returns emptyList()

        val repository = UsageRepository(dataSource)
        val result = repository.getUsage(0L, System.currentTimeMillis())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `isUsageAccessGranted returns value from data source`() {
        val dataSource = mockk<UsageStatsDataSource>()
        every { dataSource.isUsageAccessGranted() } returns true

        val repository = UsageRepository(dataSource)

        assertTrue(repository.isUsageAccessGranted())
    }

    @Test
    fun `isUsageAccessGranted returns false when not granted`() {
        val dataSource = mockk<UsageStatsDataSource>()
        every { dataSource.isUsageAccessGranted() } returns false

        val repository = UsageRepository(dataSource)

        assertFalse(repository.isUsageAccessGranted())
    }
}
