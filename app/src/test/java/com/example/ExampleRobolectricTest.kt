package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.TextPreprocessor
import com.example.engine.UrlRiskAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("AI ScamShield PK", appName)
  }

  @Test
  fun `verify url analyzer detects high risk suspicious domain`() {
    val features = UrlRiskAnalyzer.analyzeUrl("http://192.168.1.1/easypaisa-bonus/login.php")
    assertTrue(features.containsIpAddress)
    assertTrue(features.containsPakistaniBrandKeywords)
    assertTrue(features.heuristicRiskScore >= 70)
  }

  @Test
  fun `verify text preprocessor detects roman urdu and urgency`() {
    val entities = TextPreprocessor.preprocess("Apka account biometric tasdeeq na honay par block ho chuka hai. 8171 par rabta karein.")
    assertEquals("Roman Urdu", entities.detectedLanguage)
    assertTrue(entities.hasUrgencySignals)
    assertTrue(entities.hasFinancialKeywords || entities.hasOtpKeywords)
  }
}
