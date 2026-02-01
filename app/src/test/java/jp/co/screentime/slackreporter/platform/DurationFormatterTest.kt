package jp.co.screentime.slackreporter.platform

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class DurationFormatterTest {

    @Test
    fun `minutesFromMillis - 0ミリ秒は0分`() {
        val result = DurationFormatter.minutesFromMillis(0)
        assertEquals(0, result)
    }

    @Test
    fun `minutesFromMillis - 59秒は0分`() {
        val millis = TimeUnit.SECONDS.toMillis(59)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(0, result)
    }

    @Test
    fun `minutesFromMillis - 60秒は1分`() {
        val millis = TimeUnit.SECONDS.toMillis(60)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(1, result)
    }

    @Test
    fun `minutesFromMillis - 90秒は1分`() {
        val millis = TimeUnit.SECONDS.toMillis(90)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(1, result)
    }

    @Test
    fun `minutesFromMillis - 1時間は60分`() {
        val millis = TimeUnit.HOURS.toMillis(1)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(60, result)
    }

    @Test
    fun `minutesFromMillis - 1時間30分は90分`() {
        val millis = TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(90, result)
    }

    @Test
    fun `minutesFromMillis - 24時間は1440分`() {
        val millis = TimeUnit.HOURS.toMillis(24)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(1440, result)
    }

    @Test
    fun `minutesFromMillis - 負の値は負の分`() {
        val millis = TimeUnit.MINUTES.toMillis(-5)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(-5, result)
    }

    @Test
    fun `minutesFromMillis - 大きな値も正しく変換`() {
        val millis = TimeUnit.DAYS.toMillis(7) // 1週間
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(7 * 24 * 60, result)
    }

    @Test
    fun `minutesFromMillis - 1分未満の端数は切り捨て`() {
        val millis = TimeUnit.MINUTES.toMillis(5) + TimeUnit.SECONDS.toMillis(30)
        val result = DurationFormatter.minutesFromMillis(millis)
        assertEquals(5, result)
    }
}
