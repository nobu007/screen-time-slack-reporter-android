package jp.co.screentime.slackreporter.domain.model

import org.junit.Assert.*
import org.junit.Test

class AppUsageTest {

    @Test
    fun `durationMinutes calculates correctly for 1 hour`() {
        val usage = AppUsage("com.example", 3600000L) // 1 hour in millis
        
        assertEquals(60, usage.durationMinutes)
    }

    @Test
    fun `durationMinutes calculates correctly for 30 minutes`() {
        val usage = AppUsage("com.example", 1800000L) // 30 minutes in millis
        
        assertEquals(30, usage.durationMinutes)
    }

    @Test
    fun `durationMinutes returns 0 for zero duration`() {
        val usage = AppUsage("com.example", 0L)
        
        assertEquals(0, usage.durationMinutes)
    }

    @Test
    fun `durationMinutes rounds down for partial minutes`() {
        val usage = AppUsage("com.example", 90000L) // 1.5 minutes in millis
        
        assertEquals(1, usage.durationMinutes)
    }

    @Test
    fun `hasUsage returns true for positive duration`() {
        val usage = AppUsage("com.example", 1000L)
        
        assertTrue(usage.hasUsage)
    }

    @Test
    fun `hasUsage returns false for zero duration`() {
        val usage = AppUsage("com.example", 0L)
        
        assertFalse(usage.hasUsage)
    }

    @Test
    fun `data class equality works correctly`() {
        val usage1 = AppUsage("com.example", 1000L)
        val usage2 = AppUsage("com.example", 1000L)
        val usage3 = AppUsage("com.different", 1000L)
        
        assertEquals(usage1, usage2)
        assertNotEquals(usage1, usage3)
    }

    @Test
    fun `packageName is stored correctly`() {
        val packageName = "jp.co.screentime.slackreporter"
        val usage = AppUsage(packageName, 5000L)
        
        assertEquals(packageName, usage.packageName)
    }

    @Test
    fun `durationMillis is stored correctly`() {
        val duration = 123456789L
        val usage = AppUsage("com.example", duration)
        
        assertEquals(duration, usage.durationMillis)
    }
}
