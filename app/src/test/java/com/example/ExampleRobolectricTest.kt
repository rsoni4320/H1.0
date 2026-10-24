package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.automation.CommandResult
import com.example.automation.CommandRouter
import com.example.data.local.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Heena AI", appName)
    }

    @Test
    fun `command router handles youtube command in hindi`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = CommandRouter(context)
        val result = router.processCommand("YouTube kholo")
        assertTrue("Should handle YouTube intent", result is CommandResult.Handled)
        val handled = result as CommandResult.Handled
        assertEquals("OPEN_YOUTUBE", handled.actionExecuted)
    }

    @Test
    fun `command router handles camera command in hindi`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = CommandRouter(context)
        val result = router.processCommand("Camera kholo photo khicho")
        assertTrue("Should handle Camera intent", result is CommandResult.Handled)
        val handled = result as CommandResult.Handled
        assertEquals("OPEN_CAMERA", handled.actionExecuted)
    }

    @Test
    fun `command router handles structured ai action tag`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = CommandRouter(context)
        val result = router.processCommand("Main aapke liye gaane chala rahi hoon. [[ACTION: OPEN_APP | com.google.android.youtube]]")
        assertTrue(result is CommandResult.Handled)
        val handled = result as CommandResult.Handled
        assertTrue(handled.actionExecuted?.contains("OPEN_APP") == true)
    }

    @Test
    fun `command router gates phone call behind confirmation safeguard`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val router = CommandRouter(context)
        val result = router.processCommand("Call lagao 9876543210")
        assertTrue("Should require user confirmation for phone calls", result is CommandResult.NeedsConfirmation)
    }

    @Test
    fun `preferences manager initializes with heena ai defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)
        assertEquals("Heena AI", prefs.assistantName.value)
        assertEquals("hi", prefs.language.value)
        assertEquals("purple", prefs.themeName.value)
        assertTrue(prefs.wakeWordEnabled.value)
    }
}
