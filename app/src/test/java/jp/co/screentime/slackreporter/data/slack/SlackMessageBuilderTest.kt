package jp.co.screentime.slackreporter.data.slack

import android.content.Context
import android.content.res.Resources
import io.mockk.*
import jp.co.screentime.slackreporter.R
import jp.co.screentime.slackreporter.domain.model.AppUsage
import jp.co.screentime.slackreporter.platform.AppLabelResolver
import jp.co.screentime.slackreporter.platform.DurationFormatter
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SlackMessageBuilderTest {

    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var appLabelResolver: AppLabelResolver
    private lateinit var builder: SlackMessageBuilder

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        resources = mockk(relaxed = true)
        appLabelResolver = mockk(relaxed = true)
        
        every { context.resources } returns resources
        
        mockkObject(DurationFormatter)
        
        every { context.getString(R.string.slack_message_header, *anyVararg()) } returns "Header"
        every { context.getString(R.string.slack_message_no_usage) } returns "No Usage"
        every { context.getString(R.string.slack_message_app_line, *anyVararg()) } answers { 
            val args = secondArg<Array<Any?>>()
            val appName = args[0] as String
            val duration = args[1] as String
            "$appName: $duration"
        }
        every { context.getString(R.string.slack_message_other, *anyVararg()) } returns "Other"
        every { resources.getQuantityString(any(), any(), any()) } returns "mins"
        
        every { appLabelResolver.getAppLabel(any()) } answers { firstArg<String>() }
        
        builder = SlackMessageBuilder(context, appLabelResolver)
    }

    @After
    fun tearDown() {
        unmockkObject(DurationFormatter)
    }

    @Test
    fun `通常のUsageメッセージを生成できる`() {
        every { DurationFormatter.minutesFromMillis(any()) } returns 30
        every { DurationFormatter.formatMinutes(any(), any()) } returns "30m"

        val usageList = listOf(
            AppUsage("YouTube", 2700000L),
            AppUsage("Chrome", 1800000L),
            AppUsage("LINE", 900000L)
        )
        
        val message = builder.build(usageList)
        
        assertNotNull(message)
        assertTrue(message.contains("YouTube"))
        assertTrue(message.contains("Chrome"))
        assertTrue(message.contains("LINE"))
    }

    @Test
    fun `空のUsageリストでもメッセージを生成できる`() {
        val message = builder.build(emptyList())
        
        assertNotNull(message)
        assertTrue(message.contains("No Usage"))
    }
}
