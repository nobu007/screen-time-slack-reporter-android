package jp.co.screentime.slackreporter.domain.usecase

import io.mockk.*
import jp.co.screentime.slackreporter.data.repository.AppListRepository
import jp.co.screentime.slackreporter.domain.model.App
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetAllAppsUseCaseTest {

    private lateinit var appListRepository: AppListRepository
    private lateinit var useCase: GetAllAppsUseCase

    @Before
    fun setup() {
        appListRepository = mockk()
        useCase = GetAllAppsUseCase(appListRepository)
    }

    @Test
    fun `アプリ一覧がリポジトリから取得される`() = runTest {
        val mockApps = listOf(
            App("com.youtube.android", "YouTube"),
            App("com.chrome.android", "Chrome"),
            App("com.twitter.android", "Twitter")
        )
        coEvery { appListRepository.getAllApps() } returns mockApps

        val result = useCase()

        assertEquals(3, result.size)
        assertEquals("com.youtube.android", result[0].packageName)
        assertEquals("YouTube", result[0].appName)
    }

    @Test
    fun `空のリストが正しく返される`() = runTest {
        coEvery { appListRepository.getAllApps() } returns emptyList()

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `リポジトリが1回だけ呼ばれる`() = runTest {
        coEvery { appListRepository.getAllApps() } returns emptyList()

        useCase()

        coVerify(exactly = 1) { appListRepository.getAllApps() }
    }

    @Test
    fun `リポジトリの例外がそのまま伝播する`() = runTest {
        coEvery { appListRepository.getAllApps() } throws RuntimeException("Test error")

        val exception = assertThrows(RuntimeException::class.java) {
            kotlinx.coroutines.runBlocking { useCase() }
        }

        assertEquals("Test error", exception.message)
    }
}
