package jp.co.screentime.slackreporter.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import jp.co.screentime.slackreporter.data.repository.UsageRepository
import jp.co.screentime.slackreporter.domain.model.AppUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetTodayUsageUseCaseTest {

    private lateinit var usageRepository: UsageRepository
    private lateinit var useCase: GetTodayUsageUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        usageRepository = mockk()
        useCase = GetTodayUsageUseCase(usageRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `returns usage data from repository sorted by duration descending`() = runTest {
        val mockUsage = listOf(
            AppUsage("com.chrome", 1800000L),    // 30 min
            AppUsage("com.youtube", 3600000L),   // 60 min
            AppUsage("com.twitter", 900000L)     // 15 min
        )
        coEvery { usageRepository.getUsage(any(), any()) } returns mockUsage

        val result = useCase()

        assertEquals(3, result.size)
        assertEquals("com.youtube", result[0].packageName)  // 60 min first
        assertEquals("com.chrome", result[1].packageName)   // 30 min second
        assertEquals("com.twitter", result[2].packageName)  // 15 min last
    }

    @Test
    fun `filters out zero duration apps`() = runTest {
        val mockUsage = listOf(
            AppUsage("com.youtube", 3600000L),
            AppUsage("com.system", 0L),
            AppUsage("com.chrome", 1800000L)
        )
        coEvery { usageRepository.getUsage(any(), any()) } returns mockUsage

        val result = useCase()

        assertEquals(2, result.size)
        assertFalse(result.any { it.durationMillis == 0L })
    }

    @Test
    fun `returns empty list when no usage data`() = runTest {
        coEvery { usageRepository.getUsage(any(), any()) } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list when all apps have zero duration`() = runTest {
        val mockUsage = listOf(
            AppUsage("com.app1", 0L),
            AppUsage("com.app2", 0L)
        )
        coEvery { usageRepository.getUsage(any(), any()) } returns mockUsage

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `handles single app usage correctly`() = runTest {
        val mockUsage = listOf(AppUsage("com.youtube", 3600000L))
        coEvery { usageRepository.getUsage(any(), any()) } returns mockUsage

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals("com.youtube", result[0].packageName)
        assertEquals(3600000L, result[0].durationMillis)
    }

    @Test
    fun `preserves app usage details correctly`() = runTest {
        val expectedPackage = "jp.co.screentime.slackreporter"
        val expectedDuration = 7200000L // 2 hours
        val mockUsage = listOf(AppUsage(expectedPackage, expectedDuration))
        coEvery { usageRepository.getUsage(any(), any()) } returns mockUsage

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals(expectedPackage, result[0].packageName)
        assertEquals(expectedDuration, result[0].durationMillis)
    }
}
