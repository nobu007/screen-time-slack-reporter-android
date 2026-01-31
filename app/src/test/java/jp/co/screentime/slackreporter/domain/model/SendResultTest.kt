package jp.co.screentime.slackreporter.domain.model

import org.junit.Assert.*
import org.junit.Test

class SendResultTest {

    @Test
    fun `SendStatus enum contains expected values`() {
        val values = SendStatus.values()
        
        assertEquals(3, values.size)
        assertTrue(values.contains(SendStatus.NOT_SENT))
        assertTrue(values.contains(SendStatus.SUCCESS))
        assertTrue(values.contains(SendStatus.FAILED))
    }

    @Test
    fun `SendResult with SUCCESS status has no error message`() {
        val result = SendResult(
            status = SendStatus.SUCCESS,
            lastSentEpochMillis = System.currentTimeMillis()
        )
        
        assertEquals(SendStatus.SUCCESS, result.status)
        assertNull(result.errorMessage)
        assertNotNull(result.lastSentEpochMillis)
    }

    @Test
    fun `SendResult with FAILED status has error message`() {
        val errorMsg = "Network error"
        val result = SendResult(
            status = SendStatus.FAILED,
            errorMessage = errorMsg
        )
        
        assertEquals(SendStatus.FAILED, result.status)
        assertEquals(errorMsg, result.errorMessage)
        assertNull(result.lastSentEpochMillis)
    }

    @Test
    fun `SendResult with NOT_SENT status has null timestamps`() {
        val result = SendResult(status = SendStatus.NOT_SENT)
        
        assertEquals(SendStatus.NOT_SENT, result.status)
        assertNull(result.lastSentEpochMillis)
        assertNull(result.errorMessage)
    }

    @Test
    fun `SendResult default values are null`() {
        val result = SendResult(status = SendStatus.SUCCESS)
        
        assertNull(result.lastSentEpochMillis)
        assertNull(result.errorMessage)
    }

    @Test
    fun `SendResult equality works correctly`() {
        val timestamp = 1234567890L
        val result1 = SendResult(SendStatus.SUCCESS, timestamp, null)
        val result2 = SendResult(SendStatus.SUCCESS, timestamp, null)
        val result3 = SendResult(SendStatus.FAILED, timestamp, "error")
        
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }

    @Test
    fun `SendResult copy works correctly`() {
        val original = SendResult(
            status = SendStatus.NOT_SENT,
            lastSentEpochMillis = null
        )
        val copied = original.copy(
            status = SendStatus.SUCCESS,
            lastSentEpochMillis = 1234567890L
        )
        
        assertEquals(SendStatus.SUCCESS, copied.status)
        assertEquals(1234567890L, copied.lastSentEpochMillis)
    }
}
