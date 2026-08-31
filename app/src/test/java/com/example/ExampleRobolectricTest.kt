package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Fridge", appName)
  }

  @Test
  fun `test meal planning days of week generation`() {
    val days = com.example.model.DayInfo.getWeekDays(0)
    assertEquals(7, days.size)
    assertEquals("Monday", days[0].dayName)
    assertEquals("Sunday", days[6].dayName)
  }
}
