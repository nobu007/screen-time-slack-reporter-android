package jp.co.screentime.slackreporter.presentation.model

import org.junit.Assert.*
import org.junit.Test

class UiAppUsageTest {

    @Test
    fun `UiAppUsage - 基本的なインスタンス生成`() {
        val usage = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        
        assertEquals("com.example.app", usage.packageName)
        assertEquals("Example App", usage.appName)
        assertNull(usage.icon)
        assertEquals(30, usage.durationMinutes)
        assertFalse(usage.isExcluded)
    }

    @Test
    fun `UiAppUsage - isExcludedがtrueの場合`() {
        val usage = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 0,
            isExcluded = true
        )
        
        assertTrue(usage.isExcluded)
    }

    @Test
    fun `UiAppUsage - durationMinutesが0の場合`() {
        val usage = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 0,
            isExcluded = false
        )
        
        assertEquals(0, usage.durationMinutes)
    }

    @Test
    fun `UiAppUsage - 大きなdurationMinutes値`() {
        val usage = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 1440, // 24時間
            isExcluded = false
        )
        
        assertEquals(1440, usage.durationMinutes)
    }

    @Test
    fun `UiAppUsage - copyでisExcludedを変更`() {
        val original = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        val copied = original.copy(isExcluded = true)
        
        assertFalse(original.isExcluded)
        assertTrue(copied.isExcluded)
    }

    @Test
    fun `UiAppUsage - copyでdurationMinutesを変更`() {
        val original = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        val copied = original.copy(durationMinutes = 60)
        
        assertEquals(30, original.durationMinutes)
        assertEquals(60, copied.durationMinutes)
    }

    @Test
    fun `UiAppUsage - 等価性テスト`() {
        val usage1 = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        val usage2 = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        
        assertEquals(usage1, usage2)
    }

    @Test
    fun `UiAppUsage - 非等価性テスト - packageNameが異なる`() {
        val usage1 = UiAppUsage(
            packageName = "com.example.app1",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        val usage2 = UiAppUsage(
            packageName = "com.example.app2",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        
        assertNotEquals(usage1, usage2)
    }

    @Test
    fun `UiAppUsage - 非等価性テスト - isExcludedが異なる`() {
        val usage1 = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        val usage2 = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = true
        )
        
        assertNotEquals(usage1, usage2)
    }

    @Test
    fun `UiAppUsage - hashCodeの一貫性`() {
        val usage1 = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        val usage2 = UiAppUsage(
            packageName = "com.example.app",
            appName = "Example App",
            icon = null,
            durationMinutes = 30,
            isExcluded = false
        )
        
        assertEquals(usage1.hashCode(), usage2.hashCode())
    }
}
