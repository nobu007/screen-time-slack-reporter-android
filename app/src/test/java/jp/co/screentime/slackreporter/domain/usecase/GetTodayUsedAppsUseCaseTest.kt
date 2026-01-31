package jp.co.screentime.slackreporter.domain.usecase

import io.mockk.*
import jp.co.screentime.slackreporter.domain.model.AppUsage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetTodayUsedAppsUseCaseTest {

    private lateinit var getTodayUsageUseCase: GetTodayUsageUseCase
    private lateinit var useCase: GetTodayUsedAppsUseCase

    @Before
    fun setup() {
        getTodayUsageUseCase = mockk()
        useCase = GetTodayUsedAppsUseCase(getTodayUsageUseCase)
    }

    @Test
    fun `GetTodayUsageUseCaseに委譲される`() = runTest {
        val mockUsage = listOf(
            AppUsage("com.youtube.android", 1800000L),
            AppUsage("com.chrome.android", 900000L)
        )
        coEvery { getTodayUsageUseCase() } returns mockUsage

        val result = useCase()

        assertEquals(2, result.size)
        assertEquals("com.youtube.android", result[0].packageName)
        assertEquals(1800000L, result[0].durationMillis)
    }

    @Test
    fun `空のリストが正しく返される`() = runTest {
        coEvery { getTodayUsageUseCase() } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `GetTodayUsageUseCaseが1回だけ呼ばれる`() = runTest {
        coEvery { getTodayUsageUseCase() } returns emptyList()

        useCase()

        coVerify(exactly = 1) { getTodayUsageUseCase() }
    }

    @Test
    fun `利用時間降順でソートされたリストがそのまま返される`() = runTest {
        val mockUsage = listOf(
            AppUsage("com.app1", 3600000L),  // 60分
            AppUsage("com.app2", 1800000L),  // 30分
            AppUsage("com.app3", 600000L)    // 10分
        )
        coEvery { getTodayUsageUseCase() } returns mockUsage

        val result = useCase()

        assertEquals("com.app1", result[0].packageName)
        assertEquals("com.app2", result[1].packageName)
        assertEquals("com.app3", result[2].packageName)
    }

    @Test
    fun `例外がそのまま伝播する`() = runTest {
        coEvery { getTodayUsageUseCase() } throws RuntimeException("Test error")

        val exception = assertThrows(RuntimeException::class.java) {
            kotlinx.coroutines.runBlocking { useCase() }
        }

        assertEquals("Test error", exception.message)
    }
}
